package passworld.controller;

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
import passworld.service.SecurityFilterManager;
import passworld.utils.Notifier;

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
    private Button copyButton, deleteButton, saveButton;

    @FXML
    private Button backButton;

    @FXML
    private VBox securityStatusVbox;

    @FXML
    private ImageView securityStatusImageView;

    private PasswordDTO password;
    private MyPasswordsController passwordsController;

    public static void showView(PasswordDTO password, MyPasswordsController passwordsController) {
        try {
            FXMLLoader loader = new FXMLLoader(PasswordInfoController.class.getResource("/passworld/password-info-view.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load(), 600, 450);
            scene.getStylesheets().add(PasswordInfoController.class.getResource("/passworld/styles/styles.css").toExternalForm());
            stage.getIcons().add(new Image(PasswordInfoController.class.getResourceAsStream("/passworld/images/app_icon.png")));
            stage.setTitle("passworld - " + password.getDescription());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            PasswordInfoController controller = loader.getController();
            controller.setData(password, passwordsController);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
        saveButton.setVisible(false); // Ocultar por defecto

        // Listener común para validar cambios en campos
        ChangeListener<String> fieldChangeListener = (observable, oldValue, newValue) -> validateFields();

        descriptionField.textProperty().addListener(fieldChangeListener);
        usernameField.textProperty().addListener(fieldChangeListener);
        urlField.textProperty().addListener(fieldChangeListener);

        passwordFieldVisible.textProperty().addListener((observable, oldVal, newVal) -> {
            passwordFieldHidden.setText(newVal); // Sincroniza campos
            validateFields();
            checkPasswordIssues(); // Analiza problemas al cambiar la contraseña
        });

        passwordFieldHidden.textProperty().addListener((observable, oldVal, newVal) -> {
            passwordFieldVisible.setText(newVal); // Sincroniza campos
            validateFields();
            checkPasswordIssues(); // Analiza problemas al cambiar la contraseña
        });

        // Copiar al portapapeles
        copyButton.setOnAction(event -> copyPasswordToClipboard());

        // Mostrar/ocultar contraseña
        passwordFieldHidden.setOnMouseClicked(event -> togglePasswordVisibility(true));
        passwordFieldVisible.setOnMouseClicked(event -> togglePasswordVisibility(false));

        // Guardar
        saveButton.setOnAction(event -> {
            try {
                savePassword();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        // Eliminar
        deleteButton.setOnAction(event -> deletePassword());

        // Configurar el botón de volver
        setBackButton();

        // Texto de la interfaz
        setUIText();
    }

    private void setBackButton() {
        // Configurar el icono y el estilo del botón de volver
        Image ltIcon = new Image(getClass().getResource("/passworld/images/lt_icon.png").toExternalForm());
        ImageView ltImageView = new ImageView(ltIcon);
        ltImageView.getStyleClass().add("icon");
        backButton.setGraphic(ltImageView);

        backButton.getStyleClass().add("icon-button");

        // Configurar la acción del botón de volver
        backButton.setOnAction(event -> {
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
        copyButton.setVisible(showPassword);
    }

    private void copyPasswordToClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(passwordFieldVisible.getText());
        clipboard.setContent(content);

        Notifier.showNotification(copyButton.getScene().getWindow(), getBundle().getString("toolTip_textCopiedToClipboard"));
    }

    private void savePassword() throws SQLException {
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
            PasswordDTO updatedPassword = PasswordManager.getPasswordById(password.getId());
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

        iconPath = hasIssues ? "/passworld/images/warning_icon.png" : "/passworld/images/protect_icon.png";
        Image icon = new Image(getClass().getResource(iconPath).toExternalForm());
        securityStatusImageView.setImage(icon);
        securityStatusVbox.getChildren().add(securityStatusImageView); // siempre primero

        // Agregar contenido textual
        if (hasIssues) {
            if (password.isWeak()) addIssueLabel("weak_password");
            if (password.isDuplicate()) addIssueLabel("duplicate_password");
            if (password.isCompromised()) addIssueLabel("compromised_password");
            if (password.isUrlUnsafe()) addIssueLabel("unsafe_url");
        } else {
            Label noIssuesLabel = new Label(getBundle().getString("issue_passwords_button_ok_tooltip"));
            noIssuesLabel.getStyleClass().add("noIssuesLabel");
            securityStatusVbox.getChildren().add(noIssuesLabel);
        }

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
