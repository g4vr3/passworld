package passworld.utils;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import passworld.controller.MyPasswordsController;
import passworld.data.PasswordDTO;
import passworld.service.LanguageManager;
import passworld.service.SecurityFilterManager;

import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class DialogUtil {

    // Auxiliar para obtener el ResourceBundle dinámicamente
    private static ResourceBundle getBundle() {
        return LanguageManager.getBundle();
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
            saveIcon = new Image(DialogUtil.class.getResource("/passworld/images/save_icon_dark_mode.png").toExternalForm());
        }
        else {
            saveIcon = new Image(DialogUtil.class.getResource("/passworld/images/save_icon_white.png").toExternalForm());
        }
        ImageView saveIconView = new ImageView(saveIcon);
        saveIconView.getStyleClass().add("icon"); // Estilo css
        saveButton.setGraphic(saveIconView); // Establecer el icono en el botón de guardar

        // Agregar el icono al botón de cancelar
        Image cancelIcon = new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/cancel_icon.png")).toExternalForm());
        ImageView cancelIconView = new ImageView(cancelIcon);
        ThemeManager.applyThemeToImage(cancelIconView); // Aplicar el tema al icono
        cancelIconView.getStyleClass().add("icon");
        cancelButton.setGraphic(cancelIconView); // Establecer el icono en el botón de cancelar

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
        Label passwordLabel = new Label(getBundle().getString("password_label") + ":");
        TextField passwordField = new TextField();
        passwordField.setText(password);
        passwordField.setDisable(true); // Deshabilitar el campo de la contraseña

        // Establecer Tooltip para el campo de contraseña
        passwordField.setTooltip(new Tooltip(getBundle().getString("password_field_tooltip")));

        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Agregar los VBox con los campos y etiquetas al VBox principal
        vbox.getChildren().addAll(descriptionBox, usernameBox, urlBox, passwordBox);

        // Establecer el contenido del dialog
        dialog.getDialogPane().setContent(vbox);

        // Listener para la validación en tiempo real
        ChangeListener<String> fieldValidationListener = (_, _, _) -> {
            boolean isDescriptionValid = !(descriptionField.getText() == null || descriptionField.getText().trim().isEmpty());

            // Mostrar o quitar el mensaje de error en tiempo real
            if (!isDescriptionValid) {
                descriptionField.getStyleClass().add("error-border");
                mandatoryDescriptionLabel.setVisible(true);
                mandatoryDescriptionLabel.setManaged(true); // El mensaje se gestiona cuando es visible

                dialog.setHeight(380);
            } else {
                descriptionField.getStyleClass().remove("error-border");
                mandatoryDescriptionLabel.setVisible(false);
                mandatoryDescriptionLabel.setManaged(false); // El mensaje no se gestiona cuando está oculto

                dialog.setHeight(360);
            }

            // Deshabilitar el botón de guardar si el campo de descripción no es válido
            saveButton.setDisable(!isDescriptionValid);
        };

        // Agregar listeners a los campos de entrada
        descriptionField.textProperty().addListener(fieldValidationListener);

        // Realizar la validación inicial (debería mostrar el borde rojo y el mensaje de error si está vacío)
        fieldValidationListener.changed(null, null, descriptionField.getText());

        // Manejar la respuesta
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Obtener los valores de los campos
                String description = descriptionField.getText();
                String username = usernameField.getText();
                String url = urlField.getText();

                // Si los campos están vacíos, asignarles null
                if (username == null || username.trim().isEmpty()) {
                    username = null;
                }
                if (url == null || url.trim().isEmpty()) {
                    url = null;
                }

                // Devolver el DTO con los valores (con null si estaban vacíos)
                return new PasswordDTO(description, username, url, password);
            }
            return null;
        });

        return dialog.showAndWait();
    }


    // Mostrar dialog para desbloquear base de datos
    public static Optional<String> showUnlockVaultDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(ThemeManager.getCurrentStylesheet())
        );
        dialog.setTitle("passworld - " + getBundle().getString("dialog_title_unlock_vault"));

        // Configurar botones
        ButtonType unlockButtonType = new ButtonType(getBundle().getString("unlock_button"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(getBundle().getString("cancel_button"), ButtonBar.ButtonData.CANCEL_CLOSE);

        // Agregar los botones al cuadro de diálogo
        dialog.getDialogPane().getButtonTypes().addAll(unlockButtonType, cancelButtonType);

        // Estilo para el botón de desbloquear
        Button unlockButton = (Button) dialog.getDialogPane().lookupButton(unlockButtonType);
        unlockButton.getStyleClass().add("primary");

        // Estilo para el botón de Cancelar
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("secondary");

        // Agregar el icono al botón de desbloquear
        String themeSuffix = ThemeManager.isDarkMode() ? "_dark_mode" : "";
        Image unlockIcon = new Image(DialogUtil.class.getResource("/passworld/images/unlock_icon" + themeSuffix + ".png").toExternalForm());
        ImageView unlockIconView = new ImageView(unlockIcon);
        unlockIconView.getStyleClass().add("icon"); // Estilo CSS
        unlockButton.setGraphic(unlockIconView);

        // Agregar el icono al botón de cancelar
        Image cancelIcon = new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/cancel_icon.png")).toExternalForm());
        ImageView cancelIconView = new ImageView(cancelIcon);
        ThemeManager.applyThemeToImage(cancelIconView); // Aplicar el tema al icono
        cancelIconView.getStyleClass().add("icon");
        cancelButton.setGraphic(cancelIconView);

        // Establecer Tooltips para los botones
        unlockButton.setTooltip(new Tooltip(getBundle().getString("unlock_button_tooltip")));
        cancelButton.setTooltip(new Tooltip(getBundle().getString("cancel_button_tooltip")));

        // Botón de desbloquear por defecto
        unlockButton.setDefaultButton(true);

        // Crear contenedor principal VBox
        VBox vbox = new VBox(10); // Espaciado entre cada par de elementos

        // Añadir un texto con la información del desbloqueo
        Label unlockInfoLabel = new Label(getBundle().getString("unlock_vault_info"));
        vbox.getChildren().add(unlockInfoLabel);

        // Crear un campo de texto para la clave maestra
        VBox masterKeyBox = new VBox(5);  // Espacio entre Label y PasswordField
        Label masterKeyLabel = new Label(getBundle().getString("master_key_label") + ":");
        PasswordField masterKeyField = new PasswordField();
        masterKeyField.setPromptText(getBundle().getString("master_key_field"));

        // Establecer Tooltip para el campo de clave maestra
        masterKeyField.setTooltip(new Tooltip(getBundle().getString("master_key_field_tooltip")));

        // Focus inicial en el campo de clave maestra
        Platform.runLater(masterKeyField::requestFocus);

        masterKeyBox.getChildren().addAll(masterKeyLabel, masterKeyField);

        // Añadir el campo de clave maestra al VBox principal
        vbox.getChildren().add(masterKeyBox);

        // Establecer el contenido del dialog
        dialog.getDialogPane().setContent(vbox);

        // Manejar la respuesta
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == unlockButtonType) {
                return masterKeyField.getText();
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
