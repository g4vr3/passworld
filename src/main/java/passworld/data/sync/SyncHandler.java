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

public class SyncHandler {
    private static Thread refreshThread = null;

    public static void syncPasswords(List<PasswordDTO> localPasswords) throws IOException, SQLException {
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
        for (PasswordDTO local : localPasswords) {
            // Saltar si está eliminada en remoto (y registrada como eliminada)
            if (local.getIdFb() != null && DeletedPasswordsDAO.existsByIdFb(local.getIdFb())) {
                continue;
            }

            if (!local.isSynced()) {
                local.setLastModified(TimeSyncManager.correctLocalTime(local));

                if (local.getIdFb() == null || local.getIdFb().isEmpty()) {
                    // CREAR
                    String idFb = PasswordsApiClient.createPassword(userId, local);
                    if (idFb != null) {
                        local.setIdFb(idFb);
                        local.setSynced(true);
                        PasswordManager.updatePasswordById(local);
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
                    } else {
                        // ID existe en local pero no en remoto → se borro en otro lado → descartar
                        PasswordDAO.deletePasswordLocalOnly(local.getId());
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
                PasswordManager.savePasswordFromRemote(remote);
            } else {
                // Existe en ambos → comparar última modificación
                if (remote.getLastModified() != null &&
                        (local.getLastModified() == null ||
                                remote.getLastModified().isAfter(local.getLastModified()))) {
                    PasswordManager.updatePasswordByRemote(remote);
                }
            }
        }
    }

    public static boolean hasInternetConnection() {
        try {
            final URL url = new URL("https://www.google.com");
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
                    Thread.sleep(10 * 60 * 1000);
                    // ⏱ 50 minutos
                    if (!hasInternetConnection()) continue;
                    LogUtils.LOGGER.info("Mostrando verificación de clave maestra para refrescar token");

                    boolean verified = VaultProtectionController.showAndVerifyPassword();
                    if (verified) {
                        LogUtils.LOGGER.info("Master key verificada. Refrescando token...");
                        PersistentSessionManager.refreshToken(); //
                    } else {
                        LogUtils.LOGGER.warning("Master key no verificada. No se refresca el token.");
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "TokenRefreshThread");

        refreshThread.setDaemon(true);
        refreshThread.start();
    }
}