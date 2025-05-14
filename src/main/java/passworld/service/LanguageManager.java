package passworld.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;

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
        if (bundle == null) {
            // Idioma por defecto si no se ha inicializado
            loadLanguage(getSystemLanguage());
        }
        return bundle;
    }

    public static void setLanguageSupport(ComboBox<String> languageComboBox, Runnable setUITexts) {
        // Configura la lista de idiomas
        languageComboBox.setItems(getSupportedLanguages());

        // Asegura que el idioma esté cargado antes de acceder al bundle
        if (bundle == null) {
            loadLanguage(getSystemLanguage());
        }

        languageComboBox.setTooltip(new Tooltip(bundle.getString("toolTip_languageComboBox")));

        // Configura el idioma predeterminado del sistema
        String systemLanguage = getSystemLanguage();
        languageComboBox.setValue(systemLanguage);

        // Cargar los textos de la UI
        setUITexts.run();

        // Listener para cambios en el ComboBox
        languageComboBox.valueProperty().addListener((_, _, newValue) -> {
            loadLanguage(newValue);
            setUITexts.run();
        });
    }
}
