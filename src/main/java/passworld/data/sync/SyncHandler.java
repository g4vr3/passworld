package passworld.data.sync;

import passworld.data.PasswordDAO;
import passworld.data.apiclients.PasswordsApiClient;
import passworld.data.PasswordDTO;
import passworld.data.session.UserSession;
import passworld.service.PasswordManager;
import passworld.utils.Notifier;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyncHandler {

    // Descarga todas las contraseñas del usuario desde Firebase
    public static List<PasswordDTO> downloadPasswords() throws IOException {
        String userId = UserSession.getInstance().getUserId();
        return PasswordsApiClient.readAllPasswords(userId);
    }

    // Sube solo las contraseñas locales que no están sincronizadas
    public static void uploadUnsyncedPasswords(List<PasswordDTO> localPasswords) {
        String userId = UserSession.getInstance().getUserId();
        for (PasswordDTO password : localPasswords) {
            if (!password.isSynced()) {
                try {
                    if (password.getIdFb() == null || password.getIdFb().isEmpty()) {
                        // No existe en Firebase, crear y guardar el idFb
                        String idFb = PasswordsApiClient.createPassword(userId, password);
                        if (idFb != null) {
                            password.setIdFb(idFb);
                            password.setSynced(true);
                            PasswordDAO.updatePassword(password);
                        }
                    } else {
                        // Ya existe en Firebase, actualizar
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
                        PasswordDAO.updatePassword(password);
                    }
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void syncPasswords(List<PasswordDTO> localPasswords) throws IOException, SQLException {
        String userId = UserSession.getInstance().getUserId();
        List<PasswordDTO> remotePasswords = PasswordsApiClient.readAllPasswords(userId);

        // Índice rápido de contraseñas locales por idFb
        Set<String> localIds = new HashSet<>();
        for (PasswordDTO local : localPasswords) {
            if (local.getIdFb() != null) {
                localIds.add(local.getIdFb());
            }
        }

        // Añadir al DAO las contraseñas remotas que no estén localmente
        for (PasswordDTO remote : remotePasswords) {
            if (remote.getIdFb() != null && !localIds.contains(remote.getIdFb())) {
                PasswordDAO.createPassword(remote);
            }
        }

        // Sube las locales no sincronizadas
        uploadUnsyncedPasswords(localPasswords);
    }
}