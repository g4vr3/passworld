package passworld.data;

import java.io.File;
import java.sql.*;

public class DDL {
    private static String dbPath;

    static {
        String userHome = System.getProperty("user.home");

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // En Windows, se usa AppData o una ubicación personalizada
            dbPath = userHome + "\\AppData\\Local\\passworld\\passwords.db";
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            // En macOS, podrías usar Library/Application Support
            dbPath = userHome + "/Library/Application Support/passworld/passwords.db";
        } else {
            // En Linux, utiliza .local/share
            dbPath = userHome + "/.local/share/passworld/passwords.db";
        }

        // Crear directorio si no existe
        File dbDir = new File(dbPath).getParentFile();
        if (!dbDir.exists()) {
            boolean created = dbDir.mkdirs(); // Crea el directorio y todos los directorios intermedios
            if (created) {
                System.out.println("Directorio creado: " + dbDir.getAbsolutePath());
            } else {
                System.out.println("No se pudo crear el directorio.");
            }
        }
    }

    public static String getDbUrl() {
        return "jdbc:sqlite:" + dbPath;
    }

    public static void createDatabase() {
        try {
            // Conectar a la base de datos
            try (Connection conn = DriverManager.getConnection(getDbUrl())) {
                if (conn != null) {
                    // Confirmación de conexión exitosa
                    System.out.println("Conexión a la base de datos establecida.");
                    createTable(conn);
                }
            } catch (SQLException e) {
                System.out.println("Error al conectar con la base de datos: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error al crear el directorio o la base de datos: " + e.getMessage());
        }
    }

    private static void createTable(Connection conn) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS passwords ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "description TEXT NOT NULL, "
                + "url TEXT, "
                + "password TEXT NOT NULL);";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Tabla 'passwords' creada o ya existe.");
            System.out.println("Ruta de la base de datos: " + getDbUrl());  // Muestra la ruta exacta.
        } catch (SQLException e) {
            System.out.println("Error al crear la tabla: " + e.getMessage());
        }
    }
}
