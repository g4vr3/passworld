package passworld.service;

import passworld.data.PasswordDAO;
import passworld.data.PasswordDTO;
import passworld.data.sync.SyncHandler;
import passworld.utils.LanguageUtil;
import passworld.utils.LogUtils;
import passworld.utils.SecurityFilterUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class PasswordManager {

    // Guardar una nueva contraseña localmente
    public static boolean savePassword(PasswordDTO newPasswordDTO) throws SQLException {
        validatePasswordData(newPasswordDTO);


        newPasswordDTO.setLastModified(java.time.LocalDateTime.now());
        newPasswordDTO.setSynced(false);

        boolean created = PasswordDAO.createPassword(newPasswordDTO);

        if (created) {
            updateAllPasswordsSecurity();
            return true;
        }
        return false;
    }    // Guardar una nueva contraseña desde remoto
    public static void savePasswordFromRemote(PasswordDTO newPasswordDTO) throws SQLException {
        validatePasswordData(newPasswordDTO);

        // Evitar duplicados por idFb con consulta directa
        if (newPasswordDTO.getIdFb() != null && PasswordDAO.existsByIdFb(newPasswordDTO.getIdFb())) {
            LogUtils.LOGGER.info("Password with idFb " + newPasswordDTO.getIdFb() + " already exists locally, skipping insertion");
            return; // Ya existe, no insertar
        }

        newPasswordDTO.setSynced(true); // Marcar como sincronizada

        boolean created = PasswordDAO.createFromRemote(newPasswordDTO);

        if (created) {
            LogUtils.LOGGER.info("Password successfully saved from remote with idFb: " + newPasswordDTO.getIdFb());
            updateAllPasswordsSecurity();
        } else {
            LogUtils.LOGGER.warning("Failed to save password from remote with idFb: " + newPasswordDTO.getIdFb());
        }
    }

    // Actualizar una contraseña existente localmente
    public static boolean updatePassword(PasswordDTO passwordToUpdate, String description, String username, String url, String password) throws SQLException {
        SyncHandler.startLocalUpdate();
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


        boolean updated = PasswordDAO.updatePassword(updatedPasswordDTO);

        if (updated) {
            updateAllPasswordsSecurity();
        }
        SyncHandler.finishLocalUpdate();
        return updated;

    }

    public static void updatePasswordByRemote(PasswordDTO updatedPasswordDTO) throws SQLException {
        // No modificar lastModified ni isSynced aquí, ya vienen del servidor
        validatePasswordData(updatedPasswordDTO);
        updatedPasswordDTO.setSynced(true);

        boolean updated = PasswordDAO.updatePasswordFromRemote(updatedPasswordDTO);

        if (updated) {
            updateAllPasswordsSecurity();
        }
    }
    public static void updatePasswordById(PasswordDTO updatedPasswordDTO) throws SQLException {
        validatePasswordData(updatedPasswordDTO);


        boolean updated = PasswordDAO.updatePasswordById(updatedPasswordDTO);

        if (updated) {
            updateAllPasswordsSecurity();
        }
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
        return PasswordDAO.readAllPasswordsDecrypted();
    }

    // Método para detectar duplicados físicos
    public static List<PasswordDTO> findPhysicalDuplicates() throws SQLException {
        return PasswordDAO.findPhysicalDuplicates();
    }

    // Método para limpiar duplicados físicos
    public static int cleanPhysicalDuplicates() throws SQLException {
        int cleanedCount = PasswordDAO.cleanPhysicalDuplicates();
        if (cleanedCount > 0) {
            updateAllPasswordsSecurity();
        }
        return cleanedCount;
    }

    // Validar los datos de la contraseña
    private static void validatePasswordData(PasswordDTO passwordDTO) {
        ResourceBundle bundle = LanguageUtil.getBundle();

        if (passwordDTO.getPassword() == null || passwordDTO.getPassword().isEmpty()) {
            LogUtils.LOGGER.severe("Password is empty");
            throw new IllegalArgumentException(bundle.getString("empty_password"));
        }
        if (passwordDTO.getDescription() == null || passwordDTO.getDescription().isEmpty()) {
            LogUtils.LOGGER.severe("Description is empty");
            throw new IllegalArgumentException(bundle.getString("empty_description"));
        }
    }

    // Actualizar el estado de seguridad de todas las contraseñas
    private static void updateAllPasswordsSecurity() {
        try {
            List<PasswordDTO> allPasswords = getAllPasswords();
            SecurityFilterUtils.analyzePasswordList(allPasswords);
            for (PasswordDTO dto : allPasswords) {
                PasswordDAO.updatePasswordSecurity(dto);
            }
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error updating passwords security: " + e);
            e.printStackTrace();
        }
    }
}