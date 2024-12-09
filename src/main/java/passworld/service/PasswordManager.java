package passworld.service;

import passworld.data.PasswordDAO;
import passworld.data.PasswordDTO;

import java.sql.SQLException;
import java.util.ResourceBundle;

public class PasswordManager {
    public static boolean savePassword(PasswordDTO passwordDTO) throws SQLException {
        // Valida que los datos del DTO sean correctos
        validatePasswordData(passwordDTO);

        // Interactúa con el DAO para guardar los datos
        return PasswordDAO.createPassword(passwordDTO);
    }

    // Valida los datos del PasswordDTO.
    private static void validatePasswordData(PasswordDTO passwordDTO) {
        ResourceBundle bundle = LanguageManager.getBundle();
        if (passwordDTO.getPassword() == null || passwordDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException(bundle.getString("empty_password"));
        }
        if (passwordDTO.getDescription() == null || passwordDTO.getDescription().isEmpty()) {
            throw new IllegalArgumentException(bundle.getString("empty_description"));
        }
        if (passwordDTO.getUrl() == null || passwordDTO.getUrl().isEmpty()) {
            throw new IllegalArgumentException(bundle.getString("empty_url"));
        }
    }
}
