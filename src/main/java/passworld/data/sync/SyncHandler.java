package passworld.data.sync;

import passworld.data.DeletedPasswordsDAO;
import passworld.data.PasswordDAO;
import passworld.data.apiclients.PasswordsApiClient;
import passworld.data.PasswordDTO;
import passworld.data.session.UserSession;
import passworld.service.PasswordManager;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.*;

public class SyncHandler {

    public static void syncPasswords(List<PasswordDTO> localPasswords) throws IOException, SQLException {
        String userId = UserSession.getInstance().getUserId();

        // 1. Subir contraseñas locales no sincronizadas
        for (PasswordDTO local : localPasswords) {
            if (!local.isSynced()) {
                if (local.getIdFb() == null || local.getIdFb().isEmpty()) {
                    String idFb = PasswordsApiClient.createPassword(userId, local);
                    if (idFb != null) {
                        local.setIdFb(idFb);
                        local.setSynced(true);
                        PasswordManager.updatePasswordById(local); // Actualiza por id local
                    }
                } else {
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
                    PasswordManager.updatePasswordByRemote(local); // Actualiza por idFb
                }
            }
        }

        // 2. Descargar contraseñas remotas
        List<PasswordDTO> remotePasswords = PasswordsApiClient.readAllPasswords(userId);

        // 3. Mapas para acceso rápido
        Map<String, PasswordDTO> localByIdFb = new HashMap<>();
        for (PasswordDTO p : localPasswords) {
            if (p.getIdFb() != null) localByIdFb.put(p.getIdFb(), p);
        }
        Map<String, PasswordDTO> remoteByIdFb = new HashMap<>();
        for (PasswordDTO p : remotePasswords) {
            if (p.getIdFb() != null) remoteByIdFb.put(p.getIdFb(), p);
        }

        // 4. Eliminar en remoto las contraseñas eliminadas localmente
        for (String deletedIdFb : DeletedPasswordsDAO.getAllDeletedIdFb()) {
            if (remoteByIdFb.containsKey(deletedIdFb)) {
                try {
                    PasswordsApiClient.deletePassword(userId, deletedIdFb);
                } catch (Exception ignored) {}
            }
            DeletedPasswordsDAO.deleteByIdFb(deletedIdFb); // Limpia de la lista de eliminados
        }

        // 5. Sincronizar diferencias
        for (PasswordDTO remote : remotePasswords) {
            if (DeletedPasswordsDAO.existsByIdFb(remote.getIdFb())) continue; // Evita reinsertar eliminadas
            PasswordDTO local = localByIdFb.get(remote.getIdFb());
            if (local == null) {
                PasswordManager.savePasswordFromRemote(remote);
            } else {
                if (remote.getLastModified() != null &&
                        (local.getLastModified() == null || remote.getLastModified().isAfter(local.getLastModified()))) {
                    PasswordManager.updatePasswordByRemote(remote);
                }
            }
        }

        // 6. Eliminar localmente si ya no existe en remoto
        for (PasswordDTO local : new ArrayList<>(localPasswords)) {
            if (local.getIdFb() != null && !remoteByIdFb.containsKey(local.getIdFb())) {
                PasswordDAO.deletePasswordLocalOnly(local.getId());
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
}