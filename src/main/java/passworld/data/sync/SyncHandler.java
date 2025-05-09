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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SyncHandler {
    public static void uploadUnsyncedPasswords(List<PasswordDTO> localPasswords) {
        String userId = UserSession.getInstance().getUserId();
        for (PasswordDTO password : localPasswords) {
            try {
                if (!password.isSynced()) {
                    if (password.getIdFb() == null || password.getIdFb().isEmpty()) {
                        String idFb = PasswordsApiClient.createPassword(userId, password);
                        if (idFb != null) {
                            password.setIdFb(idFb);
                            password.setSynced(true);
                            PasswordDAO.updatePasswordFromRemote(password);
                        }
                    } else {
                        PasswordsApiClient.updatePassword(
                                userId,
                                password.getIdFb(),
                                password.getDescription(),
                                password.getUsername(),
                                password.getUrl(),
                                password.getPassword(),
                                password.isWeak(),
                                password.isDuplicate(),
                                password.isCompromised(),
                                password.isUrlUnsafe(),
                                password.getLastModified().toString()
                        );
                        password.setSynced(true);
                        PasswordDAO.updatePasswordFromRemote(password);
                    }
                }
            } catch (IOException | SQLException e) {
                System.out.println("Error al subir la contraseña: " + e.getMessage());
            }
        }
    }

    public static void syncPasswords(List<PasswordDTO> localPasswords) throws IOException, SQLException {
        String userId = UserSession.getInstance().getUserId();
        List<PasswordDTO> remotePasswords = PasswordsApiClient.readAllPasswords(userId);

        // Mapear IDs locales y remotos
        Set<String> localIds = new HashSet<>();
        for (PasswordDTO local : localPasswords) {
            if (local.getIdFb() != null) {
                localIds.add(local.getIdFb());
            }
        }

        Set<String> remoteIds = new HashSet<>();
        for (PasswordDTO remote : remotePasswords) {
            if (remote.getIdFb() != null) {
                remoteIds.add(remote.getIdFb());
            }
        }

        // 1. Eliminar en remoto las contraseñas eliminadas localmente
        for (String deletedIdFb : DeletedPasswordsDAO.getAllDeletedIdFb()) {
            if (remoteIds.contains(deletedIdFb)) {
                try {
                    PasswordsApiClient.deletePassword(userId, deletedIdFb);
                    Thread.sleep(500);
                } catch (Exception e) {
                    // Manejar error si es necesario
                }
            }
        }

        // 2. Insertar/actualizar localmente desde remoto
        for (PasswordDTO remote : remotePasswords) {
            if (remote.getIdFb() != null && !localIds.contains(remote.getIdFb())) {
                if (!PasswordDAO.existsByIdFb(remote.getIdFb()) && !DeletedPasswordsDAO.existsByIdFb(remote.getIdFb())) {
                    remote.setSynced(true);
                    PasswordManager.savePasswordFromRemote(remote);
                    DeletedPasswordsDAO.deleteByIdFb(remote.getIdFb());
                }
            } else {
                PasswordDTO local = localPasswords.stream()
                        .filter(p -> p.getIdFb() != null && p.getIdFb().equals(remote.getIdFb()))
                        .findFirst().orElse(null);
                if (local != null && remote.getLastModified().isAfter(local.getLastModified())) {
                    remote.setSynced(true);
                    PasswordManager.updatePasswordByRemote(remote);
                }
            }
        }

        // 3. Eliminar localmente si no existe en remoto y quitar de la lista en memoria
        Iterator<PasswordDTO> iterator = localPasswords.iterator();
        while (iterator.hasNext()) {
            PasswordDTO local = iterator.next();
            if (local.getIdFb() != null && !remoteIds.contains(local.getIdFb())) {
                PasswordDAO.deletePasswordLocalOnly(local.getId());
                iterator.remove(); // Elimina también de la lista en memoria
            }
        }

        // 4. Subir contraseñas locales no sincronizadas
        uploadUnsyncedPasswords(localPasswords);
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