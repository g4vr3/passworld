package passworld.data;

import passworld.service.LanguageManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LocalAuthUtil {

    public static void saveMasterPasswordHash(String hash) throws SQLException {
        String sql = "INSERT INTO master_password (password_hash) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(DDL.getDbUrl());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hash);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException(LanguageManager.getBundle().getString("errorSavingMasterPassword"), e);
        }
    }

    public static void clearMasterPassword() throws SQLException {
        String sql = "DELETE FROM master_password";
        try (Connection conn = DriverManager.getConnection(DDL.getDbUrl());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException(LanguageManager.getBundle().getString("errorClearingMasterPassword"), e);
        }
    }

    public static String getMasterPasswordHash() throws Exception {
        String sql = "SELECT password_hash FROM master_password";
        try (Connection conn = DriverManager.getConnection(DDL.getDbUrl());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password_hash");
            }
        } catch (SQLException e) {
            throw new Exception(LanguageManager.getBundle().getString("errorGettingMasterPasswordHash"), e);
        }
        return null;
    }
}
