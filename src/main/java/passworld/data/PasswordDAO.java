package passworld.data;

import passworld.data.exceptions.EncryptionException;
import passworld.data.session.UserSession;
import passworld.utils.EncryptionUtil;

import javax.crypto.spec.SecretKeySpec;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PasswordDAO {
    private static final String DB_URL = DDL.getDbUrl();

    // Método genérico para realizar operaciones de inserción
    private static boolean executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    // Inserción de una nueva contraseña (encriptada)
    public static boolean createPassword(PasswordDTO password) throws SQLException {
        String sql = "INSERT INTO passwords(description, username, url, password, isWeak, isDuplicate, isCompromised, isUrlUnsafe, lastModified, isSynced, idFb) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return executeUpdate(sql, password.getDescription(), password.getUsername(), password.getUrl(), encryptPassword(password.getPassword()),
                password.isWeak(), password.isDuplicate(), password.isCompromised(), password.isUrlUnsafe(),
                password.getLastModified() != null ? password.getLastModified().toString() : null, password.isSynced(), password.getIdFb());
    }

    // Inserción de una nueva contraseña sin encriptar
    public static boolean createFromRemote(PasswordDTO password) throws SQLException {
        String sql = "INSERT INTO passwords(description, username, url, password, isWeak, isDuplicate, isCompromised, isUrlUnsafe, lastModified, isSynced, idFb) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return executeUpdate(sql, password.getDescription(), password.getUsername(), password.getUrl(), password.getPassword(),
                password.isWeak(), password.isDuplicate(), password.isCompromised(), password.isUrlUnsafe(),
                password.getLastModified() != null ? password.getLastModified().toString() : null, password.isSynced(), password.getIdFb());
    }

    // Actualización de una contraseña (encriptada)
    public static boolean updatePasswordById(PasswordDTO password) throws SQLException {
        String sql = "UPDATE passwords SET description = ?, username = ?, url = ?, password = ?, isWeak = ?, isDuplicate = ?, isCompromised = ?, isUrlUnsafe = ?, lastModified = ?, isSynced = ?, idFb = ? WHERE id = ?";
        return executeUpdate(sql, password.getDescription(), password.getUsername(), password.getUrl(), encryptPassword(password.getPassword()),
                password.isWeak(), password.isDuplicate(), password.isCompromised(), password.isUrlUnsafe(),
                password.getLastModified() != null ? password.getLastModified().toString() : null, password.isSynced(), password.getIdFb(), password.getId());
    }

    // Actualización de una contraseña sin encriptar
    public static boolean updatePasswordFromRemote(PasswordDTO password) throws SQLException {
        String sql = "UPDATE passwords SET description = ?, username = ?, url = ?, password = ?, isWeak = ?, isDuplicate = ?, isCompromised = ?, isUrlUnsafe = ?, lastModified = ?, isSynced = ?, idFb = ? WHERE idFb = ?";
        return executeUpdate(sql, password.getDescription(), password.getUsername(), password.getUrl(), password.getPassword(),
                password.isWeak(), password.isDuplicate(), password.isCompromised(), password.isUrlUnsafe(),
                password.getLastModified() != null ? password.getLastModified().toString() : null, true, password.getIdFb(), password.getIdFb());
    }

    // Lectura de una contraseña por su ID
    public static PasswordDTO readPasswordById(int idSl) throws SQLException {
        String sql = "SELECT * FROM passwords WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSl);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPasswordDTO(rs);
                }
            }
        }
        return null;
    }

    // Lectura de todas las contraseñas sin desencriptar
    public static List<PasswordDTO> readAllPasswords() throws SQLException {
        String sql = "SELECT * FROM passwords";
        return readPasswords(sql, false);
    }

    // Lectura de todas las contraseñas desencriptadas
    public static List<PasswordDTO> readAllPasswordsDecrypted() throws SQLException {
        String sql = "SELECT * FROM passwords";
        return readPasswords(sql, true);
    }

    // Método auxiliar para leer contraseñas (con o sin desencriptar)
    private static List<PasswordDTO> readPasswords(String sql, boolean decrypt) throws SQLException {
        List<PasswordDTO> passwords = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    PasswordDTO password = mapResultSetToPasswordDTO(rs);
                    if (decrypt) {
                        password.setPassword(EncryptionUtil.decryptPassword(password.getPassword(), UserSession.getInstance().getMasterKey()));
                    }
                    passwords.add(password);
                } catch (EncryptionException e) {
                    System.out.println("Error al desencriptar la contraseña: " + e.getMessage());
                }
            }
        }
        return passwords;
    }

    // Eliminación de una contraseña por su ID
    public static boolean deletePassword(int id) throws SQLException {
        PasswordDTO password = readPasswordById(id);
        if (password == null) return false;
        String idFb = password.getIdFb();
        String sql = "DELETE FROM passwords WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0 && idFb != null) {
                DeletedPasswordsDAO.addDeletedIdFb(idFb);
            }
            return rowsDeleted > 0;
        }
    }

    // Eliminación de una contraseña localmente
    public static boolean deletePasswordLocalOnly(int id) throws SQLException {
        String sql = "DELETE FROM passwords WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            System.out.println("Rows deleted local only: " + rowsDeleted);
            return rowsDeleted > 0;
        }
    }

    // Eliminación de todas las contraseñas
    public static boolean deleteAllPasswords() throws SQLException {
        String sql = "DELETE FROM passwords";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        }
    }

    // Actualización de la seguridad de una contraseña (solo atributos de seguridad)
    public static boolean updatePasswordSecurity(PasswordDTO password) throws SQLException {
        String sql = "UPDATE passwords SET isWeak = ?, isDuplicate = ?, isCompromised = ?, isUrlUnsafe = ? WHERE id = ?";
        return executeUpdate(sql, password.isWeak(), password.isDuplicate(), password.isCompromised(), password.isUrlUnsafe(), password.getId());
    }

    // Método auxiliar para mapear un ResultSet a un PasswordDTO
    private static PasswordDTO mapResultSetToPasswordDTO(ResultSet rs) throws SQLException {
        PasswordDTO password = new PasswordDTO(
                rs.getString("description"),
                rs.getString("username"),
                rs.getString("url"),
                rs.getString("password") // sin desencriptar
        );
        password.setId(rs.getInt("id"));
        password.setWeak(rs.getBoolean("isWeak"));
        password.setDuplicate(rs.getBoolean("isDuplicate"));
        password.setCompromised(rs.getBoolean("isCompromised"));
        password.setUrlUnsafe(rs.getBoolean("isUrlUnsafe"));
        password.setSynced(rs.getBoolean("isSynced"));
        password.setIdFb(rs.getString("idFb"));

        String lastModifiedStr = rs.getString("lastModified");
        if (lastModifiedStr != null) {
            password.setLastModified(java.time.LocalDateTime.parse(lastModifiedStr));
        }
        return password;
    }

    // Métodos auxiliares para encriptar
    private static String encryptPassword(String plainPassword) {
        SecretKeySpec masterKey = UserSession.getInstance().getMasterKey();
        if (masterKey == null) throw new IllegalStateException("Master key no disponible");
        try {
            return EncryptionUtil.encryptPassword(plainPassword, masterKey);
        } catch (EncryptionException e) {
            return "UnableToEncryptNotShownForSecurity";
        }
    }

    // Verificar existencia de una contraseña por ID de Facebook
    public static boolean existsByIdFb(String idFb) throws SQLException {
        String sql = "SELECT COUNT(*) FROM passwords WHERE idFb = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idFb);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
