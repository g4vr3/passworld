package passworld.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Window;
import passworld.data.session.PersistentSessionManager;
import passworld.service.LanguageManager;
import passworld.service.PasswordManager;
import passworld.service.SecurityFilterManager;
import passworld.utils.*;

import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

import static passworld.service.LanguageManager.setLanguageSupport;

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
    @FXML
    Button toggleThemeButton; // Botón para alternar el tema
    @FXML
    ImageView logoImageView; // Logo de la aplicación
    @FXML
    ImageView languageImageView; // Icono del idioma
    @FXML
    Button helpButton; // Botón de ayuda
    @FXML
    ImageView helpImageView; // Icono de ayuda
    @FXML
    Button logoutButton; // Botón de cerrar sesión
    @FXML
    ImageView logoutImageView; // Icono de cerrar sesión

    public static void showView() {
        ViewManager.changeView("/passworld/main-view.fxml", "passworld");
    }

    @FXML
    public void initialize() {
        // Internacionalización: soporte para idiomas.
        setLanguageSupport(languageComboBox, this::setUITexts);

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

        // Detectar el tema del sistema operativo la primera vez
        Platform.runLater(() -> {
            ThemeManager.applyCurrentTheme(passwordField.getScene()); // Aplica el tema actual
        });

        // Listener para cambios de tema
        ThemeManager.darkModeProperty().addListener((_, _, _) -> {
            setIcons(); // Actualiza las imágenes al cambiar el tema
        });

        // Configurar el botón para alternar el tema
        toggleThemeButton.setOnAction(_ -> {
            ThemeManager.toggleTheme(passwordField.getScene()); // Cambia el tema y actualiza la escena
        });
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
        // Configurar el encabezado
        HeaderConfigurator.configureHeader(logoImageView, languageImageView, toggleThemeButton, helpButton);
        // Configurar el botón de cerrar sesión
        HeaderConfigurator.configureLogoutButton(logoutButton, logoutImageView, this::handleLogout);

        // Label contraseña
        Image passwordIcon = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/password_icon.png")).toExternalForm());
        ImageView passwordImageView = new ImageView(passwordIcon);
        ThemeManager.applyThemeToImage(passwordImageView); // Aplica el tema a la imagen
        passwordImageView.getStyleClass().add("icon");
        passwordLabel.setGraphic(passwordImageView);

        //Botón de copiar contraseña
        Image copyIcon = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/copy_icon.png")).toExternalForm());
        ImageView copyImageView = new ImageView(copyIcon);
        ThemeManager.applyThemeToImage(copyImageView); // Aplica el tema a la imagen
        copyImageView.getStyleClass().add("icon");
        copyPasswordButton.setGraphic(copyImageView);

        // Botón de guardar contraseña
        Image saveIcon = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/save_icon.png")).toExternalForm());
        ImageView saveImageView = new ImageView(saveIcon);
        ThemeManager.applyThemeToImage(saveImageView); // Aplica el tema a la imagen
        saveImageView.getStyleClass().add("icon");
        savePasswordButton.setGraphic(saveImageView);

        // Botón de mis contraseñas
        Image vaultIcon = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/vault_icon.png")).toExternalForm());
        ImageView vaultImageView = new ImageView(vaultIcon);
        ThemeManager.applyThemeToImage(vaultImageView); // Aplica el tema a la imagen
        vaultImageView.getStyleClass().add("icon");
        viewMyPasswordsButton.setGraphic(vaultImageView);

        // Label longitud contraseña
        Image lengthIcon = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/length_icon.png")).toExternalForm());
        ImageView lengthImageView = new ImageView(lengthIcon);
        ThemeManager.applyThemeToImage(lengthImageView); // Aplica el tema a la imagen
        lengthImageView.getStyleClass().add("icon");
        passwordLengthLabel.setGraphic(lengthImageView);

        // Botón de generar contraseña
        String createIconPath = ThemeManager.isDarkMode()
                ? "/passworld/images/create_icon_dark_mode.png"
                : "/passworld/images/create_icon.png";
        Image createIcon = new Image(Objects.requireNonNull(getClass().getResource(createIconPath)).toExternalForm());
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

        // Actualiza la fortaleza de la contraseña
        int strength = PasswordEvaluator.calculateStrength(password); // Calcula la fortaleza de la contraseña
        PasswordEvaluator.updatePasswordStrengthInfo(strength, passwordStrengthLabel, passwordStrengthProgressBar); // Actualiza la etiqueta y el ProgressBar
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

        // Verifica si el botón tiene una escena asociada
        if (savePasswordButton.getScene() == null) {
            return; // Salir si no hay una escena
        }

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
                    LogUtils.LOGGER.severe("Validation error while saving password: " + e);
                    // Mostrar mensaje de error de validación
                    Notifier.showNotification(window, e.getMessage());
                } catch (SQLException e) {
                    LogUtils.LOGGER.severe("Database error while saving password: " + e);
                    System.err.println("Error al guardar la contraseña: " + e.getMessage());
                    // Mostrar mensaje de error de base de datos
                    Notifier.showNotification(window, bundle.getString("toolTip_database_error"));
                }
            });
        }
    }

    @FXML
    public void viewPasswords() {
        MyPasswordsController.showView();
    }

    // Establece los textos de la interfaz con los valores del ResourceBundle
    private void setUITexts() {
        helpButton.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_helpButton")));
        toggleThemeButton.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_toggleThemeButton")));
        languageComboBox.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_languageComboBox")));
        logoutButton.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_logoutButton")));

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

        PasswordEvaluator.updateStrengthLabelOnLanguageChange(passwordField.getText(), passwordStrengthLabel, passwordStrengthProgressBar); // Actualiza la etiqueta de fortaleza de la contraseña
    }

    @FXML
    private void handleLogout() {
        // Muestra el diálogo de confirmación
        boolean confirmed = DialogUtil.showConfirmationDialog("logout_confirmation_title", "logout_confirmation_header", "logout_confirmation_message"); // Muestra el diálogo de confirmación

        if (confirmed) {
            try {
                // Borrar tokens y datos de sesión (incluido filtro de seguridad)
                PersistentSessionManager.clearTokens();

                // Notificar al usuario
                Window window = savePasswordButton.getScene().getWindow();

                LogUtils.LOGGER.info("Logout successful");
                Notifier.showNotification(window, LanguageManager.getBundle().getString("toolTip_logout_success"));

                // Cambiar a la vista de inicio de sesión
                ViewManager.changeView("/passworld/authentication-view.fxml", "passworld");
            } catch (Exception e) {
                LogUtils.LOGGER.severe("Error while logging out: " + e);
                System.err.println("Error al cerrar sesión: " + e.getMessage());
                e.printStackTrace();
                // Mostrar mensaje de error
                Window window = savePasswordButton.getScene().getWindow();
                Notifier.showNotification(window, LanguageManager.getBundle().getString("toolTip_logout_error"));
            }
        }
    }
}
