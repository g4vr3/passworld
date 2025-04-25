package passworld.utils;

import javafx.scene.Scene;
import java.util.prefs.Preferences;

public class ThemeManager {

    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private static boolean isDarkMode = prefs.getBoolean("darkMode", false);

    public static void toggleTheme(Scene scene) {
        if (isDarkMode) {
            scene.getStylesheets().remove(ThemeManager.class.getResource("/passworld/styles/dark-mode.css").toExternalForm());
            scene.getStylesheets().add(ThemeManager.class.getResource("/passworld/styles/styles.css").toExternalForm());
        } else {
            scene.getStylesheets().remove(ThemeManager.class.getResource("/passworld/styles/styles.css").toExternalForm());
            scene.getStylesheets().add(ThemeManager.class.getResource("/passworld/styles/dark-mode.css").toExternalForm());
        }

        isDarkMode = !isDarkMode;
        prefs.putBoolean("darkMode", isDarkMode); // ¡GUARDAR aquí es clave!
    }

    public static void applyCurrentTheme(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getCurrentStylesheet());
    }

    public static String getCurrentStylesheet() {
        return ThemeManager.class.getResource(
                isDarkMode ? "/passworld/styles/dark-mode.css" : "/passworld/styles/styles.css"
        ).toExternalForm();
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }
}

