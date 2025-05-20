package passworld.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import passworld.utils.LogUtils;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class LanguageManager {
    private static final Preferences prefs = Preferences.userNodeForPackage(LanguageManager.class);
    private static ResourceBundle bundle;
    private static String currentLanguage;

    // Lista de idiomas soportados
    private static final ObservableList<String> supportedLanguages = FXCollections.observableArrayList(
            "Español", "English", "Deutsch"
    );

    public static ObservableList<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    // Obtener el idioma predeterminado del sistema o el guardado en preferencias
    public static String getSystemLanguage() {
        String savedLanguage = prefs.get("language", null);
        if (savedLanguage != null) {
            return savedLanguage;
        }

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
        currentLanguage = language;

        LogUtils.LOGGER.info("Language loaded: " + language);

        // Guarda el idioma seleccionado en las preferencias
        prefs.put("language", language);
        LogUtils.LOGGER.info("Language preferences saved: " + language);
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
            String systemLanguage = getSystemLanguage();
            loadLanguage(systemLanguage);
            languageComboBox.setValue(systemLanguage);
        }

        // Establecer el idioma seleccionado en el ComboBox
        languageComboBox.setValue(currentLanguage);

        // Cargar los textos de la UI
        setUITexts.run();

        // Listener para cambios en el ComboBox
        languageComboBox.valueProperty().addListener((_, _, newValue) -> {
            loadLanguage(newValue);
            setUITexts.run();
        });
    }
}