package passworld.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    private static ResourceBundle bundle;

    // Lista de idiomas soportados
    private static final ObservableList<String> supportedLanguages = FXCollections.observableArrayList(
            "Español", "English", "Deutsch"
    );

    public static ObservableList<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    // Método para obtener el idioma predeterminado del sistema
    public static String getSystemLanguage() {
        Locale systemLocale = Locale.getDefault();
        return switch (systemLocale.getLanguage()) {
            case "es" -> "Español";
            case "en" -> "English";
            case "de" -> "Deutsch";
            default -> "Español"; // Español por defecto si no está soportado
        };
    }

    // Método para cargar el ResourceBundle basado en el idioma seleccionado
    public static void loadLanguage(String language) {
        // Verifica si el idioma seleccionado es soportado, si no carga Español por defecto
        String languageCode = switch (language) {
            case "English" -> "en";
            case "Deutsch" -> "de";
            default -> "es"; // Español por defecto si el idioma no está soportado
        };

        // Carga el ResourceBundle correspondiente al idioma seleccionado
        bundle = ResourceBundle.getBundle("passworld.resource_bundle.lang_" + languageCode);
    }

    // Método para obtener el ResourceBundle cargado
    public static ResourceBundle getBundle() {
        return bundle;
    }

}
