package g4vr3.passworld;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;


public class PassworldController implements FocusListener {
    @FXML
    TextField passwordField;
    @FXML
    ImageView copyPasswordImageView;
    @FXML
    Label passwordStrengthLabel;
    @FXML
    ProgressBar passwordStrengthProgressBar;
    @FXML
    CheckBox upperAndLowerCasseCheckbox;
    @FXML
    CheckBox numberCheckbox;
    @FXML
    CheckBox specialCharCheckbox;
    @FXML
    Slider passwordLengthSlider;
    @FXML
    Button generatePasswordButton;

    @FXML
    public void initialize() {
        // Focus en inicio para el checkbox de mayúsculas y minúsculas,
        // evitando así darle el foco inicial al passwordField y que se oculte su prompt text
        Platform.runLater(() -> upperAndLowerCasseCheckbox.requestFocus());


        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                passwordField.selectAll();  // Selecciona todo el texto cuando recibe el foco
            }
        });
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
        Tooltip copiedTooltip = new Tooltip("Texto copiado al portapapeles");
        copiedTooltip.setAutoHide(true);
        copiedTooltip.show(passwordField.getScene().getWindow());

        // Ocultar automáticamente después de 2 segundos
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> copiedTooltip.hide());
        pause.play();
    }

    @FXML
    public void generatePassword() {
        boolean uppercase = upperAndLowerCasseCheckbox.isSelected(); // Obtiene el estado del CheckBox de mayúsculas y minúsculas
        boolean numbers = numberCheckbox.isSelected(); // Obtiene el estado del CheckBox de números
        boolean specialChars = specialCharCheckbox.isSelected(); // Obtiene el estado del CheckBox de caracteres especiales
        int length = (int) passwordLengthSlider.getValue(); // Obtiene el valor del Slider de longitud de contraseña

        String password = PasswordGenerator.generatePassword(uppercase, numbers, specialChars, length); // Genera la contraseña con las características seleccionadas
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

        // En funcion de la fortaleza, cambia el color y el texto de la etiqueta
        switch (strength) {
            case 0:
                passwordStrengthLabel.setText("Muy Débil");
                passwordStrengthLabel.setTextFill(Color.RED);
                passwordStrengthProgressBar.getStyleClass().add("red");
                break;
            case 1:
                passwordStrengthLabel.setText("Débil");
                passwordStrengthLabel.setTextFill(Color.RED);
                passwordStrengthProgressBar.getStyleClass().add("red");
                break;
            case 2:
                passwordStrengthLabel.setText("Media");
                passwordStrengthLabel.setTextFill(Color.ORANGE);
                passwordStrengthProgressBar.getStyleClass().add("orange");
                break;
            case 3:
                passwordStrengthLabel.setText("Fuerte");
                passwordStrengthLabel.setTextFill(Color.YELLOWGREEN);
                passwordStrengthProgressBar.getStyleClass().add("yellowgreen");
                break;
            case 4:
                passwordStrengthLabel.setText("Muy Fuerte");
                passwordStrengthLabel.setTextFill(Color.GREEN);
                passwordStrengthProgressBar.getStyleClass().add("green");
                break;
        }

        passwordStrengthProgressBar.setVisible(true); // Muestra el ProgressBar
        passwordStrengthLabel.setVisible(true);  // Muestra la etiqueta de fortaleza
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {

    }
}
