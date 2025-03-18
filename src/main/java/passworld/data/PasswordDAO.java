package passworld.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PasswordDAO {
    private static final String DB_URL = DDL.getDbUrl();

    public static boolean createPassword(PasswordDTO password) throws SQLException {
        String sql = "INSERT INTO passwords(description, username, url, password, isWeak, isDuplicate, isCompromised) VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, password.getDescription());
            pstmt.setString(2, password.getUsername());
            pstmt.setString(3, password.getUrl());
            pstmt.setString(4, password.getPassword());
            pstmt.setBoolean(5, password.isWeak());
            pstmt.setBoolean(6, password.isDuplicate());
            pstmt.setBoolean(7, password.isCompromised());

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

    public static boolean updatePassword(int id, String description, String username, String url, String password, boolean isWeak, boolean isDuplicate, boolean isCompromised) throws SQLException {
        String sql = "UPDATE passwords SET description = ?, username = ?, url = ?, password = ?, isWeak = ?, isDuplicate = ?, isCompromised = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, description);
            stmt.setString(2, username);
            stmt.setString(3, url);
            stmt.setString(4, password);
            stmt.setBoolean(5, isWeak);
            stmt.setBoolean(6, isDuplicate);
            stmt.setBoolean(7, isCompromised);
            stmt.setInt(8, id);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    public static PasswordDTO readPasswordById(int id) throws SQLException {
        String sql = "SELECT * FROM passwords WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PasswordDTO password = new PasswordDTO(
                            rs.getString("description"),
                            rs.getString("username"),
                            rs.getString("url"),
                            rs.getString("password")
                    );
                    password.setId(rs.getInt("id"));
                    password.setWeak(rs.getBoolean("isWeak"));
                    password.setDuplicate(rs.getBoolean("isDuplicate"));
                    password.setCompromised(rs.getBoolean("isCompromised"));
                    return password;
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
                PasswordDTO password = new PasswordDTO(
                        rs.getString("description"),
                        rs.getString("username"),
                        rs.getString("url"),
                        rs.getString("password")
                );
                password.setId(rs.getInt("id"));
                password.setWeak(rs.getBoolean("isWeak"));
                password.setDuplicate(rs.getBoolean("isDuplicate"));
                password.setCompromised(rs.getBoolean("isCompromised"));
                passwords.add(password);
            }
        }
        return passwords;
    }

    public static boolean deletePassword(int id) throws SQLException {
        String sql = "DELETE FROM passwords WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        }
    }
}