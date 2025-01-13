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

    public static boolean deletePassword(int id) throws SQLException {
        String sql = "DELETE FROM passwords WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Establecer el valor del parámetro 'id'
            stmt.setInt(1, id);

            // Ejecutar la actualización y verificar cuántas filas se han afectado
            int rowsAffected = stmt.executeUpdate();

            // Si se afectaron filas, la eliminación fue exitosa
            return rowsAffected > 0;
        } catch (SQLException e) {
            // En caso de error, lanzar una excepción
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updatePassword(int id, String description, String url, String password) {
        String sql = "UPDATE passwords SET description = ?, url = ?, password = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL); // Obtener conexión a la base de datos
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Establecer los parámetros de la sentencia
            stmt.setString(1, description);
            stmt.setString(2, url);
            stmt.setString(3, password);
            stmt.setInt(4, id);  // Usamos el ID para buscar el registro

            // Ejecutar la actualización
            int rowsUpdated = stmt.executeUpdate();

            // Si se actualizó al menos una fila, la operación fue exitosa
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
