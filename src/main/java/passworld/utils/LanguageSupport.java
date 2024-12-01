package passworld.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageSupport {

    // Lista de idiomas soportados
    private static final ObservableList<String> supportedLanguages = FXCollections.observableArrayList(
            "Español", "English", "Deutsch"
    );

    public ObservableList<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    // Método para obtener el idioma predeterminado del sistema
    public String getSystemLanguage() {
        Locale systemLocale = Locale.getDefault();
        String systemLanguage = systemLocale.getDisplayLanguage();

        return switch (systemLanguage.toLowerCase()) {
            case "spanish" -> "Español";
            case "english" -> "English";
            case "german" -> "Deutsch";
            default -> "Español"; // Español por defecto si no está soportado
        };
    }

    // Método para cargar el ResourceBundle basado en el idioma seleccionado
    public ResourceBundle loadLanguage(String language) {
        String languageCode = switch (language) {
            case "Español" -> "es";
            case "English" -> "en";
            case "Deutsch" -> "de";
            default -> "es";
        };

        return ResourceBundle.getBundle("passworld/resource_bundle/lang_" + languageCode);
    }
}
