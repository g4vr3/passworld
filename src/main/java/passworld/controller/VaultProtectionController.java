package passworld.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import passworld.data.LocalAuthUtil;
import passworld.data.exceptions.EncryptionException;
import passworld.data.session.UserSession;
import passworld.service.LanguageManager;
import passworld.utils.*;

import static passworld.service.LanguageManager.getBundle;
import static passworld.service.LanguageManager.setLanguageSupport;

public class VaultProtectionController {

    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private Button toggleThemeButton, helpButton;
    @FXML
    private ImageView logoImageView, languageImageView;
    @FXML
    private Label vaultTitleLabel, errorLabel, vaultTextLabel;
    @FXML
    private PasswordField masterPasswordField;

    public static void showView() {
        ViewManager.changeView("/passworld/vault-protection-view.fxml", String.format("passworld - " + getBundle().getString("vaultProtectionLabel")));
    }

    @FXML
    public void initialize() {
        configureLanguageSupport(); // Configurar soporte de idiomas
        configureTheme(); // Configurar el tema visual
        configureIcons(); // Configurar los íconos de la interfaz
        configureAccessibility(); // Configurar accesibilidad
        setupValidationListeners(); // Configurar validaciones de los campos
        setupMasterPasswordField(); // Configura el campo de contraseña para manejar el evento Enter
    }

    // Configura el soporte de idiomas para la interfaz
    private void configureLanguageSupport() {
        setLanguageSupport(languageComboBox, this::setUITexts);
    }

    // Configura el tema visual (modo claro/oscuro)
    private void configureTheme() {
        Platform.runLater(() -> ThemeManager.applyCurrentTheme(toggleThemeButton.getScene()));
        ThemeManager.darkModeProperty().addListener((_, _, _) -> configureIcons());
        toggleThemeButton.setOnAction(_ -> ThemeManager.toggleTheme(toggleThemeButton.getScene()));
    }

    // Configura los íconos de la interfaz según el tema actual
    private void configureIcons() {
        HeaderConfigurator.configureHeader(logoImageView, languageImageView, toggleThemeButton, helpButton);
    }

    // Configura opciones de accesibilidad para la interfaz
    private void configureAccessibility() {
        Accessibility.setShowLanguageListOnFocus(languageComboBox);
        Platform.runLater(() -> {
            masterPasswordField.requestFocus(); // Establece el foco en el campo de contraseña al iniciar
        });
    }

    // Configura el texto de la interfaz según el idioma seleccionado
    private void setUITexts() {
        helpButton.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_helpButton")));
        toggleThemeButton.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_toggleThemeButton")));
        languageComboBox.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_languageComboBox")));

        vaultTitleLabel.setText(getBundle().getString("vaultProtectionLabel"));
        vaultTextLabel.setText(getBundle().getString("vaultProtectionText"));

        errorLabel.setText(getBundle().getString("incorrectPassword"));
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        masterPasswordField.setPromptText(getBundle().getString("master_key_label"));
    }

    // Configurar validaciones de los campos
    private void setupValidationListeners() {
        // Listener para eliminar el mensaje de error y la clase de borde de error al escribir
        masterPasswordField.textProperty().addListener((_, _, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
                masterPasswordField.getStyleClass().remove("error-border");
            }
        });
    }

    // Configura el campo de contraseña para manejar el evento Enter
    private void setupMasterPasswordField() {
        masterPasswordField.setOnAction(_ -> {
            String enteredPassword = masterPasswordField.getText();
            if (isValidPassword(enteredPassword)) {
                // Si la contraseña es válida, se establece la clave maestra en la sesión
                try {
                    UserSession.getInstance().setMasterKey(EncryptionUtil.deriveAESKey(enteredPassword));
                } catch (EncryptionException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText(getBundle().getString("errorDerivingKey"));
                    alert.showAndWait();
                }
                PassworldController.showView(); // Cambia a la vista de Passworld
            } else {
                // Mostrar mensaje de error y agregar clase de borde de error
                errorLabel.setText(getBundle().getString("incorrectPassword"));
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                if (!masterPasswordField.getStyleClass().contains("error-border")) {
                    masterPasswordField.getStyleClass().add("error-border");
                }
                masterPasswordField.clear(); // Limpiar el campo de contraseña
                masterPasswordField.requestFocus(); // Mantener el foco en el campo

                // Agitar el campo de contraseña
                AnimationUtil.shakeField(masterPasswordField);
            }
        });
    }

    private boolean isValidPassword(String enteredPassword) {
            // Obtén el hash de la master password desde la base de datos local
        String storedHash = null;
        try {
            storedHash = LocalAuthUtil.getMasterPasswordHash();
            // Verifica la contraseña ingresada usando EncryptionUtil
            return EncryptionUtil.verifyMasterPassword(enteredPassword, storedHash);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

        return false;
    }
}
