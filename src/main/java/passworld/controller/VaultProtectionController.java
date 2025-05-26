package passworld.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import passworld.data.LocalAuthUtil;
import passworld.data.exceptions.EncryptionException;
import passworld.data.session.UserSession;
import passworld.utils.LanguageUtil;
import passworld.utils.*;

import java.util.concurrent.CountDownLatch;

import static passworld.utils.LanguageUtil.getBundle;
import static passworld.utils.LanguageUtil.setLanguageSupport;

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
    public static boolean passwordVerified = false;

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
        helpButton.setTooltip(new Tooltip(LanguageUtil.getBundle().getString("toolTip_helpButton")));
        toggleThemeButton.setTooltip(new Tooltip(LanguageUtil.getBundle().getString("toolTip_toggleThemeButton")));
        languageComboBox.setTooltip(new Tooltip(LanguageUtil.getBundle().getString("toolTip_languageComboBox")));

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
                    LogUtils.LOGGER.info("Master key derived successfully");
                    passwordVerified = true;
                } catch (EncryptionException e) {
                    LogUtils.LOGGER.severe("Error deriving key: " + e);

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

                LogUtils.LOGGER.warning("Master key verification failed");
            }
        });
    }

    private boolean isValidPassword(String enteredPassword) {
            // Obtén el hash de la master password desde la base de datos local
        String storedHash;
        try {
            storedHash = LocalAuthUtil.getMasterPasswordHash();
            // Verifica la contraseña ingresada usando EncryptionUtil
            return EncryptionUtil.verifyMasterPassword(enteredPassword, storedHash);
        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error while validating password: " + e);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

        return false;
    }

    public static boolean showAndVerifyPassword() {
        passwordVerified = false; // Reiniciar estado antes de mostrar la vista
        CountDownLatch latch = new CountDownLatch(1);

        // Hilo que vigila passwordVerified sin bloquear la UI
        new Thread(() -> {
            while (!passwordVerified) {
                try {
                    Thread.sleep(100); // Pequeña espera para no consumir CPU en exceso
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            latch.countDown(); // Libera el latch cuando passwordVerified cambie
        }).start();

        // Mostrar la vista en el hilo JavaFX
        Platform.runLater(VaultProtectionController::showView);

        try {
            latch.await(); // Espera aquí hasta que passwordVerified cambie
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        return passwordVerified;
    }
}
