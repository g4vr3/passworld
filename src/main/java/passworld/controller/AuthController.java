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
import passworld.utils.Accessibility;
import passworld.utils.HeaderConfigurator;
import passworld.utils.ThemeManager;
import passworld.utils.ViewManager;

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
    private Label vaultProtectionLabel, errorLabel, accountDetailsLabel, accountMailLabel, accountPasswordLabel;
    @FXML
    private Label emailErrorLabel, passwordErrorLabel, loginEmailErrorLabel, loginPasswordErrorLabel;
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

        // Validar email y contraseñas
        valid &= validateField(signupMailField, emailErrorLabel, "empty_email");
        valid &= validateField(signupPasswordField, passwordErrorLabel, "empty_password");
        valid &= validateField(signupConfirmPasswordField, passwordErrorLabel, "empty_password");

        // Validar que las contraseñas coincidan
        valid &= validatePasswords(signupPasswordField, signupConfirmPasswordField, passwordErrorLabel, "passwords_do_not_match");

        // Validar campos de contraseña maestra
        valid &= validateMasterPasswordFields();

        signupButton.setDisable(!valid); // Deshabilitar el botón si hay errores
        return valid;
    }

    // Valida los campos relacionados con la contraseña maestra
    private boolean validateMasterPasswordFields() {
        boolean valid = true;

        // Validar campos de contraseña maestra
        valid &= validateField(signupMasterPasswordField, errorLabel, "empty_password");
        valid &= validateField(signupConfirmMasterPasswordField, errorLabel, "empty_password");

        // Validar que las contraseñas maestras coincidan
        valid &= validatePasswords(signupMasterPasswordField, signupConfirmMasterPasswordField, errorLabel, "passwords_do_not_match");

        return valid;
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
            field.clear(); // Vaciar el campo con error
            return false;
        }
        return true;
    }

    // Valida que dos contraseñas coincidan
    private boolean validatePasswords(PasswordField passwordField, PasswordField confirmPasswordField, Label errorLabel, String mismatchMessageKey) {
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            handleNotEqualPasswords(passwordField, confirmPasswordField, errorLabel, mismatchMessageKey);
            passwordField.clear(); // Vaciar ambos campos con error
            confirmPasswordField.clear();
            return false;
        }
        return true;
    }

    // Maneja el caso en que las contraseñas no coincidan
    private void handleNotEqualPasswords(PasswordField passwordField, PasswordField confirmPasswordField, Label errorLabel, String mismatchMessageKey) {
        if (!passwordField.getStyleClass().contains("error-border"))
            passwordField.getStyleClass().add("error-border");
        if (!confirmPasswordField.getStyleClass().contains("error-border"))
            confirmPasswordField.getStyleClass().add("error-border");

        errorLabel.setText(LanguageManager.getBundle().getString(mismatchMessageKey));
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    // Limpia los estilos de error de un campo si ya no está vacío
    private void clearErrorStyles(TextField field, Label errorLabel) {
        if (!field.getText().trim().isEmpty()) { // Solo limpiar si el campo no está vacío
            field.getStyleClass().remove("error-border");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    // Configura los listeners para validar los campos en tiempo real
    private void setupValidationListeners() {
        // Listeners para los campos de registro
        setupFieldListener(signupMailField, signupButton, emailErrorLabel, this::areAllSignupFieldsFilled);
        setupFieldListener(signupPasswordField, signupButton, passwordErrorLabel, this::areAllSignupFieldsFilled);
        setupFieldListener(signupConfirmPasswordField, signupButton, passwordErrorLabel, this::areAllSignupFieldsFilled);
        setupFieldListener(signupMasterPasswordField, signupButton, errorLabel, this::areAllSignupFieldsFilled);
        setupFieldListener(signupConfirmMasterPasswordField, signupButton, errorLabel, this::areAllSignupFieldsFilled);

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