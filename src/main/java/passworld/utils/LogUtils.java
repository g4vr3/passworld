package passworld.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * Utilidad para la gestión de logs en la aplicación Passworld.
 * Configura un logger que escribe en un archivo específico según el sistema operativo.
 */
public class LogUtils {
    // Logger global para la aplicación
    public static final Logger LOGGER = Logger.getLogger("passworldLogger");
    // Ruta del archivo de log, depende del sistema operativo
    private static final String logFilePath;

    static {
        // Obtiene el directorio del usuario y el sistema operativo
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        // Define la ruta del log según el sistema operativo
        if (os.contains("win")) {
            logFilePath = userHome + "\\AppData\\Local\\passworld\\logs\\app.log";
        } else if (os.contains("mac")) {
            logFilePath = userHome + "/Library/Application Support/passworld/logs/app.log";
        } else {
            logFilePath = userHome + "/.local/share/passworld/logs/app.log";
        }

        // Crea el directorio de logs si no existe
        File logDir = new File(logFilePath).getParentFile();
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (created) {
                System.out.println("Directorio de logs creado: " + logDir.getAbsolutePath());
            } else {
                System.out.println("No se pudo crear el directorio de logs.");
            }
        }

        try {
            // Configura el FileHandler para escribir logs en el archivo
            FileHandler fileHandler = new FileHandler(logFilePath, true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al inicializar el logger: " + e.getMessage());
        }
    }
}