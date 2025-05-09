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

    public static boolean createPassword(PasswordDTO password) throws SQLException {
        String sql = "INSERT INTO passwords(description, username, url, password, isWeak, isDuplicate, isCompromised, isUrlUnsafe, lastModified, isSynced, idFb) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, password.getDescription());
            pstmt.setString(2, password.getUsername());
            pstmt.setString(3, password.getUrl());
            pstmt.setString(4, encryptPassword(password.getPassword()));
            pstmt.setBoolean(5, password.isWeak());
            pstmt.setBoolean(6, password.isDuplicate());
            pstmt.setBoolean(7, password.isCompromised());
            pstmt.setBoolean(8, password.isUrlUnsafe());
            pstmt.setString(9, password.getLastModified() != null ? password.getLastModified().toString() : null);
            pstmt.setBoolean(10, password.isSynced());
            pstmt.setString(11, password.getIdFb());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        password.setId(generatedId);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static boolean createFromRemote(PasswordDTO password) throws SQLException {
        String sql = "INSERT INTO passwords(description, username, url, password, isWeak, isDuplicate, isCompromised, isUrlUnsafe, lastModified, isSynced, idFb) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, password.getDescription());
            pstmt.setString(2, password.getUsername());
            pstmt.setString(3, password.getUrl());
            pstmt.setString(4, password.getPassword()); // Sin encriptar
            pstmt.setBoolean(5, password.isWeak());
            pstmt.setBoolean(6, password.isDuplicate());
            pstmt.setBoolean(7, password.isCompromised());
            pstmt.setBoolean(8, password.isUrlUnsafe());
            pstmt.setString(9, password.getLastModified() != null ? password.getLastModified().toString() : null);
            pstmt.setBoolean(10, password.isSynced());
            pstmt.setString(11, password.getIdFb());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        password.setId(generatedId);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static boolean updatePassword(PasswordDTO password) throws SQLException {
        String sql = "UPDATE passwords SET description = ?, username = ?, url = ?, password = ?, isWeak = ?, isDuplicate = ?, isCompromised = ?, isUrlUnsafe = ?, lastModified = ?, isSynced = ?, idFb = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, password.getDescription());
            stmt.setString(2, password.getUsername());
            stmt.setString(3, password.getUrl());
            stmt.setString(4, encryptPassword(password.getPassword()));
            stmt.setBoolean(5, password.isWeak());
            stmt.setBoolean(6, password.isDuplicate());
            stmt.setBoolean(7, password.isCompromised());
            stmt.setBoolean(8, password.isUrlUnsafe());
            stmt.setString(9, password.getLastModified() != null ? password.getLastModified().toString() : null);
            stmt.setBoolean(10, password.isSynced());
            stmt.setString(11, password.getIdFb());
            stmt.setInt(12, password.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        }
    }
    // Método para actualizar una contraseña sin encriptar
    public static boolean updatePasswordFromRemote(PasswordDTO password) throws SQLException {
        String sql = "UPDATE passwords SET description = ?, username = ?, url = ?, password = ?, isWeak = ?, isDuplicate = ?, isCompromised = ?, isUrlUnsafe = ?, lastModified = ?, isSynced = ?, idFb = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, password.getDescription());
            stmt.setString(2, password.getUsername());
            stmt.setString(3, password.getUrl());
            stmt.setString(4, password.getPassword()); // Sin encriptar
            stmt.setBoolean(5, password.isWeak());
            stmt.setBoolean(6, password.isDuplicate());
            stmt.setBoolean(7, password.isCompromised());
            stmt.setBoolean(8, password.isUrlUnsafe());
            stmt.setString(9, password.getLastModified() != null ? password.getLastModified().toString() : null);
            stmt.setBoolean(10, password.isSynced());
            stmt.setString(11, password.getIdFb());
            stmt.setInt(12, password.getId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        }
    }

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

    public static List<PasswordDTO> readAllPasswords() throws SQLException {
        String sql = "SELECT * FROM passwords";
        List<PasswordDTO> passwords = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PasswordDTO password = mapResultSetToPasswordDTO(rs);
                passwords.add(password);
            }
        }
        return passwords;
    }
    public static List<PasswordDTO> readAllPasswordsDecrypted() throws SQLException {
        String sql = "SELECT * FROM passwords";
        List<PasswordDTO> passwords = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PasswordDTO password = mapResultSetToPasswordDTO(rs);
                password.setPassword(EncryptionUtil.decryptPassword(password.getPassword(), UserSession.getInstance().getMasterKey()));
                passwords.add(password);
            }
        } catch (Exception e){
            System.out.println("Error al desencriptar la contraseña: " + e.getMessage());
        }
        return passwords;
    }

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
    public static boolean deletePasswordLocalOnly(int id) throws SQLException {
        String sql = "DELETE FROM passwords WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        }
    }

    public static boolean deleteAllPasswords() throws SQLException {
        String sql = "DELETE FROM passwords";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        }
    }

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

    // Métodos auxiliares para encriptar y desencriptar usando la master key de la sesión
    private static String encryptPassword(String plainPassword) {
        SecretKeySpec masterKey = UserSession.getInstance().getMasterKey();
        if (masterKey == null) throw new IllegalStateException("Master key no disponible");
        try {
            return EncryptionUtil.encryptPassword(plainPassword, masterKey);
        } catch (EncryptionException e) {
            return "UnableToEncryptNotShownForSecurity";
        }
    }

    public static boolean existsByIdFb(String idFb) throws SQLException {
        String sql = "SELECT COUNT(*) FROM passwords WHERE idFb = ?";
        try (var conn = java.sql.DriverManager.getConnection(PasswordDAO.DB_URL);
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idFb);
            try (var rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}