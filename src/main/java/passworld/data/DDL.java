package passworld.data;

import java.io.File;
import java.sql.*;

public class DDL {
    private static final String dbPath;

    static {
        String userHome = System.getProperty("user.home");

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            dbPath = userHome + "\\AppData\\Local\\passworld\\passwords.db";
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            dbPath = userHome + "/Library/Application Support/passworld/passwords.db";
        } else {
            dbPath = userHome + "/.local/share/passworld/passwords.db";
        }

        File dbDir = new File(dbPath).getParentFile();
        if (!dbDir.exists()) {
            boolean created = dbDir.mkdirs();
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
            try (Connection conn = DriverManager.getConnection(getDbUrl())) {
                if (conn != null) {
                    System.out.println("Conexión a la base de datos establecida.");
                    createTable(conn);
                    createMasterPasswordTable(conn); // Crear tabla para la master password
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
                + "username TEXT, "
                + "url TEXT, "
                + "password TEXT NOT NULL, "
                + "isWeak BOOLEAN NOT NULL DEFAULT 0, "
                + "isDuplicate BOOLEAN NOT NULL DEFAULT 0, "
                + "isCompromised BOOLEAN NOT NULL DEFAULT 0, "
                + "isUrlUnsafe BOOLEAN NOT NULL DEFAULT 0, "
                + "lastModified TEXT, "
                + "isSynced BOOLEAN NOT NULL DEFAULT 0, "
                + "idFb TEXT"
                + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Tabla 'passwords' creada o ya existe.");
            System.out.println("Ruta de la base de datos: " + getDbUrl());
        } catch (SQLException e) {
            System.out.println("Error al crear la tabla: " + e.getMessage());
        }
    }

    // Nuevo método para crear la tabla de master password
    private static void createMasterPasswordTable(Connection conn) {
        String createMasterTableSQL = "CREATE TABLE IF NOT EXISTS master_password ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "password_hash TEXT NOT NULL, "
                + "created_at TEXT NOT NULL"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createMasterTableSQL);
            System.out.println("Tabla 'master_password' creada o ya existe.");
        } catch (SQLException e) {
            System.out.println("Error al crear la tabla master_password: " + e.getMessage());
        }
    }
}