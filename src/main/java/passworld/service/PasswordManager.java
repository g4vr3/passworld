package passworld.service;

import passworld.data.PasswordDAO;
import passworld.data.PasswordDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class PasswordManager {

    private static final SecurityFilterService securityFilterService = new SecurityFilterService();

    // Guardar una nueva contraseña
    public static boolean savePassword(PasswordDTO newPasswordDTO) throws SQLException {
        validatePasswordData(newPasswordDTO);

        boolean created = PasswordDAO.createPassword(newPasswordDTO);

        if (created) {
            // Solo actualizar la lista de contraseñas únicas si la contraseña es nueva
            securityFilterService.addUniquePassword(newPasswordDTO.getPassword());
            updateAllPasswordsSecurity();
        }

        return created;
    }

    // Actualizar una contraseña existente
    public static boolean updatePassword(PasswordDTO passwordToUpdate, String description, String username, String url, String password) throws SQLException {
        // Crear un nuevo DTO con los datos actualizados
        PasswordDTO updatedPasswordDTO = new PasswordDTO(description, username, url, password);
        validatePasswordData(updatedPasswordDTO);

        // Eliminar la contraseña anterior de la lista de contraseñas únicas
        securityFilterService.removeUniquePassword(passwordToUpdate.getPassword());

        boolean updated = PasswordDAO.updatePassword(
                passwordToUpdate.getId(),
                description,
                username,
                url,
                password,
                updatedPasswordDTO.isWeak(),
                updatedPasswordDTO.isDuplicate(),
                updatedPasswordDTO.isCompromised()
        );

        if (updated) {
            // Después de la actualización, actualizamos todos los estados de seguridad
            updateAllPasswordsSecurity();
        }

        return updated;
    }


    // Eliminar una contraseña
    public static boolean deletePassword(int id) throws SQLException {
        PasswordDTO passwordToDelete = PasswordDAO.readPasswordById(id);
        boolean deleted = PasswordDAO.deletePassword(id);

        if (deleted) {
            // Eliminar la contraseña de la lista de contraseñas únicas
            securityFilterService.removeUniquePassword(passwordToDelete.getPassword());

            // Después de eliminar, actualizar el estado de seguridad de todas las contraseñas
            updateAllPasswordsSecurity();
        }

        return deleted;
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
    private static void updateAllPasswordsSecurity() throws SQLException {
        List<PasswordDTO> allPasswords = PasswordDAO.readAllPasswords();

        // Limpiar la lista de contraseñas únicas para volver a analizar todas las contraseñas
        securityFilterService.clearUniquePasswords();

        // Volver a analizar todas las contraseñas
        for (PasswordDTO dto : allPasswords) {
            // Analizar la seguridad de cada contraseña
            securityFilterService.analyzePasswordSecurity(dto);

            // Actualizar la base de datos con la nueva información de seguridad
            PasswordDAO.updatePassword(
                    dto.getId(),
                    dto.getDescription(),
                    dto.getUsername(),
                    dto.getUrl(),
                    dto.getPassword(),
                    dto.isWeak(),
                    dto.isDuplicate(),
                    dto.isCompromised()
            );
        }


    }
}
