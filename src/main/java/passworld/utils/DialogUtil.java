package passworld.utils;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import passworld.data.PasswordDTO;

import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class DialogUtil {

    // Auxiliar para obtener el ResourceBundle dinámicamente
    private static ResourceBundle getBundle() {
        return LanguageUtil.getBundle();
    }

    public static Optional<PasswordDTO> showPasswordCreationDialog(String password) {
        Dialog<PasswordDTO> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(ThemeManager.getCurrentStylesheet())
        );
        dialog.setTitle("passworld - " + getBundle().getString("dialog_title_save_password"));
        dialog.setWidth(400);

        // Crear los botones con los textos actualizados según el idioma actual
        ButtonType saveButtonType = new ButtonType(getBundle().getString("save_button"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(getBundle().getString("cancel_button"), ButtonBar.ButtonData.CANCEL_CLOSE);

        // Agregar los botones al cuadro de diálogo
        dialog.getDialogPane().getButtonTypes().addAll(cancelButtonType, saveButtonType);

        // Estilo para el botón de Guardar
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setTooltip(new Tooltip(getBundle().getString("save_button_tooltip")));
        saveButton.getStyleClass().add("primary");

        // Estilo para el botón de Cancelar
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setTooltip(new Tooltip(getBundle().getString("cancel_button_tooltip")));
        cancelButton.getStyleClass().add("secondary");

        // Agregar el icono al botón de guardar
        Image saveIcon;
        if (ThemeManager.isDarkMode()) {
            saveIcon = new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/save_icon_dark_mode.png")).toExternalForm());
        } else {
            saveIcon = new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/save_icon_white.png")).toExternalForm());
        }
        ImageView saveIconView = new ImageView(saveIcon);
        saveIconView.getStyleClass().add("icon"); // Estilo css
        saveButton.setGraphic(saveIconView); // Establecer el icono en el botón de guardar

        // Agregar el icono al botón de cancelar
        setCancelButton(cancelButton);

        // Establecer Tooltips para los botones
        saveButton.setTooltip(new Tooltip(getBundle().getString("save_button_tooltip")));
        cancelButton.setTooltip(new Tooltip(getBundle().getString("cancel_button_tooltip")));

        // Botón de guardar por defecto
        saveButton.setDefaultButton(true);

        // Crear contenedor principal VBox
        VBox vbox = new VBox(10); // Espaciado entre cada par de elementos

        // Crear etiquetas y campos de texto
        VBox descriptionBox = new VBox(5);
        Label descriptionLabel = new Label(getBundle().getString("description_label") + ": *");
        TextField descriptionField = new TextField();
        descriptionField.setTooltip(new Tooltip(getBundle().getString("description_field_tooltip")));
        descriptionField.setPromptText(getBundle().getString("description_prompt"));

        // Label para mostrar mensaje de error debajo de la descripción
        Label mandatoryDescriptionLabel = new Label(getBundle().getString("mandatory_description_label"));
        mandatoryDescriptionLabel.getStyleClass().add("mandatoryFieldsLabel");
        mandatoryDescriptionLabel.setVisible(false);  // Inicialmente no visible
        mandatoryDescriptionLabel.setManaged(false); // No gestionado cuando no se muestra

        descriptionBox.getChildren().addAll(descriptionLabel, descriptionField, mandatoryDescriptionLabel);

        // Focus inicial en el field de descripción
        Platform.runLater(descriptionField::requestFocus);

        VBox usernameBox = new VBox(5);
        Label usernameLabel = new Label(getBundle().getString("username_label") + ":");
        TextField usernameField = new TextField();
        usernameField.setPromptText(getBundle().getString("username_prompt"));

        // Establecer Tooltip para el campo de username
        usernameField.setTooltip(new Tooltip(getBundle().getString("username_field_tooltip")));

        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        VBox urlBox = new VBox(5);
        Label urlLabel = new Label(getBundle().getString("url_label") + ":");
        TextField urlField = new TextField();
        urlField.setPromptText(getBundle().getString("url_prompt"));

        // Establecer Tooltip para el campo de URL
        urlField.setTooltip(new Tooltip(getBundle().getString("url_field_tooltip")));

        urlBox.getChildren().addAll(urlLabel, urlField);

        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label(getBundle().getString("password_label") + ": *");

// Crear un StackPane para el TextField y el botón
        StackPane passwordFieldStack = new StackPane();
        passwordFieldStack.setAlignment(Pos.CENTER_RIGHT);
        passwordFieldStack.setPrefWidth(300); // Puedes ajustar este valor
        passwordFieldStack.setMaxWidth(Double.MAX_VALUE);

// Crear el campo de contraseña
        TextField passwordField = new TextField();
        passwordField.setText(password);
        passwordField.setPromptText(getBundle().getString("password_prompt"));
        passwordField.setTooltip(new Tooltip(getBundle().getString("password_field_tooltip")));
        passwordField.getStyleClass().add("password-field-with-button");
        passwordField.setPadding(new Insets(0, 30, 0, 0)); // Deja espacio para el botón
        passwordField.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(passwordField, Pos.CENTER_LEFT);

// Crear el botón de regenerar
        Button regeneratePasswordButton = new Button();
        regeneratePasswordButton.setFocusTraversable(false);
        regeneratePasswordButton.getStyleClass().add("icon-button");
        regeneratePasswordButton.setPrefSize(20, 20);
        regeneratePasswordButton.setMinSize(20, 20);

// Icono del botón
        Image reloadIcon = new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/reload_icon.png")).toExternalForm());
        ImageView reloadIconView = new ImageView(reloadIcon);
        reloadIconView.getStyleClass().add("icon");
        reloadIconView.setFitHeight(16);
        reloadIconView.setFitWidth(16);
        regeneratePasswordButton.setGraphic(reloadIconView);

// Acción del botón
        regeneratePasswordButton.setOnAction(_ -> {
            String newPassword = PasswordGenerator.generateDefaultPassword();
            passwordField.setText(newPassword);
        });

// Posición del botón
        StackPane.setAlignment(regeneratePasswordButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(regeneratePasswordButton, new Insets(0, 5, 0, 0));

// Añadir al StackPane
        passwordFieldStack.getChildren().setAll(passwordField, regeneratePasswordButton);

// Mensaje de error para la contraseña
        Label mandatoryPasswordLabel = new Label(getBundle().getString("mandatory_password_label"));
        mandatoryPasswordLabel.getStyleClass().add("mandatoryFieldsLabel");
        mandatoryPasswordLabel.setVisible(false);
        mandatoryPasswordLabel.setManaged(false);

// Añadir al contenedor
        passwordBox.getChildren().addAll(passwordLabel, passwordFieldStack, mandatoryPasswordLabel);

        // Agregar los VBox con los campos y etiquetas al VBox principal
        vbox.getChildren().addAll(descriptionBox, usernameBox, urlBox, passwordBox);

        // Establecer el contenido del diálogo
        dialog.getDialogPane().setContent(vbox);

        // Establecer icono de la ventana del diálogo
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/app_icon.png")).toExternalForm()));

        Platform.runLater(stage::sizeToScene);

        // Listener para la validación en tiempo real
        ChangeListener<String> fieldValidationListener = (_, _, _) -> {
            boolean isDescriptionValid = !(descriptionField.getText() == null || descriptionField.getText().trim().isEmpty());
            boolean isPasswordValid = !(passwordField.getText() == null || passwordField.getText().trim().isEmpty());

            // Validación de descripción
            isFieldValid(dialog, descriptionField, mandatoryDescriptionLabel, isDescriptionValid);

            // Validación de contraseña
            isFieldValid(dialog, passwordField, mandatoryPasswordLabel, isPasswordValid);

            // Deshabilitar el botón de guardar si algún campo obligatorio no es válido
            saveButton.setDisable(!isDescriptionValid || !isPasswordValid);
        };

        // Agregar listeners a los campos de entrada
        descriptionField.textProperty().addListener(fieldValidationListener);
        passwordField.textProperty().addListener(fieldValidationListener);

        // Realizar la validación inicial
        fieldValidationListener.changed(null, null, descriptionField.getText());
        fieldValidationListener.changed(null, null, passwordField.getText());

        // Manejar la respuesta
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Obtener los valores de los campos
                String description = descriptionField.getText();
                String username = usernameField.getText();
                String url = urlField.getText();
                String passwordText = passwordField.getText(); // Obtener el texto del field de contraseña por si ha habido cambios

                // Si los campos están vacíos, asignarles null
                if (username == null || username.trim().isEmpty()) {
                    username = null;
                }
                if (url == null || url.trim().isEmpty()) {
                    url = null;
                }

                // Devolver el DTO con los valores (con null si estaban vacíos)
                return new PasswordDTO(description, username, url, passwordText);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private static void isFieldValid(Dialog<PasswordDTO> dialog, TextField descriptionField, Label mandatoryDescriptionLabel, boolean isDescriptionValid) {
        if (!isDescriptionValid) {
            descriptionField.getStyleClass().add("error-border");
            mandatoryDescriptionLabel.setVisible(true);
            mandatoryDescriptionLabel.setManaged(true);
            dialog.setHeight(400);
        } else {
            descriptionField.getStyleClass().remove("error-border");
            mandatoryDescriptionLabel.setVisible(false);
            mandatoryDescriptionLabel.setManaged(false);
        }
    }

    private static void setCancelButton(Button cancelButton) {
        // Agregar el icono al botón de cancelar
        Image cancelIcon = new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/cancel_icon.png")).toExternalForm());
        ImageView cancelIconView = new ImageView(cancelIcon);
        ThemeManager.applyThemeToImage(cancelIconView); // Aplicar el tema al icono
        cancelIconView.getStyleClass().add("icon");
        cancelButton.setGraphic(cancelIconView);
    }

    // Mostrar un cuadro de diálogo de confirmación y devolver el resultado
    public static boolean showConfirmationDialog(String titleKey, String headerKey, String messageKey) {
        ResourceBundle bundle = LanguageUtil.getBundle();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(bundle.getString(titleKey));
        alert.setHeaderText(bundle.getString(headerKey));
        alert.setContentText(bundle.getString(messageKey));

        // Icono del dialogo
        ImageView infoIcon = new ImageView(new Image(
                Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/question_icon.png")).toExternalForm()
        ));
        infoIcon.setFitWidth(40);
        infoIcon.setFitHeight(40);
        alert.setGraphic(infoIcon);

        // Establecer tema y clase al cuadro de diálogo
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(ThemeManager.getCurrentStylesheet())
        );
        dialogPane.getStyleClass().add("confirmation-dialog");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Mostrar un cuadro de diálogo de información de la aplicación
    public static void showAboutDialog() {
        ResourceBundle bundle = LanguageManager.getBundle();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Passworld");
        alert.setHeaderText(null);

        // Icono del dialogo
        ImageView infoIcon = new ImageView(new Image(
                Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/info_icon.png")).toExternalForm()
        ));
        infoIcon.setFitWidth(40);
        infoIcon.setFitHeight(40);
        alert.setGraphic(infoIcon);

        String versionText = "Passworld 1.0.0";
        String iconsText = bundle.getString("about_icons");
        String icons8Text = "icons8";
        String icons8Url = "https://icons8.com/";

        Label versionLabel = new Label(versionText);
        Label iconsLabel = new Label(iconsText);
        Hyperlink icons8Link = new Hyperlink(icons8Text);
        icons8Link.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(icons8Url));
            } catch (Exception ex) {
                LogUtils.LOGGER.warning("Error opening URL: " + icons8Url);
            }
        });
        icons8Link.setStyle("-fx-text-fill: #222; -fx-underline: true;");

        HBox iconsBox = new HBox(iconsLabel, icons8Link);
        iconsBox.setSpacing(2);
        iconsBox.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10, versionLabel, iconsBox);

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(220);
        ThemeManager.applyCurrentTheme(alert.getDialogPane().getScene());

        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.getStyleClass().add("primary");
        }

        alert.showAndWait();
    }
}
