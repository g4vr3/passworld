package passworld.data.sync;

import passworld.controller.VaultProtectionController;
import passworld.data.DeletedPasswordsDAO;
import passworld.data.PasswordDAO;
import passworld.data.apiclients.PasswordsApiClient;
import passworld.data.PasswordDTO;
import passworld.data.session.PersistentSessionManager;
import passworld.data.session.UserSession;
import passworld.service.PasswordManager;
import passworld.utils.LogUtils;
import passworld.utils.TimeSyncManager;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SyncHandler {
    private static Thread refreshThread = null;
    // Variable estática para controlar si hay operaciones locales en curso
    private static final AtomicBoolean localUpdateInProgress = new AtomicBoolean(false);


    // Métodos para controlar el estado de las operaciones locales
    public static void startLocalUpdate() {
        localUpdateInProgress.set(true);
    }

    public static void finishLocalUpdate() {
        localUpdateInProgress.set(false);
    }


    public static void syncPasswords(List<PasswordDTO> localPasswords) throws IOException, SQLException {
        if (localUpdateInProgress.get()) {
            LogUtils.LOGGER.info("Sincronización cancelada: hay operaciones locales en progreso");
            return;
        }
        String userId = UserSession.getInstance().getUserId();

        // 1. ELIMINAR en remoto lo que se eliminó en local (evita que vuelva a descargarse)
        List<String> deletedIdFbs = DeletedPasswordsDAO.getAllDeletedIdFb();
        for (String deletedIdFb : deletedIdFbs) {
            try {
                PasswordsApiClient.deletePassword(userId, deletedIdFb);
            } catch (Exception ignored) {
                // Puede fallar si ya no existe en remoto
            }
            DeletedPasswordsDAO.deleteByIdFb(deletedIdFb); // Borrado local del registro de eliminación
        }

        // 2. Descargar contraseñas remotas actualizadas
        List<PasswordDTO> remotePasswords = PasswordsApiClient.readAllPasswords(userId);

        // 3. Crear mapas de acceso rápido
        Map<String, PasswordDTO> remoteByIdFb = new HashMap<>();
        for (PasswordDTO p : remotePasswords) {
            if (p.getIdFb() != null) remoteByIdFb.put(p.getIdFb(), p);
        }

        Map<String, PasswordDTO> localByIdFb = new HashMap<>();
        for (PasswordDTO p : localPasswords) {
            if (p.getIdFb() != null) localByIdFb.put(p.getIdFb(), p);
        }

        // 4. Eliminar localmente contraseñas que ya no existen en remoto (y que no están marcadas como eliminadas)
        for (PasswordDTO local : new ArrayList<>(localPasswords)) {
            if (local.getIdFb() != null &&
                    !remoteByIdFb.containsKey(local.getIdFb()) &&
                    !DeletedPasswordsDAO.existsByIdFb(local.getIdFb())) {
                PasswordDAO.deletePasswordLocalOnly(local.getId());
            }
        }        // 5. Subir contraseñas locales no sincronizadas
        for (PasswordDTO local : new ArrayList<>(localPasswords)) { // Crear copia para evitar modificaciones concurrentes
            // Saltar si está eliminada en remoto (y registrada como eliminada)
            if (local.getIdFb() != null && DeletedPasswordsDAO.existsByIdFb(local.getIdFb())) {
                continue;
            }

            if (!local.isSynced()) {
                local.setLastModified(TimeSyncManager.correctLocalTimeToUtc(local));

                if (local.getIdFb() == null || local.getIdFb().isEmpty()) {
                    // CREAR - Verificar que no hay duplicados por contenido antes de crear
                    boolean isDuplicate = false;
                    for (PasswordDTO remote : remotePasswords) {
                        if (arePasswordsContentEqual(local, remote)) {
                            PasswordManager.updatePasswordByRemote(remote);
                            isDuplicate = true;
                            LogUtils.LOGGER.info("Found duplicate content in remote, linking local password ID " + local.getId() + " with remote idFb " + remote.getIdFb());
                            break;
                        }
                    }
                    
                    if (!isDuplicate) {
                        // No hay duplicados, crear en remoto
                        String idFb = PasswordsApiClient.createPassword(userId, local);
                        if (idFb != null) {
                            local.setIdFb(idFb);
                            local.setSynced(true);
                            PasswordManager.updatePasswordById(local);
                            LogUtils.LOGGER.info("Created new password in remote with idFb: " + idFb);
                        }
                    }
                } else {
                    // Verifica si aún existe en remoto antes de intentar update
                    if (remoteByIdFb.containsKey(local.getIdFb())) {
                        // UPDATE
                        PasswordsApiClient.updatePassword(
                                userId,
                                local.getIdFb(),
                                local.getDescription(),
                                local.getUsername(),
                                local.getUrl(),
                                local.getPassword(),
                                local.isWeak(),
                                local.isDuplicate(),
                                local.isCompromised(),
                                local.isUrlUnsafe(),
                                local.getLastModified().toString()
                        );
                        local.setSynced(true);
                        PasswordManager.updatePasswordByRemote(local);
                        LogUtils.LOGGER.info("Updated password in remote with idFb: " + local.getIdFb());
                    } else {
                        // ID existe en local pero no en remoto → se borro en otro lado → descartar
                        PasswordDAO.deletePasswordLocalOnly(local.getId());
                        LogUtils.LOGGER.info("Deleted local password ID " + local.getId() + " as it no longer exists in remote");
                    }
                }
            }
        }// 6. Sincronizar diferencias desde el servidor hacia local
        for (PasswordDTO remote : remotePasswords) {
            // Ignorar si fue eliminado por el usuario (no debe volver a aparecer)
            if (DeletedPasswordsDAO.existsByIdFb(remote.getIdFb())) continue;

            PasswordDTO local = localByIdFb.get(remote.getIdFb());
            if (local == null) {
                // Verificar una vez más que no existe por idFb antes de insertar
                if (!PasswordDAO.existsByIdFb(remote.getIdFb())) {
                    // Verificar si existe una contraseña con el mismo contenido en local
                    boolean existeContenidoDuplicado = false;
                    for (PasswordDTO localPassword : localPasswords) {
                        if (arePasswordsContentEqual(remote, localPassword)) {
                            // Encontrado duplicado por contenido, vincular con el idFb remoto
                            localPassword.setIdFb(remote.getIdFb());
                            localPassword.setSynced(true);
                            PasswordManager.updatePasswordById(localPassword);
                            LogUtils.LOGGER.info("Encontrado contenido duplicado, vinculando contraseña local con idFb remoto: " + remote.getIdFb());
                            existeContenidoDuplicado = true;
                            break;
                        }
                    }

                    if (!existeContenidoDuplicado) {
                        // No existe en local ni por idFb ni por contenido → insertar
                        PasswordManager.savePasswordFromRemote(remote);
                        LogUtils.LOGGER.info("Inserted new password from remote with idFb: " + remote.getIdFb());
                    }
                }
            }  else {
                // Existe en ambos → comparar última modificación
                if (remote.getLastModified() != null &&
                        (local.getLastModified() == null ||
                                remote.getLastModified().isAfter(local.getLastModified()))) {
                    // Asegurarse de que el ID local esté asignado para evitar duplicación
                    remote.setId(local.getId());
                    PasswordManager.updatePasswordByRemote(remote);
                }
            }
        }    }

    // Método auxiliar para comparar el contenido de dos contraseñas (sin considerar IDs ni timestamps)
    private static boolean arePasswordsContentEqual(PasswordDTO p1, PasswordDTO p2) {
        return safeEquals(p1.getDescription(), p2.getDescription()) &&
               safeEquals(p1.getUsername(), p2.getUsername()) &&
               safeEquals(p1.getUrl(), p2.getUrl()) &&
               safeEquals(p1.getPassword(), p2.getPassword());
    }
    
    // Método auxiliar para comparación segura de strings (manejando nulls)
    private static boolean safeEquals(String s1, String s2) {
        if (s1 == null && s2 == null) return true;
        if (s1 == null || s2 == null) return false;
        return s1.equals(s2);
    }    public static boolean hasInternetConnection() {
        try {
            final URL url = java.net.URI.create("https://www.google.com").toURL();
            final URLConnection conn = url.openConnection();
            conn.connect();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static synchronized void startTokenRefreshThread() {

        if (refreshThread != null && refreshThread.isAlive()) {
            return; // Ya hay un hilo corriendo
        }
        refreshThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(40 * 60 * 1000);

                    if (!hasInternetConnection()) continue;

                    LogUtils.LOGGER.info("Mostrando verificación de clave maestra para refrescar token");

                    // Activar la bandera antes de mostrar el diálogo
                    startLocalUpdate();
                    try {
                        boolean verified = VaultProtectionController.showAndVerifyPassword();

                        if (verified) {
                            LogUtils.LOGGER.info("Master key verificada. Refrescando token...");
                            PersistentSessionManager.refreshToken();
                        } else {
                            LogUtils.LOGGER.warning("Master key no verificada. No se refresca el token.");
                        }
                    } finally {
                        // Desactivar la bandera al terminar (pase lo que pase)
                        finishLocalUpdate();
                    }
                } catch (InterruptedException e) {
                    // ...resto del código...
                }
            }
        }, "TokenRefreshThread");

        refreshThread.setDaemon(true);
        refreshThread.start();
    }
}