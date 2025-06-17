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
        }        
        
        // 5. Subir contraseñas locales no sincronizadas
        for (PasswordDTO local : new ArrayList<>(localPasswords)) { // Crear copia para evitar modificaciones concurrentes
            // IGNORAR si está eliminada en remoto y registrada como eliminada
            if (local.getIdFb() != null && DeletedPasswordsDAO.existsByIdFb(local.getIdFb())) {
                continue;
            } 
            
            // Si no está sincronizada, significa que es nueva o ha sido modificada
            if (!local.isSynced()) {
                local.setLastModified(TimeSyncManager.correctLocalTimeToUtc(local));                
                if (local.getIdFb() == null || local.getIdFb().isEmpty()) {
                    // CREAR en remoto
                    String idFb = PasswordsApiClient.createPassword(userId, local);
                    if (idFb != null) {
                        local.setIdFb(idFb);
                        PasswordManager.updatePasswordById(local);
                        LogUtils.LOGGER.info("Created new password in remote with idFb: " + idFb);
                    }} else {
                    // Verifica si aún existe en remoto antes de intentar update
                    if (remoteByIdFb.containsKey(local.getIdFb())) {
                        // UPDATE - La contraseña existe en remoto, actualizar con los cambios locales
                        PasswordsApiClient.updatePassword(userId, local.getIdFb(), local.getDescription(), local.getUsername(),
                                local.getUrl(), local.getPassword(), local.isWeak(), local.isDuplicate(), local.isCompromised(), 
                                local.isUrlUnsafe(), local.getLastModified().toString());
                        PasswordManager.updatePasswordById(local); // Usar updatePasswordById en lugar de updatePasswordByRemote
                        LogUtils.LOGGER.info("Updated password in remote with local changes for idFb: " + local.getIdFb());
                    } else {
                        // ID existe en local pero no en remoto, entonces se borro en otro lado, descartar
                        PasswordDAO.deletePasswordLocalOnly(local.getId());
                        LogUtils.LOGGER.info("Deleted local password ID " + local.getId() + " as it no longer exists in remote");
                    }
                }
            }
        }        
        // 6. Sincronizar diferencias desde el servidor hacia local
        for (PasswordDTO remote : remotePasswords) {
            // Ignorar si fue eliminado por el usuario (no debe volver a aparecer)
            if (DeletedPasswordsDAO.existsByIdFb(remote.getIdFb())) continue;

            PasswordDTO local = localByIdFb.get(remote.getIdFb());

            if (local == null) {
                // No existe en local → insertar
                if (!PasswordDAO.existsByIdFb(remote.getIdFb())) {
                    PasswordManager.savePasswordFromRemote(remote);
                    LogUtils.LOGGER.info("Inserted new password from remote with idFb: " + remote.getIdFb());
                }            
            } else {
                // Existe en ambos → verificar si el local tiene cambios no sincronizados
                if (!local.isSynced() && local.getIdFb() != null && local.getIdFb().equals(remote.getIdFb())) {
                    // El local tiene cambios pendientes (recién creado o editado), priorizar local sobre remoto
                    LogUtils.LOGGER.info("Local password has unsynchronized changes, keeping local version for idFb: " + local.getIdFb());
                } else if (remote.getLastModified() != null &&
                        (local.getLastModified() == null || remote.getLastModified().isAfter(local.getLastModified()))) {
                    // Control normal por timestamp para contraseñas ya sincronizadas al menos una 
                    remote.setId(local.getId());
                    PasswordManager.updatePasswordByRemote(remote);
                    LogUtils.LOGGER.info("Updated local password from remote for idFb: " + remote.getIdFb());
                }
            }
        }        
        // 7. Marcar como sincronizadas todas las contraseñas que se subieron exitosamente
        for (PasswordDTO local : localPasswords) {
            if (!local.isSynced() && local.getIdFb() != null && !local.getIdFb().isEmpty()) {
                local.setSynced(true);
                PasswordManager.updatePasswordById(local);
                LogUtils.LOGGER.info("Marked password as synced for idFb: " + local.getIdFb());
            }
        }
    }

    public static boolean hasInternetConnection() {
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

                    // Activar la flag antes de mostrar el diálogo
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
                        // Desactivar la flag al terminar (pase lo que pase)
                        finishLocalUpdate();
                    }
                } catch (InterruptedException e) {
                }
            }
        }, "TokenRefreshThread");

        refreshThread.setDaemon(true);
        refreshThread.start();
    }
}