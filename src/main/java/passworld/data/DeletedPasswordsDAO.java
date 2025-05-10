package passworld.data;

import java.sql.*;
import java.util.List;

public class DeletedPasswordsDAO {
    private static final String DB_URL = DDL.getDbUrl();

    public static void addDeletedIdFb(String idFb) throws SQLException {
        String sql = "INSERT OR IGNORE INTO deleted_passwords(idFb, deletedAt) VALUES (?, datetime('now'))";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idFb);
            stmt.executeUpdate();
        }
    }

    public static boolean existsByIdFb(String idFb) throws SQLException {
        String sql = "SELECT COUNT(*) FROM deleted_passwords WHERE idFb = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idFb);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    public static void deleteByIdFb(String idFb) throws SQLException {
        String sql = "DELETE FROM deleted_passwords WHERE idFb = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idFb);
            stmt.executeUpdate();
        }
    }
    public static List<String> getAllDeletedIdFb() throws SQLException {
        String sql = "SELECT idFb FROM deleted_passwords";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            List<String> deletedIds = new java.util.ArrayList<>();
            while (rs.next()) {
                deletedIds.add(rs.getString("idFb"));
            }
            return deletedIds;
        }
    }
}