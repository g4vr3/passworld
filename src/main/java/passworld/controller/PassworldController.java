package passworld.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import passworld.service.LanguageManager;
import passworld.service.PasswordManager;
import passworld.utils.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PassworldController {

    @FXML
    Label passwordLabel;
    @FXML
    TextField passwordField;
    @FXML
    Button copyPasswordButton;
    @FXML
    Button savePasswordButton;
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
    Button viewMyPasswordsButton;
    @FXML
    ComboBox<String> languageComboBox;

    public void showView() {
        // Cargar ventana principal y mostrarla
        Stage mainStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/passworld/main-view.fxml"));
        try {
            Scene scene = new Scene(loader.load(), 600, 450);
            scene.getStylesheets().add(getClass().getResource("/passworld/styles/styles.css").toExternalForm());
            mainStage.getIcons().add(new Image(MyPasswordsController.class.getResourceAsStream("/passworld/images/app_icon.png")));
            mainStage.setTitle("passworld");
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {

        // Internacionalización: soporte para idiomas.
        setLanguageSupport();

        // Inicializar iconos
        setIcons();

        // Despliega lista de idiomas soportados al hacer focus al ComboBox
        Accessibility.setShowLanguageListOnFocus(languageComboBox);

        // Focus en inicio para el botón de generar contraseña
        Platform.runLater(() -> generatePasswordButton.requestFocus());

        // Establecer botón de generar contraseña como el predeterminado
        generatePasswordButton.setDefaultButton(true);

        // Selecciona todo el texto al enfocar el TextField
        Accessibility.setSelectAllOnFocus(passwordField);
        
        // Añade los atajos de teclado
        setKeyboardShortcuts();
    }

    private void setKeyboardShortcuts() {
        // Uso de Platform.runLater para asegurar que el Scene ya esté disponible
        Platform.runLater(() -> {
            if (passwordField.getScene() != null) {
                Accessibility.addCopyShortcut(passwordField, this::copyPasswordToClipboard);
                Accessibility.addSavePasswordShortcut(passwordField.getScene(), this::savePassword);
            }
        });
    }

    private void setIcons() {
        // Label contraseña
        Image passwordIcon = new Image(getClass().getResource("/passworld/images/password_icon.png").toExternalForm());
        ImageView passwordImageView = new ImageView(passwordIcon);
        passwordImageView.getStyleClass().add("icon");
        passwordLabel.setGraphic(passwordImageView);

        //Botón de copiar contraseña
        Image copyIcon = new Image(getClass().getResource("/passworld/images/copy_icon.png").toExternalForm());
        ImageView copyImageView = new ImageView(copyIcon);
        copyImageView.getStyleClass().add("icon");
        copyPasswordButton.setGraphic(copyImageView);

        // Botón de guardar contraseña
        Image saveIcon = new Image(getClass().getResource("/passworld/images/save_icon.png").toExternalForm());
        ImageView saveImageView = new ImageView(saveIcon);
        saveImageView.getStyleClass().add("icon");
        savePasswordButton.setGraphic(saveImageView);

        // Botón de mis contraseñas
        Image vaultIcon = new Image(getClass().getResource("/passworld/images/vault_icon.png").toExternalForm());
        ImageView vaultImageView = new ImageView(vaultIcon);
        vaultImageView.getStyleClass().add("icon");
        viewMyPasswordsButton.setGraphic(vaultImageView);

        // Label longitud contraseña
        Image lengthIcon = new Image(getClass().getResource("/passworld/images/length_icon.png").toExternalForm());
        ImageView lengthImageView = new ImageView(lengthIcon);
        lengthImageView.getStyleClass().add("icon");
        passwordLengthLabel.setGraphic(lengthImageView);

        // Botón de generar contraseña
        Image createIcon = new Image(getClass().getResource("/passworld/images/create_icon.png").toExternalForm());
        ImageView createImageView = new ImageView(createIcon);
        createImageView.getStyleClass().add("icon");
        generatePasswordButton.setGraphic(createImageView);
    }

    @FXML
    public void generatePassword() {
        boolean upperAndLowerCase = upperAndLowerCaseCheckbox.isSelected(); // Obtiene el estado del CheckBox de mayúsculas y minúsculas
        boolean numbers = numberCheckbox.isSelected(); // Obtiene el estado del CheckBox de números
        boolean specialChars = specialCharCheckbox.isSelected(); // Obtiene el estado del CheckBox de caracteres especiales
        int length = (int) passwordLengthSlider.getValue(); // Obtiene el valor del Slider de longitud de contraseña

        String password = PasswordGenerator.generatePassword(upperAndLowerCase, numbers, specialChars, length); // Genera la contraseña con las características seleccionadas
        passwordField.setText(password); // Muestra la contraseña en el TextField
        copyPasswordButton.setVisible(true); // Muestra el botón de copia al portapapeles
        savePasswordButton.setVisible(true); // Muestra el botón de guardar la contraseña

        updatePasswordStrengthInfo(PasswordEvaluator.calculateStrength(password)); // Actualiza la información de la fortaleza de la contraseña (ProgressBar y Label)
    }

    @FXML
    public void copyPasswordToClipboard() {
        String password = passwordField.getText();

        Window window = savePasswordButton.getScene().getWindow();
        ResourceBundle bundle = LanguageManager.getBundle();

        // Verifica si hay una contraseña en el TextField antes de copiar
        if (!password.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(password);  // Coloca el texto en el portapapeles
            Clipboard.getSystemClipboard().setContent(content);  // Establece el contenido del portapapeles
            Notifier.showNotification(window, bundle.getString("toolTip_textCopiedToClipboard")); // Muestra la notificación de éxito

        }
    }

    @FXML
    public void savePassword() {
        String password = passwordField.getText();

        Window window = savePasswordButton.getScene().getWindow();
        ResourceBundle bundle = LanguageManager.getBundle();

        if (!password.isEmpty()) {
            // Llama al método utilitario para mostrar el diálogo
            DialogUtil.showPasswordCreationDialog(password).ifPresent(passwordDTO -> {
                try {
                    // Llama a PasswordManager para guardar los datos
                    if (PasswordManager.savePassword(passwordDTO)) {
                        // Mostrar notificación de éxito
                        Notifier.showNotification(window, bundle.getString("toolTip_password_saved"));
                    } else {
                        Notifier.showNotification(window, bundle.getString("toolTip_password_not_saved"));
                    }
                } catch (IllegalArgumentException e) {
                    // Mostrar mensaje de error de validación
                    Notifier.showNotification(window, e.getMessage());
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Mostrar mensaje de error de base de datos
                    Notifier.showNotification(window, bundle.getString("toolTip_database_error"));
                }
            });
        }
    }

    @FXML
    public void viewPasswords() {
        // Muestra el diálogo para introducir la contraseña maestra
        DialogUtil.showUnlockVaultDialog().ifPresent(masterKey -> {
            // Verifica si la contraseña es "admin"
            // Implementar: Creación de master key personalizada en la primera vez
            // Implementar: Desbloqueo según master key personalizada
            if ("admin".equals(masterKey)) {
                // Si la contraseña es correcta, abre la vista de contraseñas en la misma ventana
                MyPasswordsController.showView();  // Pasa el Stage actual
            } else {
                // Si la contraseña es incorrecta, muestra un mensaje de error
                Window window = savePasswordButton.getScene().getWindow();
                ResourceBundle bundle = LanguageManager.getBundle();
                Notifier.showNotification(window, bundle.getString("toolTip_invalid_master_key"));
            }
        });
    }


    private void setLanguageSupport() {
        // Configura la lista de idiomas
        languageComboBox.setItems(LanguageManager.getSupportedLanguages());

        // Configura el idioma predeterminado del sistema
        String systemLanguage = LanguageManager.getSystemLanguage();
        languageComboBox.setValue(systemLanguage);
        LanguageManager.loadLanguage(systemLanguage);
        setUITexts();

        // Listener para cambios en el ComboBox
        languageComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            LanguageManager.loadLanguage(newValue);
            setUITexts();
        });
    }

    // Establece los textos de la interfaz con los valores del ResourceBundle
    private void setUITexts() {
        ResourceBundle bundle = LanguageManager.getBundle();  // Obtener el ResourceBundle desde LanguageSupport
        passwordLabel.setText(bundle.getString("generatedPassword_label"));
        passwordField.setPromptText(bundle.getString("prompt_generatedPasswordField"));
        upperAndLowerCaseCheckbox.setText(bundle.getString("upperAndLowerCaseCheckbox"));
        numberCheckbox.setText(bundle.getString("numberCheckbox"));
        specialCharCheckbox.setText(bundle.getString("specialCharCheckbox"));
        passwordLengthLabel.setText(bundle.getString("passwordLengthLabel"));
        generatePasswordButton.setText(bundle.getString("generatePasswordButton"));
        viewMyPasswordsButton.setText(bundle.getString("viewMyPasswordsButton"));

        //Tooltips y accesibilidad
        languageComboBox.setTooltip(new Tooltip(bundle.getString("toolTip_languageComboBox")));
        passwordLabel.setTooltip(new Tooltip(bundle.getString("toolTip_generatedPasswordLabel")));
        passwordField.setTooltip(new Tooltip(bundle.getString("toolTip_generatedPasswordField")));
        copyPasswordButton.setTooltip(new Tooltip(bundle.getString("toolTip_copyToClipboard")));
        savePasswordButton.setTooltip(new Tooltip(bundle.getString("toolTip_savePassword")));
        upperAndLowerCaseCheckbox.setTooltip(new Tooltip(bundle.getString("toolTip_upperAndLowerCaseCheckbox")));
        numberCheckbox.setTooltip(new Tooltip(bundle.getString("toolTip_numberCheckbox")));
        specialCharCheckbox.setTooltip(new Tooltip(bundle.getString("toolTip_specialCharCheckbox")));
        passwordLengthLabel.setTooltip(new Tooltip(bundle.getString("toolTip_passwordLength")));
        passwordLengthSlider.setTooltip(new Tooltip(bundle.getString("toolTip_passwordLength")));
        generatePasswordButton.setTooltip(new Tooltip(bundle.getString("toolTip_generatePassword")));
        viewMyPasswordsButton.setTooltip(new Tooltip(bundle.getString("toolTip_viewMyPasswords")));

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
                passwordStrengthLabel.setText(LanguageManager.getBundle().getString("passwordStrengthLabel_0"));
                passwordStrengthLabel.setTextFill(Color.RED);
                passwordStrengthProgressBar.getStyleClass().add("red");
                passwordStrengthLabel.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_passwordStrength_0")));
                passwordStrengthProgressBar.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_passwordStrength_0")));
                break;
            case 1:
                passwordStrengthLabel.setText(LanguageManager.getBundle().getString("passwordStrengthLabel_1"));
                passwordStrengthLabel.setTextFill(Color.RED);
                passwordStrengthProgressBar.getStyleClass().add("red");
                passwordStrengthLabel.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_passwordStrength_1")));
                passwordStrengthProgressBar.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_passwordStrength_1")));
                break;
            case 2:
                passwordStrengthLabel.setText(LanguageManager.getBundle().getString("passwordStrengthLabel_2"));
                passwordStrengthLabel.setTextFill(Color.ORANGE);
                passwordStrengthProgressBar.getStyleClass().add("orange");
                passwordStrengthLabel.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_passwordStrength_2")));
                passwordStrengthProgressBar.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_passwordStrength_2")));
                break;
            case 3:
                passwordStrengthLabel.setText(LanguageManager.getBundle().getString("passwordStrengthLabel_3"));
                passwordStrengthLabel.setTextFill(Color.YELLOWGREEN);
                passwordStrengthProgressBar.getStyleClass().add("yellowgreen");
                passwordStrengthLabel.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_passwordStrength_3")));
                passwordStrengthProgressBar.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_passwordStrength_3")));
                break;
            case 4:
                passwordStrengthLabel.setText(LanguageManager.getBundle().getString("passwordStrengthLabel_4"));
                passwordStrengthLabel.setTextFill(Color.GREEN);
                passwordStrengthProgressBar.getStyleClass().add("green");
                passwordStrengthLabel.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_passwordStrength_4")));
                passwordStrengthProgressBar.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_passwordStrength_4")));
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
