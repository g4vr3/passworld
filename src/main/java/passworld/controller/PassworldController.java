package passworld.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import passworld.utils.*;

import java.util.ResourceBundle;

public class PassworldController {
    private final LanguageSupport languageSupport = new LanguageSupport();

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

        // Despliega lista de idiomas soportados al hacer focus al ComboBox
        Accessibility.setShowListOnFocus(languageComboBox);

        // Focus en inicio para el checkbox de mayúsculas y minúsculas,
        // evitando así darle el foco inicial al passwordField y que se oculte su prompt text
        Platform.runLater(() -> upperAndLowerCaseCheckbox.requestFocus());

        // Selecciona todo el texto al enfocar el TextField
        Accessibility.setSelectAllOnFocus(passwordField);

        // Añade el atajo de teclado Ctrl+C para copiar el contenido
        // Se le pasa el controlador como parámetro, ya que este se encargará de llamar al Notifier para mostrar la notificación
        // Uso de Platform.runLater para asegurar que el Scene ya esté disponible
        Platform.runLater(() -> {
            if (passwordField.getScene() != null) {
                Accessibility.addCopyShortcut(passwordField, this);
            }
        });
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

        updatePasswordStrengthInfo(PasswordEvaluator.calculateStrength(password)); // Actualiza la información de la fortaleza de la contraseña (ProgressBar y Label)
    }

    @FXML
    private void copyPasswordToClipboard() {
        // Verifica si hay una contraseña en el TextField antes de copiar y notificar
        if (!passwordField.getText().isEmpty()) {
            Accessibility.copyToClipboard(passwordField);
            notifyPasswordCopiedToClipboard();
        }
    }

    // Método que llama al Notifier para notificar que la contraseña se copió al portapapeles
    public void notifyPasswordCopiedToClipboard() {
        Notifier.showNotification(passwordField.getScene().getWindow(), languageSupport.getBundle().getString("toolTip_textCopiedToClipboard"));
    }

    private void setLanguageSupport() {
        // Configura la lista de idiomas
        languageComboBox.setItems(languageSupport.getSupportedLanguages());

        // Configura el idioma predeterminado del sistema
        String systemLanguage = languageSupport.getSystemLanguage();
        languageComboBox.setValue(systemLanguage);
        languageSupport.loadLanguage(systemLanguage);
        setUITexts();

        // Listener para cambios en el ComboBox
        languageComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            languageSupport.loadLanguage(newValue);
            setUITexts();
        });
    }

    // Establece los textos de la interfaz con los valores del ResourceBundle
    private void setUITexts() {
        ResourceBundle bundle = languageSupport.getBundle();  // Obtener el ResourceBundle desde LanguageSupport
        passwordLabel.setText(bundle.getString("passwordLabel"));
        passwordField.setPromptText(bundle.getString("prompt_passwordField"));
        upperAndLowerCaseCheckbox.setText(bundle.getString("upperAndLowerCaseCheckbox"));
        numberCheckbox.setText(bundle.getString("numberCheckbox"));
        specialCharCheckbox.setText(bundle.getString("specialCharCheckbox"));
        passwordLengthLabel.setText(bundle.getString("passwordLengthLabel"));
        generatePasswordButton.setText(bundle.getString("generatePasswordButton"));

        //Tooltips y accesibilidad
        languageComboBox.setTooltip(new Tooltip(bundle.getString("toolTip_languageComboBox")));
        passwordLabel.setTooltip(new Tooltip(bundle.getString("toolTip_passwordLabel")));
        passwordField.setTooltip(new Tooltip(bundle.getString("toolTip_passwordField")));
        copyPasswordImageView.setAccessibleHelp(bundle.getString("toolTip_copyToClipboard"));
        upperAndLowerCaseCheckbox.setTooltip(new Tooltip(bundle.getString("toolTip_upperAndLowerCaseCheckbox")));
        numberCheckbox.setTooltip(new Tooltip(bundle.getString("toolTip_numberCheckbox")));
        specialCharCheckbox.setTooltip(new Tooltip(bundle.getString("toolTip_specialCharCheckbox")));
        passwordLengthLabel.setTooltip(new Tooltip(bundle.getString("toolTip_passwordLength")));
        passwordLengthSlider.setTooltip(new Tooltip(bundle.getString("toolTip_passwordLength")));
        generatePasswordButton.setTooltip(new Tooltip(bundle.getString("toolTip_generatePassword")));

        updateStrengthLabelOnLanguageChange();
    }

    // Actualiza el ProgressBar y la etiqueta según la fortaleza
    private void updatePasswordStrengthInfo(int strength) {
        double progress = strength / 4.0;  // Divide la fortaleza entre 4 para obtener un valor entre 0 y 1
        passwordStrengthProgressBar.setProgress(progress + 0.1);  // Actualiza el progreso del ProgressBar con un mínimo de 0.1

        // Eliminar cualquier clase anterior al actualizar
        passwordStrengthProgressBar.getStyleClass().removeAll("red", "orange", "yellowgreen", "green");

        // En función de la fortaleza, cambia el color, el texto y los mensajes de ayuda
        switch (strength) {
            case 0:
                passwordStrengthLabel.setText(languageSupport.getBundle().getString("passwordStrengthLabel_0"));
                passwordStrengthLabel.setTextFill(Color.RED);
                passwordStrengthProgressBar.getStyleClass().add("red");
                passwordStrengthLabel.setTooltip(new Tooltip(languageSupport.getBundle().getString("toolTip_passwordStrength_0")));
                passwordStrengthProgressBar.setTooltip(new Tooltip(languageSupport.getBundle().getString("toolTip_passwordStrength_0")));
                break;
            case 1:
                passwordStrengthLabel.setText(languageSupport.getBundle().getString("passwordStrengthLabel_1"));
                passwordStrengthLabel.setTextFill(Color.RED);
                passwordStrengthProgressBar.getStyleClass().add("red");
                passwordStrengthLabel.setTooltip(new Tooltip(languageSupport.getBundle().getString("toolTip_passwordStrength_1")));
                passwordStrengthProgressBar.setTooltip(new Tooltip(languageSupport.getBundle().getString("toolTip_passwordStrength_1")));
                break;
            case 2:
                passwordStrengthLabel.setText(languageSupport.getBundle().getString("passwordStrengthLabel_2"));
                passwordStrengthLabel.setTextFill(Color.ORANGE);
                passwordStrengthProgressBar.getStyleClass().add("orange");
                passwordStrengthLabel.setTooltip(new Tooltip(languageSupport.getBundle().getString("toolTip_passwordStrength_2")));
                passwordStrengthProgressBar.setTooltip(new Tooltip(languageSupport.getBundle().getString("toolTip_passwordStrength_2")));
                break;
            case 3:
                passwordStrengthLabel.setText(languageSupport.getBundle().getString("passwordStrengthLabel_3"));
                passwordStrengthLabel.setTextFill(Color.YELLOWGREEN);
                passwordStrengthProgressBar.getStyleClass().add("yellowgreen");
                passwordStrengthLabel.setTooltip(new Tooltip(languageSupport.getBundle().getString("toolTip_passwordStrength_3")));
                passwordStrengthProgressBar.setTooltip(new Tooltip(languageSupport.getBundle().getString("toolTip_passwordStrength_3")));
                break;
            case 4:
                passwordStrengthLabel.setText(languageSupport.getBundle().getString("passwordStrengthLabel_4"));
                passwordStrengthLabel.setTextFill(Color.GREEN);
                passwordStrengthProgressBar.getStyleClass().add("green");
                passwordStrengthLabel.setTooltip(new Tooltip(languageSupport.getBundle().getString("toolTip_passwordStrength_4")));
                passwordStrengthProgressBar.setTooltip(new Tooltip(languageSupport.getBundle().getString("toolTip_passwordStrength_4")));
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
        int strength = PasswordEvaluator.calculateStrength(passwordField.getText());

        // Actualiza la fortaleza en función de la contraseña actual
        updatePasswordStrengthInfo(strength);
    }
}
