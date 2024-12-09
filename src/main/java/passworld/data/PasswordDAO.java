package passworld.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PasswordDAO {
    // Usamos el método estático para obtener la URL de la base de datos
    private static final String DB_URL = DDL.getDbUrl();

    // Crear una nueva contraseña
    public static boolean createPassword(PasswordDTO password) throws SQLException {
        String sql = "INSERT INTO passwords(description, url, password) VALUES(?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, password.getDescription());
            pstmt.setString(2, password.getUrl());
            pstmt.setString(3, password.getPassword());

            int rowsAffected = pstmt.executeUpdate();

            // Si se insertaron filas, obtener el id generado
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1); // Obtener el id generado
                        password.setId(generatedId); // Establecer el id en el objeto PasswordDTO
                        return true; // La contraseña se guardó exitosamente
                    }
                }
            }
            return false; // Si no se logró guardar o generar el id
        }
    }

    // Leer todas las contraseñas
    public static List<PasswordDTO> readAllPasswords() throws SQLException {
        String sql = "SELECT * FROM passwords";
        List<PasswordDTO> passwords = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PasswordDTO password = new PasswordDTO(
                        rs.getString("description"),
                        rs.getString("url"),
                        rs.getString("password")
                );
                password.setId(rs.getInt("id")); // Establecer el id en el objeto PasswordDTO
                passwords.add(password);
            }
        }
        return passwords;
    }
}
