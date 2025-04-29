package passworld.service;

import passworld.data.PasswordDAO;
import passworld.data.PasswordDTO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PasswordManager {

    private static final SecurityFilterManager securityFilterService = new SecurityFilterManager();

    // Guardar una nueva contraseña localmente
    public static boolean savePassword(PasswordDTO newPasswordDTO) throws SQLException {
        validatePasswordData(newPasswordDTO);

        newPasswordDTO.setLastModified(LocalDateTime.now());
        newPasswordDTO.setSynced(false);
        boolean created = PasswordDAO.createPassword(newPasswordDTO);

        if (created) {
            securityFilterService.addUniquePassword(newPasswordDTO.getPassword());
            updateAllPasswordsSecurity();
            return true;
        }
        return false;
    }

    // Actualizar una contraseña existente localmente
    public static boolean updatePassword(PasswordDTO passwordToUpdate, String description, String username, String url, String password) throws SQLException {
        PasswordDTO updatedPasswordDTO = new PasswordDTO(description, username, url, password);
        updatedPasswordDTO.setId(passwordToUpdate.getId());
        updatedPasswordDTO.setIdFb(passwordToUpdate.getIdFb());
        updatedPasswordDTO.setLastModified(LocalDateTime.now());
        updatedPasswordDTO.setSynced(false); // Marcar como no sincronizada tras cambios
        updatedPasswordDTO.setWeak(passwordToUpdate.isWeak());
        updatedPasswordDTO.setDuplicate(passwordToUpdate.isDuplicate());
        updatedPasswordDTO.setCompromised(passwordToUpdate.isCompromised());
        updatedPasswordDTO.setUrlUnsafe(passwordToUpdate.isUrlUnsafe());

        validatePasswordData(updatedPasswordDTO);

        securityFilterService.removeUniquePassword(passwordToUpdate.getPassword());

        boolean updated = PasswordDAO.updatePassword(updatedPasswordDTO);

        if (updated) {
            updateAllPasswordsSecurity();
        }
        return updated;
    }
    public static boolean updatePasswordSynced(PasswordDTO updatedPasswordDTO) throws SQLException {
        updatedPasswordDTO.setLastModified(LocalDateTime.now());
        validatePasswordData(updatedPasswordDTO);

        // Solo actualiza en la base de datos, no toques la seguridad
        return PasswordDAO.updatePassword(updatedPasswordDTO);
    }

    // Eliminar una contraseña localmente
    public static boolean deletePassword(int idSl) throws SQLException {
        boolean deleted = PasswordDAO.deletePassword(idSl);

        if (deleted) {
            updateAllPasswordsSecurity();
        }
        return deleted;
    }

    // Obtener una contraseña por su ID local
    public static PasswordDTO getPasswordById(int idSl) throws SQLException {
        return PasswordDAO.readPasswordById(idSl);
    }

    // Obtener todas las contraseñas locales
    public static List<PasswordDTO> getAllPasswords() throws SQLException {
        List<PasswordDTO> allPasswords = PasswordDAO.readAllPasswords();
        return allPasswords != null ? allPasswords : new ArrayList<>();
    }

    // Validar los datos de la contraseña
    private static void validatePasswordData(PasswordDTO passwordDTO) {
        ResourceBundle bundle = LanguageManager.getBundle();

        if (passwordDTO.getPassword() == null || passwordDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException(bundle.getString("empty_password"));
        }
        if (passwordDTO.getDescription() == null || passwordDTO.getDescription().isEmpty()) {
            throw new IllegalArgumentException(bundle.getString("empty_description"));
        }
    }

    // Actualizar el estado de seguridad de todas las contraseñas
    private static void updateAllPasswordsSecurity() {
        try {
            List<PasswordDTO> allPasswords = getAllPasswords();
            securityFilterService.clearUniquePasswords();

            for (PasswordDTO dto : allPasswords) {
                securityFilterService.analyzePasswordSecurity(dto);
                // Actualizar la contraseña en la base de datos
                PasswordDAO.updatePassword(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}