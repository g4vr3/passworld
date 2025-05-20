package passworld.data;

import passworld.utils.LogUtils;

import java.io.File;
import java.sql.*;

public class DDL {
    private static final String dbPath;

    static {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            dbPath = userHome + "\\AppData\\Local\\passworld\\localdb\\passwords.db";
        } else if (os.contains("mac")) {
            dbPath = userHome + "/Library/Application Support/passworld/localdb/passwords.db";
        } else {
            dbPath = userHome + "/.local/share/passworld/localdb/passwords.db";
        }

        File dbDir = new File(dbPath).getParentFile();
        if (!dbDir.exists()) {
            boolean created = dbDir.mkdirs();
            if (created) {
                System.out.println("Directorio creado: " + dbDir.getAbsolutePath());
                LogUtils.LOGGER.info("Database dir created successfully: " + dbDir.getAbsolutePath());
            } else {
                System.out.println("No se pudo crear el directorio.");
                LogUtils.LOGGER.severe("Failed to create database dir: " + dbDir.getAbsolutePath());
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
                    LogUtils.LOGGER.info("Local database connection established successfully.");
                    createPasswordsTable(conn); // Crear tabla para las contraseñas
                    createMasterPasswordTable(conn); // Crear tabla para la master password
                    createDeletedPasswordsTable(conn); // Crear tabla para las contraseñas eliminadas
                }
            } catch (SQLException e) {
                LogUtils.LOGGER.severe("Error connecting to local database: " + e);
                System.out.println("Error al conectar con la base de datos: " + e.getMessage());
            }
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error creating database: " + e);
            System.out.println("Error al crear el directorio o la base de datos: " + e.getMessage());
        }
    }

    private static void createPasswordsTable(Connection conn) {
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
            LogUtils.LOGGER.info("Passwords table created or already exists.");
            System.out.println("Ruta de la base de datos: " + getDbUrl());
            LogUtils.LOGGER.info("Local database path: " + getDbUrl());
        } catch (SQLException e) {
            System.out.println("Error al crear la tabla: " + e.getMessage());
            LogUtils.LOGGER.severe("Error creating passwords table: " + e);
        }
    }

    // Nuevo método para crear la tabla de master password
    private static void createMasterPasswordTable(Connection conn) {
        String createMasterTableSQL = "CREATE TABLE IF NOT EXISTS master_password ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "password_hash TEXT NOT NULL"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createMasterTableSQL);
            System.out.println("Tabla 'master_password' creada o ya existe.");
            LogUtils.LOGGER.info("Master password table created or already exists.");
        } catch (SQLException e) {
            System.out.println("Error al crear la tabla master_password: " + e.getMessage());
            LogUtils.LOGGER.severe("Error creating master password table: " + e);
        }
    }
    private static void createDeletedPasswordsTable(Connection conn) {

        String createDeletedPassTableSQL = "CREATE TABLE IF NOT EXISTS deleted_passwords ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "idFb TEXT NOT NULL UNIQUE, "
                + "deletedAt TEXT NOT NULL"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createDeletedPassTableSQL);
            System.out.println("Tabla 'deleted_passwords' creada o ya existe.");
            LogUtils.LOGGER.info("Deleted passwords table created or already exists.");
        } catch (SQLException e) {
            System.out.println("Error al crear la tabla deleted_passwords: " + e.getMessage());
            LogUtils.LOGGER.severe("Error creating deleted passwords table: " + e);
        }


    }
}