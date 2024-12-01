package passworld.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import javafx.scene.paint.Color;
import passworld.utils.*;

import java.util.ResourceBundle;

public class PassworldController {
    private ResourceBundle bundle;
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

        // Focus en inicio para el checkbox de mayúsculas y minúsculas,
        // evitando así darle el foco inicial al passwordField y que se oculte su prompt text
        Platform.runLater(() -> upperAndLowerCaseCheckbox.requestFocus());

        // Selecciona todo el texto al enfocar el TextField
        Accessibility.setSelectAllOnFocus(passwordField);

        // Añade el atajo de teclado Ctrl+C para copiar el contenido y mostrar la notificación
        // Uso de Platform.runLater para asegurar que el Scene ya está disponible
        Platform.runLater(() -> {
            if (passwordField.getScene() != null) {
                Accessibility.addCopyShortcut(passwordField, passwordField.getScene().getWindow());
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

        updatePasswordStrength(PasswordEvaluator.calculateStrength(password)); // Actualiza la información de la fortaleza de la contraseña (ProgressBar y Label)
    }

    @FXML
    private void copyPasswordToClipboard() {
        // Verifica si hay una contraseña en el TextField antes de llamar copyAndNotify
        if (!passwordField.getText().isEmpty()) {
            Accessibility.copyAndNotify(passwordField, passwordField.getScene().getWindow());
        }
    }

    private void setLanguageSupport() {
        // Configura la lista de idiomas
        languageComboBox.setItems(languageSupport.getSupportedLanguages());

        // Configura el idioma predeterminado del sistema
        String systemLanguage = languageSupport.getSystemLanguage();
        languageComboBox.setValue(systemLanguage);
        bundle = languageSupport.loadLanguage(systemLanguage);
        setUITexts();

        // Listener para cambios en el ComboBox
        languageComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            bundle = languageSupport.loadLanguage(newValue);
            setUITexts();
        });
    }

    // Establece los textos de la interfaz con los valores del ResourceBundle
    private void setUITexts() {
        passwordLabel.setText(bundle.getString("passwordLabel"));
        passwordField.setPromptText(bundle.getString("prompt_passwordField"));
        upperAndLowerCaseCheckbox.setText(bundle.getString("upperAndLowerCaseCheckbox"));
        numberCheckbox.setText(bundle.getString("numberCheckbox"));
        specialCharCheckbox.setText(bundle.getString("specialCharCheckbox"));
        passwordLengthLabel.setText(bundle.getString("passwordLengthLabel"));
        generatePasswordButton.setText(bundle.getString("generatePasswordButton"));
        updateStrengthLabelOnLanguageChange();
    }

    // Notifica que se ha copiado la contraseña al portapapeles
    private void notifyTextCopiedToClipboard() {
        // Se le envía la ventana actual y el mensaje
        Notifier.showNotification(passwordField.getScene().getWindow(), bundle.getString("toolTip_textCopiedToClipboard"));
    }

    // Actualiza el ProgressBar y la etiqueta según la fortaleza
    private void updatePasswordStrength(int strength) {
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
        int strength = PasswordEvaluator.calculateStrength(passwordField.getText());

        // Actualiza la fortaleza en función de la contraseña actual
        updatePasswordStrength(strength);
    }

}
