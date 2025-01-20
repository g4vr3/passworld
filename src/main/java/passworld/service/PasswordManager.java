package passworld.service;

import passworld.data.PasswordDAO;
import passworld.data.PasswordDTO;

import java.sql.SQLException;
import java.util.ResourceBundle;

public class PasswordManager {
    public static boolean savePassword(PasswordDTO passwordDTO) throws SQLException {
        // Validar que los datos del DTO sean correctos
        validatePasswordData(passwordDTO);

        // Interactúa con el DAO para guardar los datos
        return PasswordDAO.createPassword(passwordDTO);
    }

    public static boolean deletePassword(PasswordDTO passwordDTO) throws SQLException {
        return PasswordDAO.deletePassword(passwordDTO.getId());
    }

    public static boolean updatePassword(PasswordDTO passwordToUpdate, String description, String username, String url, String password) throws SQLException {
        // Validar los datos antes de actualizarlos
        validatePasswordData(new PasswordDTO(description, username, url, password));

        // Llamar al DAO para actualizar la contraseña
        return PasswordDAO.updatePassword(passwordToUpdate.getId(), description, username, url, password);
    }

    // Validar los datos del PasswordDTO
    private static void validatePasswordData(PasswordDTO passwordDTO) {
        ResourceBundle bundle = LanguageManager.getBundle();

        // Validar que la contraseña no sea nula ni vacía
        if (passwordDTO.getPassword() == null || passwordDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException(bundle.getString("empty_password"));
        }

        // Validar que la descripción no sea nula ni vacía
        if (passwordDTO.getDescription() == null || passwordDTO.getDescription().isEmpty()) {
            throw new IllegalArgumentException(bundle.getString("empty_description"));
        }
    }
}
