package passworld.utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;

import java.util.prefs.Preferences;

public class ThemeManager {

    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private static boolean isDarkMode = prefs.getBoolean("darkMode", false);
    private static final BooleanProperty darkModeProperty = new SimpleBooleanProperty(isDarkMode);

    public static void toggleTheme(Scene scene) {
        if (isDarkMode) {
            scene.getStylesheets().remove(ThemeManager.class.getResource("/passworld/styles/dark-mode.css").toExternalForm());
            scene.getStylesheets().add(ThemeManager.class.getResource("/passworld/styles/styles.css").toExternalForm());
        } else {
            scene.getStylesheets().remove(ThemeManager.class.getResource("/passworld/styles/styles.css").toExternalForm());
            scene.getStylesheets().add(ThemeManager.class.getResource("/passworld/styles/dark-mode.css").toExternalForm());
        }

        isDarkMode = !isDarkMode;
        prefs.putBoolean("darkMode", isDarkMode); // Guarda la preferencia del usuario
        darkModeProperty.set(isDarkMode); // Notifica a los listeners para que se actualicen las imágenes
    }

    public static void applyCurrentTheme(Scene scene) {
        // Si no hay preferencias guardadas, usar el tema del sistema operativo
        if (!prefs.getBoolean("themeInitialized", false)) {
            isDarkMode = isSystemInDarkMode();
            prefs.putBoolean("darkMode", isDarkMode);
            prefs.putBoolean("themeInitialized", true); // Marcar como inicializado
        }

        scene.getStylesheets().clear();
        scene.getStylesheets().add(getCurrentStylesheet());
    }

    public static String getCurrentStylesheet() {
        return ThemeManager.class.getResource(
                isDarkMode ? "/passworld/styles/dark-mode.css" : "/passworld/styles/styles.css"
        ).toExternalForm();
    }

    public static void applyThemeToImage(ImageView imageView) {
        ColorAdjust colorAdjust = new ColorAdjust();
        if (isDarkMode) {
            colorAdjust.setBrightness(0.7); // Aclara la imagen
        } else {
            colorAdjust.setBrightness(0); // Imagen normal
        }
        imageView.setEffect(colorAdjust);
    }

    public static boolean isSystemInDarkMode() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("mac")) {
            return isMacInDarkMode();
        } else if (osName.contains("win")) {
            return isWindowsInDarkMode();
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return isLinuxInDarkMode();
        }

        return false; // Default to light mode if OS is not recognized
    }

    private static boolean isMacInDarkMode() {
        String appearance = System.getProperty("apple.awt.application.appearance", "light");
        return appearance.toLowerCase().contains("dark");
    }

    private static boolean isWindowsInDarkMode() {
        Preferences userRoot = Preferences.userRoot();
        Preferences registryKey = userRoot.node("Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize");
        int appsUseLightTheme = registryKey.getInt("AppsUseLightTheme", 1); // Default to light mode
        return appsUseLightTheme == 0; // 0 means dark mode
    }

    private static boolean isLinuxInDarkMode() {
        String gtkTheme = System.getenv("GTK_THEME");
        String xdgDesktop = System.getenv("XDG_CURRENT_DESKTOP");

        if (gtkTheme != null && gtkTheme.toLowerCase().contains("dark")) {
            return true;
        }

        return xdgDesktop != null && xdgDesktop.toLowerCase().contains("dark");// Default to light mode
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static BooleanProperty darkModeProperty() {
        return darkModeProperty;
    }
}

