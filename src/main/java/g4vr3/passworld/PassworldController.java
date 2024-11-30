package g4vr3.passworld;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class PassworldController {
    private ResourceBundle bundle;

    @FXML
    Label passwordLabel;
    @FXML
    TextField passwordField;
    @FXML
    ImageView copyPasswordImageView;
    @FXML
    Label passwordStrengthLabel;
    @FXML
    ProgressBar passwordStrengthProgressBar;
    @FXML
    CheckBox upperAndLowerCaseCheckbox;
    @FXML
    CheckBox numberCheckbox;
    @FXML
    CheckBox specialCharCheckbox;
    @FXML
    Label passwordLengthLabel;
    @FXML
    Slider passwordLengthSlider;
    @FXML
    Button generatePasswordButton;
    @FXML
    ComboBox<String> languageComboBox;

    @FXML
    public void initialize() {
        // Internacionalización: soporte para idiomas.
        setLanguageSupport();

        // Focus en inicio para el checkbox de mayúsculas y minúsculas,
        // evitando así darle el foco inicial al passwordField y que se oculte su prompt text
        Platform.runLater(() -> upperAndLowerCaseCheckbox.requestFocus());

        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                passwordField.selectAll();  // Selecciona todo el texto cuando recibe el foco
            }
        });

    }

    private void setLanguageSupport() {

        // Lista de idiomas soportados
        ObservableList<String> languages = FXCollections.observableArrayList(
                List.of("Español", "English", "Deutsch")
        );

        // Valores del selector de idiomas
        languageComboBox.setItems(languages);

        // Obtener el idioma por defecto del sistema
        String systemLanguage = getSystemLanguage();

        // Si el idioma por defecto está soportado, se selecciona,
        // Si no está soportado, se selecciona español por defecto
        if (languages.contains(systemLanguage)) {
            languageComboBox.setValue(systemLanguage);
            loadLanguage(systemLanguage);
        } else {
            languageComboBox.setValue("Español");
            loadLanguage("Español");  // Carga el ResourceBundle para Español por defecto
        }

        // Muestra la lista de idiomas cuando se hace focus en el ComboBox
        languageComboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {  // Si el ComboBox obtiene el focus
                languageComboBox.show();  // Despliega la lista de opciones
            }
        });

        // Cambia el idioma cuando se selecciona un nuevo idioma en el ComboBox
        languageComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Cambiar el ResourceBundle según al idioma seleccionado
            loadLanguage(newValue);
        });
    }

    // Método para obtener el idioma del sistema
    private String getSystemLanguage() {
        // Obtener el idioma por defecto del sistema
        Locale systemLocale = Locale.getDefault();
        String systemLanguage = systemLocale.getDisplayLanguage();

        // Retornar el idioma en formato de la lista de idiomas soportados
        return switch (systemLanguage.toLowerCase()) {
            case "spanish" -> "Español";
            case "english" -> "English";
            case "german" -> "Deutsch";
            default -> "Español"; // Si no está en la lista, Español por defecto
        };
    }

    // Método para cargar el idioma seleccionado
    private void loadLanguage(String language) {
        // Obtener código del idioma seleccionado
        String languageCode = switch (language) {
            case "Español" -> "es"; // Español
            case "English" -> "en"; // Inglés
            case "Deutsch" -> "de"; // Alemán
            default -> "es"; // Español por defecto
        };

        // Cargar el ResourceBundle con el código del idioma seleccionado
        bundle = ResourceBundle.getBundle("g4vr3/passworld/lang_" + languageCode);

        // Actualizar los TextFields y Labels con los valores del ResourceBundle
        passwordLabel.setText(bundle.getString("passwordLabel"));
        passwordField.setPromptText(bundle.getString("prompt_passwordField"));

        upperAndLowerCaseCheckbox.setText(bundle.getString("upperAndLowerCaseCheckbox"));
        numberCheckbox.setText(bundle.getString("numberCheckbox"));
        specialCharCheckbox.setText(bundle.getString("specialCharCheckbox"));

        passwordLengthLabel.setText(bundle.getString("passwordLengthLabel"));

        generatePasswordButton.setText(bundle.getString("generatePasswordButton"));

        // Actualiza la etiqueta de fortaleza si ya existe una contraseña generada
        updateStrengthLabelOnLanguageChange();
    }

    @FXML
    private void copyToClipboard() {
        ClipboardContent content = new ClipboardContent(); // Crea ClipboardContent
        content.putString(passwordField.getText()); // Agrega la contraseña generada al Clipboard
        Clipboard.getSystemClipboard().setContent(content); // Agrega el ClipboardContent al portapapeles del sistema

        notifyTextCopied();
    }

    private void notifyTextCopied() {
        // Crear un Tooltip para la notificación
        Tooltip copiedTooltip = new Tooltip(bundle.getString("toolTip_textCopiedToClipboard"));
        copiedTooltip.setAutoHide(true);
        copiedTooltip.show(passwordField.getScene().getWindow());

        // Ocultar automáticamente después de 2 segundos
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> copiedTooltip.hide());
        pause.play();
    }

    @FXML
    public void generatePassword() {
        boolean upperAndLowerCase = upperAndLowerCaseCheckbox.isSelected(); // Obtiene el estado del CheckBox de mayúsculas y minúsculas
        boolean numbers = numberCheckbox.isSelected(); // Obtiene el estado del CheckBox de números
        boolean specialChars = specialCharCheckbox.isSelected(); // Obtiene el estado del CheckBox de caracteres especiales
        int length = (int) passwordLengthSlider.getValue(); // Obtiene el valor del Slider de longitud de contraseña

        String password = PasswordGenerator.generatePassword(upperAndLowerCase, numbers, specialChars, length); // Genera la contraseña con las características seleccionadas
        passwordField.setText(password); // Muestra la contraseña en el TextField
        copyPasswordImageView.setVisible(true); // Muestra el botón de copia al portapapeles

        updateProgressBar(calculatePasswordStrength(password)); // Actualiza la información de la fortaleza de la contraseña (ProgressBar y Label)
    }

    // Método para calcular la fortaleza de la contraseña
    private int calculatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 8) strength++; // Aumenta la fortaleza si la contraseña tiene al menos 8 caracteres
        if (password.matches(".*[A-Z].*")) strength++; // Aumenta la fortaleza si la contraseña contiene al menos una letra mayúscula
        if (password.matches(".*[0-9].*")) strength++; // Aumenta la fortaleza si la contraseña contiene al menos un número
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) strength++; // Aumenta la fortaleza si la contraseña contiene al menos un carácter especial
        return strength;
    }

    // Actualiza el ProgressBar y la etiqueta según la fortaleza
    private void updateProgressBar(int strength) {
        double progress = strength / 4.0;  // Divide la fortaleza entre 4 para obtener un valor entre 0 y 1
        passwordStrengthProgressBar.setProgress(progress + 0.1);  // Actualiza el progreso del ProgressBar con un mínimo de 0.1

        // Eliminar cualquier clase anterior al actualizar
        passwordStrengthProgressBar.getStyleClass().removeAll("red", "orange", "yellowgreen", "green");

        // En función de la fortaleza, cambia el color y el texto de la etiqueta
        switch (strength) {
            case 0:
                passwordStrengthLabel.setText(bundle.getString("passwordStrengthLabel_0"));
                passwordStrengthLabel.setTextFill(Color.RED);
                passwordStrengthProgressBar.getStyleClass().add("red");
                break;
            case 1:
                passwordStrengthLabel.setText(bundle.getString("passwordStrengthLabel_1"));
                passwordStrengthLabel.setTextFill(Color.RED);
                passwordStrengthProgressBar.getStyleClass().add("red");
                break;
            case 2:
                passwordStrengthLabel.setText(bundle.getString("passwordStrengthLabel_2"));
                passwordStrengthLabel.setTextFill(Color.ORANGE);
                passwordStrengthProgressBar.getStyleClass().add("orange");
                break;
            case 3:
                passwordStrengthLabel.setText(bundle.getString("passwordStrengthLabel_3"));
                passwordStrengthLabel.setTextFill(Color.YELLOWGREEN);
                passwordStrengthProgressBar.getStyleClass().add("yellowgreen");
                break;
            case 4:
                passwordStrengthLabel.setText(bundle.getString("passwordStrengthLabel_4"));
                passwordStrengthLabel.setTextFill(Color.GREEN);
                passwordStrengthProgressBar.getStyleClass().add("green");
                break;
        }

        passwordStrengthProgressBar.setVisible(true); // Muestra el ProgressBar
        passwordStrengthLabel.setVisible(true);  // Muestra la etiqueta de fortaleza
    }

    // Método para actualizar el idioma de la etiqueta de la fortaleza de la contraseña cuando cambie el idioma
    private void updateStrengthLabelOnLanguageChange() {
        if (passwordField.getText().isEmpty()) {
            return; // No actualiza si no hay una contraseña generada
        }

        // Calcular la fortaleza de la contraseña
        int strength = calculatePasswordStrength(passwordField.getText());

        // Actualiza la fortaleza en función de la contraseña actual
        updateProgressBar(strength);
    }

}
