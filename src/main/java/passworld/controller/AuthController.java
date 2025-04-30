package passworld.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import passworld.service.LanguageManager;
import passworld.utils.*;

import java.io.IOException;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import static passworld.service.LanguageManager.setLanguageSupport;

public class AuthController {
    @FXML
    private VBox signupSection, loginSection;
    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private Button toggleThemeButton, loginSectionButton, signupSectionButton, signupButton, loginButton;
    @FXML
    private ImageView logoImageView, languageImageView, infoImageView;
    @FXML
    private Label vaultProtectionLabel, accountDetailsLabel, accountMailLabel, accountPasswordLabel;
    @FXML
    private Label signupEmailErrorLabel, signupPasswordErrorLabel, loginEmailErrorLabel, loginPasswordErrorLabel, signupMasterPasswordErrorLabel;
    @FXML
    private TextField signupMailField, loginMailField;
    @FXML
    private PasswordField signupPasswordField, signupConfirmPasswordField, signupMasterPasswordField, signupConfirmMasterPasswordField, loginPasswordField;

    public static void showView() {
        Stage mainStage = new Stage();
        FXMLLoader loader = new FXMLLoader(AuthController.class.getResource("/passworld/authentication-view.fxml"));
        try {
            Scene scene = new Scene(loader.load(), 600, 450);
            mainStage.getIcons().add(new Image(Objects.requireNonNull(AuthController.class.getResourceAsStream("/passworld/images/app_icon.png"))));
            mainStage.setTitle("passworld");
            mainStage.setScene(scene);
            mainStage.setResizable(false);
            mainStage.show();
            ViewManager.setPrimaryStage(mainStage);
        } catch (IOException e) {
            System.err.println("Error loading authentication view: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        configureLanguageSupport(); // Configurar soporte de idiomas
        configureTheme(); // Configurar el tema visual
        configureIcons(); // Configurar los íconos de la interfaz
        configureAccessibility(); // Configurar accesibilidad
        setupValidationListeners(); // Configurar validaciones de los campos
        showLoginSection(); // Mostrar la sección de inicio de sesión por defecto
        setupKeyNavigationAndActions(); // Configurar navegación con flechas y acción de Enter
    }

    private void setupKeyNavigationAndActions() {
        Platform.runLater(() -> {
            Scene scene = toggleThemeButton.getScene();
            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    switch (event.getCode()) {
                        case LEFT -> {
                            if (signupSection.isVisible()) {
                                showLoginSection();
                                event.consume(); // evita que se propague
                            }
                        }
                        case RIGHT -> {
                            if (loginSection.isVisible()) {
                                showSignupSection();
                                event.consume(); // evita que se propague
                            }
                        }
                        case ENTER -> {
                            if (signupSection.isVisible() && !signupButton.isDisabled()) {
                                handleSignup();
                                event.consume(); // evita que se propague
                            } else if (loginSection.isVisible() && !loginButton.isDisabled()) {
                                handleLogin();
                                event.consume(); // evita que se propague
                            }
                        }
                    }
                });
            }
        });
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
        HeaderConfigurator.configureHeader(logoImageView, languageImageView, toggleThemeButton);
        Image infoIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/passworld/images/info_icon.png")));
        infoImageView.setImage(infoIcon);
        ThemeManager.applyThemeToImage(infoImageView);
    }

    // Configura opciones de accesibilidad para la interfaz
    private void configureAccessibility() {
        Accessibility.setShowLanguageListOnFocus(languageComboBox);
        vaultProtectionLabel.setOnMouseClicked(_ -> showVaultProtectionMessage());
        loginSectionButton.setDefaultButton(false);
        signupSectionButton.setDefaultButton(false);
    }

    // Muestra un mensaje informativo sobre la protección de la bóveda
    private void showVaultProtectionMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LanguageManager.getBundle().getString("vaultProtectionLabel"));
        alert.setHeaderText(null);
        alert.setContentText(LanguageManager.getBundle().getString("vaultProtectionTooltip"));
        alert.getDialogPane().setPrefWidth(400);
        ThemeManager.applyCurrentTheme(alert.getDialogPane().getScene());
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.getStyleClass().add("primary");
        alert.showAndWait();
    }

    // Establece los textos de la interfaz según el idioma seleccionado
    private void setUITexts() {
        vaultProtectionLabel.setText(LanguageManager.getBundle().getString("vaultProtectionLabel"));
        accountPasswordLabel.setText(LanguageManager.getBundle().getString("accountPasswordLabel"));
        accountMailLabel.setText(LanguageManager.getBundle().getString("accountMailLabel"));
        accountDetailsLabel.setText(LanguageManager.getBundle().getString("accountDetailsLabel"));

        signupMailField.setPromptText(LanguageManager.getBundle().getString("signupMailField"));
        signupPasswordField.setPromptText(LanguageManager.getBundle().getString("signupPasswordField"));
        signupConfirmPasswordField.setPromptText(LanguageManager.getBundle().getString("signupConfirmPasswordField"));
        signupMasterPasswordField.setPromptText(LanguageManager.getBundle().getString("signupMasterPasswordField"));
        signupConfirmMasterPasswordField.setPromptText(LanguageManager.getBundle().getString("signupConfirmMasterPasswordField"));
        loginMailField.setPromptText(LanguageManager.getBundle().getString("loginMailField"));
        loginPasswordField.setPromptText(LanguageManager.getBundle().getString("loginPasswordField"));

        loginSectionButton.setText(LanguageManager.getBundle().getString("loginSectionButton"));
        loginSectionButton.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_loginSectionButton")));
        signupSectionButton.setText(LanguageManager.getBundle().getString("signupSectionButton"));
        signupSectionButton.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_signupSectionButton")));
        signupButton.setText(LanguageManager.getBundle().getString("signupButton"));
        signupButton.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_signupButton")));
        loginButton.setText(LanguageManager.getBundle().getString("loginButton"));
        loginButton.setTooltip(new Tooltip(LanguageManager.getBundle().getString("toolTip_loginButton")));
    }

    // Valida los campos del formulario de registro
    private boolean validateSignupFields() {
        boolean valid = true;

        // Validar campos básicos
        valid &= validateField(signupMailField, signupEmailErrorLabel, "empty_email");
        valid &= validateField(signupPasswordField, signupPasswordErrorLabel, "empty_password");
        valid &= validateField(signupConfirmPasswordField, signupPasswordErrorLabel, "empty_password");

        valid &= validateField(signupMasterPasswordField, signupMasterPasswordErrorLabel, "empty_password");
        valid &= validateField(signupConfirmMasterPasswordField, signupMasterPasswordErrorLabel, "empty_password");

        if (!valid) {
            signupButton.setDisable(true);
            return false;
        }

        // 1. Contraseña normal
        if (arePasswordsEqual(signupPasswordField, signupConfirmPasswordField, signupPasswordErrorLabel)) return false;
        if (isPasswordWeak(signupPasswordField, signupConfirmPasswordField, signupPasswordErrorLabel)) return false;
        if (isPasswordCompromised(signupPasswordField, signupConfirmPasswordField, signupPasswordErrorLabel)) return false;

        // 2. Contraseña maestra
        if (arePasswordsEqual(signupMasterPasswordField, signupConfirmMasterPasswordField, signupMasterPasswordErrorLabel)) return false;
        if (isPasswordWeak(signupMasterPasswordField, signupConfirmMasterPasswordField, signupMasterPasswordErrorLabel)) return false;
        if (isPasswordCompromised(signupMasterPasswordField, signupConfirmMasterPasswordField, signupMasterPasswordErrorLabel)) return false;

        signupButton.setDisable(false);
        return true;
    }

    private boolean arePasswordsEqual(PasswordField passwordField, PasswordField confirmPasswordField, Label errorLabel) {
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showFieldError(passwordField, confirmPasswordField, errorLabel, "passwords_do_not_match");
            passwordField.clear();
            confirmPasswordField.clear();
            signupButton.setDisable(true);
            return true;
        }
        return false;
    }

    private boolean isPasswordWeak(PasswordField passwordField, PasswordField confirmPasswordField, Label errorLabel) {
        int strength = PasswordEvaluator.calculateStrength(passwordField.getText());
        if (strength < 3) {
            showFieldError(passwordField, confirmPasswordField, errorLabel, "weak_password");
            passwordField.clear();
            confirmPasswordField.clear();
            signupButton.setDisable(true);
            return true;
        }
        return false;
    }

    private boolean isPasswordCompromised(PasswordField passwordField, PasswordField confirmPasswordField, Label errorLabel) {
        try {
            if (CompromisedPasswordChecker.isCompromisedPassword(passwordField.getText())) {
                showFieldError(passwordField, confirmPasswordField, errorLabel, "compromised_password");
                passwordField.clear();
                confirmPasswordField.clear();
                signupButton.setDisable(true);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error checking compromised password: " + e.getMessage());
            return true;
        }
        return false;
    }

    private void showFieldError(PasswordField pw, PasswordField confirmPw, Label errorLabel, String messageKey) {
        if (!pw.getStyleClass().contains("error-border")) pw.getStyleClass().add("error-border");
        if (!confirmPw.getStyleClass().contains("error-border")) confirmPw.getStyleClass().add("error-border");

        errorLabel.setText(LanguageManager.getBundle().getString(messageKey));
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    // Valida los campos del formulario de inicio de sesión
    private boolean validateLoginFields() {
        boolean valid = true;

        // Validar email y contraseña
        valid &= validateField(loginMailField, loginEmailErrorLabel, "empty_email");
        valid &= validateField(loginPasswordField, loginPasswordErrorLabel, "empty_password");

        loginButton.setDisable(!valid); // Deshabilitar el botón si hay errores
        return valid;
    }

    // Valida un campo de texto y muestra un mensaje de error si es necesario
    private boolean validateField(TextField field, Label errorLabel, String errorMessageKey) {
        if (field.getText().trim().isEmpty()) {
            if (!field.getStyleClass().contains("error-border")) {
                field.getStyleClass().add("error-border");
            }
            errorLabel.setText(LanguageManager.getBundle().getString(errorMessageKey));
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return false;
        } else {
            field.getStyleClass().remove("error-border");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
        return true;
    }

    // Limpia los estilos de error de un campo si ya no está vacío
    private void clearErrorStyles(TextField field, Label errorLabel) {
        if (!field.getText().trim().isEmpty()) {  // Solo limpiar si el campo no está vacío
            field.getStyleClass().remove("error-border");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    // Configura los listeners para validar los campos en tiempo real
    private void setupValidationListeners() {
        // Listeners para los campos de registro
        setupFieldListener(signupMailField, signupButton, signupEmailErrorLabel, this::areAllSignupFieldsFilled);
        setupFieldListener(signupPasswordField, signupButton, signupPasswordErrorLabel, this::areAllSignupFieldsFilled);
        setupFieldListener(signupConfirmPasswordField, signupButton, signupPasswordErrorLabel, this::areAllSignupFieldsFilled);
        setupFieldListener(signupMasterPasswordField, signupButton, signupMasterPasswordErrorLabel, this::areAllSignupFieldsFilled);
        setupFieldListener(signupConfirmMasterPasswordField, signupButton, signupMasterPasswordErrorLabel, this::areAllSignupFieldsFilled);

        // Listeners para los campos de inicio de sesión
        setupFieldListener(loginMailField, loginButton, loginEmailErrorLabel, this::areAllLoginFieldsFilled);
        setupFieldListener(loginPasswordField, loginButton, loginPasswordErrorLabel, this::areAllLoginFieldsFilled);
    }

    // Configura un listener para un campo de texto
    private void setupFieldListener(TextField field, Button button, Label errorLabel, BooleanSupplier areFieldsFilled) {
        field.textProperty().addListener((_, _, _) -> {
            button.setDisable(!areFieldsFilled.getAsBoolean());
            clearErrorStyles(field, errorLabel);
        });
    }

    // Maneja el evento de registro
    @FXML
    private void handleSignup() {
        // Validar todos los campos y mostrar errores si los hay
        if (!validateSignupFields()) {
            signupButton.setDisable(true); // Deshabilitar el botón si hay errores
            return;
        }

        // Si es válido, proceder con el registro
        String masterPassword = signupMasterPasswordField.getText();
        System.out.println("Master password set: " + masterPassword);

        // Solicitar desbloqueo de base de datos
        VaultProtectionController.showView();
    }

    // Maneja el evento de inicio de sesión
    @FXML
    private void handleLogin() {
        // Validar todos los campos y mostrar errores si los hay
        if (!validateLoginFields()) {
            loginButton.setDisable(true); // Deshabilitar el botón si hay errores
            return;
        }

        // Si es válido, proceder con el inicio de sesión

        // Solicitar desbloqueo de base de datos
        VaultProtectionController.showView();
    }

    // Muestra la sección de registro
    @FXML
    private void showSignupSection() {
        toggleSectionVisibility(signupSection, loginSection, signupMailField, signupSectionButton, loginSectionButton);
    }

    // Muestra la sección de inicio de sesión
    @FXML
    private void showLoginSection() {
        toggleSectionVisibility(loginSection, signupSection, loginMailField, loginSectionButton, signupSectionButton);
    }

    // Alterna la visibilidad entre dos secciones
    private void toggleSectionVisibility(VBox showSection, VBox hideSection, TextField emailField, Button activeButton, Button inactiveButton) {
        showSection.setVisible(true);
        hideSection.setVisible(false);

        // Limpiar estado anterior
        loginSectionButton.setDefaultButton(false);
        signupSectionButton.setDefaultButton(false);

        activeButton.getStyleClass().add("auth-section-selected");
        inactiveButton.getStyleClass().remove("auth-section-selected");

        Platform.runLater(emailField::requestFocus);
    }


    // Verifica si todos los campos del formulario de registro están llenos
    private boolean areAllSignupFieldsFilled() {
        return !signupMailField.getText().trim().isEmpty() &&
                !signupPasswordField.getText().trim().isEmpty() &&
                !signupConfirmPasswordField.getText().trim().isEmpty() &&
                !signupMasterPasswordField.getText().trim().isEmpty() &&
                !signupConfirmMasterPasswordField.getText().trim().isEmpty();
    }

    // Verifica si todos los campos del formulario de inicio de sesión están llenos
    private boolean areAllLoginFieldsFilled() {
        return !loginMailField.getText().trim().isEmpty() &&
                !loginPasswordField.getText().trim().isEmpty();
    }
}