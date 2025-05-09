package passworld.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import passworld.data.PasswordDTO;
import passworld.service.LanguageManager;
import passworld.service.PasswordManager;
import passworld.utils.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class PasswordInfoController {

    @FXML
    private Label descriptionLabel, usernameLabel, urlLabel, passwordLabel;

    @FXML
    private Label mandatoryFieldsLabel, mandatoryDescriptionLabel, mandatoryPasswordLabel;

    @FXML
    private TextField descriptionField, usernameField, urlField, passwordFieldVisible;

    @FXML
    private PasswordField passwordFieldHidden;

    @FXML
    private Button copyButton, deleteButton, saveButton, regenerateButton;

    @FXML
    private Button backButton;

    @FXML
    private VBox securityStatusVbox;

    @FXML
    private ImageView securityStatusImageView;

    @FXML
    private ImageView logoImageView, copyImageView, saveImageView, deleteImageView, regenerateImageView;

    @FXML
    public Label passwordStrengthLabel;
    @FXML
    public ProgressBar passwordStrengthProgressBar;

    private PasswordDTO password;
    private MyPasswordsController passwordsController;

    public static void showView(PasswordDTO password, MyPasswordsController passwordsController) {
        try {
            FXMLLoader loader = new FXMLLoader(PasswordInfoController.class.getResource("/passworld/password-info-view.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load(), 600, 450);
            ThemeManager.applyCurrentTheme(scene);
            stage.getIcons().add(new Image(Objects.requireNonNull(PasswordInfoController.class.getResourceAsStream("/passworld/images/app_icon.png"))));
            stage.setTitle("passworld - " + password.getDescription());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            PasswordInfoController controller = loader.getController();
            controller.setData(password, passwordsController);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading PasswordInfoController: " + e.getMessage());
        }
    }

    public void initialize() {
        setIcons(); // Establecer iconos

        saveButton.setVisible(false); // Ocultar por defecto
        copyButton.setVisible(false); // Ocultar botón de copiar
        regenerateButton.setVisible(false); // Ocultar botón de regenerar

        // Configurar el botón de regenerar
        regenerateButton.setOnAction(_ -> regeneratePassword());

        // Listener común para validar cambios en campos
        ChangeListener<String> fieldChangeListener = (_, _, _) -> validateFields();

        descriptionField.textProperty().addListener(fieldChangeListener);
        usernameField.textProperty().addListener(fieldChangeListener);
        urlField.textProperty().addListener(fieldChangeListener);

        passwordFieldVisible.textProperty().addListener((_, _, newVal) -> {
            passwordFieldHidden.setText(newVal); // Sincroniza campos
            validateFields();
            checkPasswordIssues(); // Analiza problemas al cambiar la contraseña
        });

        passwordFieldHidden.textProperty().addListener((_, _, newVal) -> {
            passwordFieldVisible.setText(newVal); // Sincroniza campos
            validateFields();
            checkPasswordIssues(); // Analiza problemas al cambiar la contraseña
        });

        // Copiar al portapapeles
        copyButton.setOnAction(_ -> copyPasswordToClipboard());

        // Mostrar/ocultar contraseña
        passwordFieldHidden.setOnMouseClicked(_ -> togglePasswordVisibility(true));
        passwordFieldVisible.setOnMouseClicked(_ -> togglePasswordVisibility(false));

        // Guardar
        saveButton.setOnAction(_ -> savePassword());

        // Eliminar
        deleteButton.setOnAction(_ -> deletePassword());

        // Configurar el botón de volver
        setBackButton();

        // Texto de la interfaz
        setUIText();

        // Atajos de teclado
        setKeyboardShortcuts();
    }

    private void regeneratePassword() {
        // Generar una nueva contraseña con las políticas por defecto
        String newPassword = PasswordGenerator.generateDefaultPassword();

        // Actualizar el campo de contraseña visible y oculto
        passwordFieldVisible.setText(newPassword);
        passwordFieldHidden.setText(newPassword);
    }

    private void setIcons() {
        // Establecer imagen de logo
        Image logoImage = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/passworld_logo.png")).toExternalForm());
        logoImageView.setImage(logoImage);
        ThemeManager.applyThemeToImage(logoImageView);

        // Establecer imagen de copiar
        Image copyImage = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/copy_icon.png")).toExternalForm());
        copyImageView.setImage(copyImage);
        ThemeManager.applyThemeToImage(copyImageView);
        copyImageView.getStyleClass().add("icon");

        // Establecer imagen de regenerar
        Image regenerateImage = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/reload_icon.png")).toExternalForm());
        regenerateImageView.setImage(regenerateImage);
        ThemeManager.applyThemeToImage(regenerateImageView);
        regenerateImageView.getStyleClass().add("icon");

        // Establecer imagen de guardar
        if (ThemeManager.isDarkMode()) {
            saveImageView.setImage(new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/save_icon_dark_mode.png")).toExternalForm()));
        } else {
            saveImageView.setImage(new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/save_icon_white.png")).toExternalForm()));
        }
        saveImageView.getStyleClass().add("icon");

        // Establecer imagen de eliminar
        Image deleteImage = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/trash_icon.png")).toExternalForm());
        deleteImageView.setImage(deleteImage);
        ThemeManager.applyThemeToImage(deleteImageView);
        deleteImageView.getStyleClass().add("icon");
    }

    private void setKeyboardShortcuts() {
        // Uso de Platform.runLater para asegurar que el Scene ya esté disponible
        Platform.runLater(() -> {
            if (passwordFieldVisible.getScene() != null) {
                Accessibility.addCopyShortcut(passwordFieldVisible, this::copyPasswordToClipboard);
                Accessibility.addSavePasswordShortcut(passwordFieldVisible.getScene(), this::savePassword);
            }
        });
    }

    private void setBackButton() {
        // Establecer icono y estilo del botón de volver
        ViewManager.setBackButton(backButton);

        backButton.getStyleClass().add("icon-button");

        // Configurar la acción del botón de volver
        backButton.setOnAction(_ -> {
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.close();
        });
    }

    public void setUIText() {
        ResourceBundle bundle = LanguageManager.getBundle();

        descriptionLabel.setText(bundle.getString("description_label") + ":*");
        descriptionField.setPromptText(bundle.getString("description_prompt"));

        usernameLabel.setText(bundle.getString("username_label") + ":");
        usernameField.setPromptText(bundle.getString("username_prompt"));

        urlLabel.setText(bundle.getString("url_label") + ":");
        urlField.setPromptText(bundle.getString("url_prompt"));

        passwordLabel.setText(bundle.getString("password_label") + ":*");
        passwordFieldHidden.setPromptText(bundle.getString("password_prompt"));
        passwordFieldVisible.setPromptText(bundle.getString("password_prompt"));

        deleteButton.setText(bundle.getString("delete_button"));
        deleteButton.setTooltip(new Tooltip(bundle.getString("toolTip_delete")));
        saveButton.setText(bundle.getString("save_button"));
        saveButton.setTooltip(new Tooltip(bundle.getString("toolTip_save_update")));
    }


    public void setData(PasswordDTO password, MyPasswordsController passwordsController) {
        this.password = password;
        this.passwordsController = passwordsController;

        // Rellenar campos
        descriptionField.setText(password.getDescription());
        usernameField.setText(password.getUsername());
        urlField.setText(password.getUrl());
        passwordFieldHidden.setText(password.getPassword());
        passwordFieldVisible.setText(password.getPassword());

        checkPasswordIssues();
    }

    private void validateFields() {
        boolean isDescriptionValid = !(descriptionField.getText() == null || descriptionField.getText().trim().isEmpty());
        boolean isPasswordValid = !(passwordFieldVisible.getText() == null || passwordFieldVisible.getText().trim().isEmpty());

        // Validar campos
        mandatoryDescriptionLabel.setText(getBundle().getString("mandatory_description_label"));
        mandatoryDescriptionLabel.setVisible(!isDescriptionValid && isPasswordValid);
        mandatoryDescriptionLabel.setManaged(!isDescriptionValid && isPasswordValid);

        mandatoryPasswordLabel.setText(getBundle().getString("mandatory_password_label"));
        mandatoryPasswordLabel.setVisible(!isPasswordValid && isDescriptionValid);
        mandatoryPasswordLabel.setManaged(!isPasswordValid && isDescriptionValid);

        mandatoryFieldsLabel.setText(getBundle().getString("mandatory_fields_label"));
        mandatoryFieldsLabel.setVisible(!isDescriptionValid && !isPasswordValid);
        mandatoryFieldsLabel.setManaged(!isDescriptionValid && !isPasswordValid);

        // Aplicar estilos de error
        if (!isDescriptionValid) {
            if (!descriptionField.getStyleClass().contains("error-border")) {
                descriptionField.getStyleClass().add("error-border");
            }
        } else {
            descriptionField.getStyleClass().remove("error-border");
        }

        if (!isPasswordValid) {
            if (!passwordFieldVisible.getStyleClass().contains("error-border")) {
                passwordFieldVisible.getStyleClass().add("error-border");
            }
        } else {
            passwordFieldVisible.getStyleClass().remove("error-border");
        }

        // Ocultar el botón de copiar si la contraseña está vacía y respetar visibilidad inicial
        if (copyButton.isVisible()) {
            copyButton.setVisible(isPasswordValid);
        }

        boolean hasChanges =
                !Objects.equals(descriptionField.getText(), password.getDescription()) ||
                        !Objects.equals(usernameField.getText(), password.getUsername()) ||
                        !Objects.equals(urlField.getText(), password.getUrl()) ||
                        !Objects.equals(passwordFieldVisible.getText(), password.getPassword());

        saveButton.setVisible(hasChanges);
        saveButton.setDefaultButton(true); // botón por defecto
        saveButton.setManaged(hasChanges);
        saveButton.setDisable(!isDescriptionValid || !isPasswordValid);
    }

    private void togglePasswordVisibility(boolean showPassword) {
        passwordFieldVisible.setVisible(showPassword);
        passwordFieldHidden.setVisible(!showPassword);
        copyButton.setVisible(showPassword); // Muestra el botón de copiar solo si la contraseña es visible
        regenerateButton.setVisible(showPassword); // Muestra el botón de regenerar solo si la contraseña es visible

    }

    private void copyPasswordToClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(passwordFieldVisible.getText());
        clipboard.setContent(content);

        Notifier.showNotification(copyButton.getScene().getWindow(), getBundle().getString("toolTip_textCopiedToClipboard"));
    }

    private void savePassword() {
        String newDescription = descriptionField.getText();
        String newUsername = usernameField.getText();
        String newUrl = urlField.getText();
        String newPassword = passwordFieldVisible.getText();

        boolean hasChanges =
                !Objects.equals(newDescription, password.getDescription()) ||
                        !Objects.equals(newUsername, password.getUsername()) ||
                        !Objects.equals(newUrl, password.getUrl()) ||
                        !Objects.equals(newPassword, password.getPassword());

        if (hasChanges) {
            // Guardar en la base de datos
            passwordsController.updatePassword(password, newDescription, newUsername, newUrl, newPassword);

            // Recargar el objeto desde la base de datos
            PasswordDTO updatedPassword;
            try {
                updatedPassword = PasswordManager.getPasswordById(password.getId());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (updatedPassword != null) {
                this.password = updatedPassword;

                // Actualizar los campos con los datos sincronizados
                descriptionField.setText(password.getDescription());
                usernameField.setText(password.getUsername());
                urlField.setText(password.getUrl());
                passwordFieldHidden.setText(password.getPassword());
                passwordFieldVisible.setText(password.getPassword());

                // Refrescar validaciones y problemas de seguridad
                validateFields();
                checkPasswordIssues();

                // Mostrar notificación de éxito
                Notifier.showNotification(saveButton.getScene().getWindow(), getBundle().getString("password_updated_successfully"));
            } else {
                // Manejar el caso en que no se pueda recargar el objeto
                Notifier.showNotification(saveButton.getScene().getWindow(), getBundle().getString("error_loading_password"));
            }
        }
    }

    private void deletePassword() {
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle(getBundle().getString("delete_confirmation_title"));
        confirmationDialog.setHeaderText(getBundle().getString("delete_confirmation_header"));
        confirmationDialog.setContentText(getBundle().getString("delete_confirmation_message"));

        // Aplicar el tema actual al DialogPane
        DialogPane dialogPane = confirmationDialog.getDialogPane();
        dialogPane.getStylesheets().add(ThemeManager.getCurrentStylesheet());
        dialogPane.getStyleClass().add("delete-dialog");

        Optional<ButtonType> confirmationResult = confirmationDialog.showAndWait();
        if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
            passwordsController.deletePassword(password);
            ((Stage) deleteButton.getScene().getWindow()).close();
        }
    }

    private void checkPasswordIssues() {
        // Limpia solo los labels (deja el ImageView en su lugar si ya está cargado)
        securityStatusVbox.getChildren().clear();

        boolean hasIssues = false;

        // Cambiar la imagen según el estado
        String iconPath;
        if (password.isWeak()) {
            hasIssues = true;
        }
        if (password.isDuplicate()) {
            hasIssues = true;
        }
        if (password.isCompromised()) {
            hasIssues = true;
        }
        if (password.isUrlUnsafe()) {
            hasIssues = true;
        }

        String themeSuffix = ThemeManager.isDarkMode() ? "_dark_mode" : "";
        iconPath = hasIssues ? "/passworld/images/warning_icon" + themeSuffix + ".png" : "/passworld/images/protect_icon" + themeSuffix + ".png";
        Image icon = new Image(Objects.requireNonNull(getClass().getResource(iconPath)).toExternalForm());
        securityStatusImageView.setImage(icon);
        securityStatusVbox.getChildren().add(securityStatusImageView); // siempre primero

        // Agregar contenido textual
        if (hasIssues) {
            if (password.isWeak()) passwordStrengthLabel.setText("weak_password");
            if (password.isDuplicate()) passwordStrengthLabel.setText("duplicate_password");
            if (password.isCompromised()) passwordStrengthLabel.setText("compromised_password");
            if (password.isUrlUnsafe()) passwordStrengthLabel.setText("unsafe_url");

            if (password.isWeak()) addIssueLabel("weak_password");
            if (password.isDuplicate()) addIssueLabel("duplicate_password");
            if (password.isCompromised()) addIssueLabel("compromised_password");
            if (password.isUrlUnsafe()) addIssueLabel("unsafe_url");
        } else {
            Label noIssuesLabel = new Label(getBundle().getString("issue_passwords_button_ok_tooltip"));
            noIssuesLabel.setWrapText(true);
            noIssuesLabel.getStyleClass().add("noIssuesLabel");
            securityStatusVbox.getChildren().add(noIssuesLabel);
        }

        // **Nueva funcionalidad: Evaluar fortaleza de la contraseña**
        String currentPassword = passwordFieldVisible.getText();
        int strength = PasswordEvaluator.calculateStrength(currentPassword); // Calcula la fortaleza
        PasswordEvaluator.updatePasswordStrengthInfo(strength, passwordStrengthLabel, passwordStrengthProgressBar); // Actualiza la etiqueta y el ProgressBar

        securityStatusVbox.setVisible(true);
        securityStatusVbox.setManaged(true);
    }

    private void addIssueLabel(String issueKey) {
        Label issueLabel = new Label(getBundle().getString(issueKey));
        issueLabel.setAlignment(Pos.CENTER);         // centra el contenido
        issueLabel.setTextAlignment(TextAlignment.CENTER); // centra cada línea del texto
        issueLabel.setWrapText(true);
        securityStatusVbox.getChildren().add(issueLabel);
    }

    private static ResourceBundle getBundle() {
        return LanguageManager.getBundle();
    }
}
