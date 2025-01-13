package passworld.utils;

import javafx.application.Platform;
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

import java.util.Optional;
import java.util.ResourceBundle;

public class DialogUtil {

    // Obtener el ResourceBundle para manejar los textos en el idioma adecuado
    private static ResourceBundle bundle = LanguageManager.getBundle();

    // Botones genéricos
    private static final ButtonType saveButtonType = new ButtonType(bundle.getString("save_button"), ButtonBar.ButtonData.CANCEL_CLOSE);
    private static final ButtonType cancelButtonType = new ButtonType(bundle.getString("cancel_button"), ButtonBar.ButtonData.CANCEL_CLOSE);

    public static Optional<PasswordDTO> showPasswordCreationDialog(String password) {
        Dialog<PasswordDTO> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                DialogUtil.class.getResource("/passworld/styles/styles.css").toExternalForm()
        );
        dialog.setTitle("passworld - " + bundle.getString("dialog_title_save_password"));

        // Configurar botones
        ButtonType saveButtonType = new ButtonType(bundle.getString("save_button"), ButtonBar.ButtonData.OK_DONE);

        // Agregar los botones al cuadro de diálogo
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Estilo para el botón de Guardar
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setTooltip(new Tooltip(bundle.getString("save_button_tooltip")));
        saveButton.getStyleClass().add("primary");

        // Estilo para el botón de Cancelar
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setTooltip(new Tooltip(bundle.getString("cancel_button_tooltip")));
        cancelButton.getStyleClass().add("secondary");

        // Agregar el icono al botón de guardar
        Image saveIcon = new Image(DialogUtil.class.getResource("/passworld/images/save_icon_white.png").toExternalForm());
        ImageView saveIconView = new ImageView(saveIcon);
        saveIconView.getStyleClass().add("icon"); // Estilo css
        saveButton.setGraphic(saveIconView); // Establecer el icono en el botón de guardar

        // Agregar el icono al botón de cancelar
        Image cancelIcon = new Image(DialogUtil.class.getResource("/passworld/images/cancel_icon.png").toExternalForm());
        ImageView cancelIconView = new ImageView(cancelIcon);
        cancelIconView.getStyleClass().add("icon");
        cancelButton.setGraphic(cancelIconView); // Establecer el icono en el botón de cancelar

        // Establecer Tooltips para los botones
        saveButton.setTooltip(new Tooltip(bundle.getString("save_button_tooltip")));
        cancelButton.setTooltip(new Tooltip(bundle.getString("cancel_button_tooltip")));

        // Botón de guardar por defecto
        saveButton.setDefaultButton(true);

        // Crear contenedor principal VBox
        VBox vbox = new VBox(10); // Espaciado entre cada par de elementos

        // Crear etiquetas y campos de texto con espaciado entre ellos
        VBox descriptionBox = new VBox(5);  // Espacio entre Label y TextField
        Label descriptionLabel = new Label(bundle.getString("description_label") + ":");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText(bundle.getString("description_prompt"));

        // Establecer Tooltip para el campo de descripción
        descriptionField.setTooltip(new Tooltip(bundle.getString("description_field_tooltip")));

        descriptionBox.getChildren().addAll(descriptionLabel, descriptionField);

        // Focus inicial en el field de descripción
        Platform.runLater(descriptionField::requestFocus);

        VBox urlBox = new VBox(5);  // Espacio de 5px entre Label y TextField
        Label urlLabel = new Label(bundle.getString("url_label") + ":");
        TextField urlField = new TextField();
        urlField.setPromptText(bundle.getString("url_prompt"));

        // Establecer Tooltip para el campo de URL
        urlField.setTooltip(new Tooltip(bundle.getString("url_field_tooltip")));

        urlBox.getChildren().addAll(urlLabel, urlField);

        VBox passwordBox = new VBox(5);  // Espacio entre Label y TextField
        Label passwordLabel = new Label(bundle.getString("password_label2") + ":");
        TextField passwordField = new TextField();
        passwordField.setText(password);
        passwordField.setDisable(true); // Deshabilitar el campo de la contraseña

        // Establecer Tooltip para el campo de contraseña
        passwordField.setTooltip(new Tooltip(bundle.getString("password_field_tooltip")));

        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Agregar los VBox con los campos y etiquetas al VBox principal con espaciado de 10px
        vbox.getChildren().addAll(descriptionBox, urlBox, passwordBox);

        // Establecer el contenido del dialog
        dialog.getDialogPane().setContent(vbox);

        // Manejar la respuesta
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new PasswordDTO(descriptionField.getText(), urlField.getText(), password);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // Mostrar dialog para desbloquear base de datos
    public static Optional<String> showUnlockVaultDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                DialogUtil.class.getResource("/passworld/styles/styles.css").toExternalForm()
        );
        dialog.setTitle("passworld - " + bundle.getString("dialog_title_unlock_vault"));

        // Configurar botones
        ButtonType unlockButtonType = new ButtonType(bundle.getString("unlock_button"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(bundle.getString("cancel_button"), ButtonBar.ButtonData.CANCEL_CLOSE);

        // Agregar los botones al cuadro de diálogo
        dialog.getDialogPane().getButtonTypes().addAll(unlockButtonType, cancelButtonType);

        // Estilo para el botón de desbloquear
        Button unlockButton = (Button) dialog.getDialogPane().lookupButton(unlockButtonType);
        unlockButton.getStyleClass().add("primary");

        // Estilo para el botón de Cancelar
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("secondary");

        // Agregar el icono al botón de desbloquear
        Image unlockIcon = new Image(DialogUtil.class.getResource("/passworld/images/unlock_icon_white.png").toExternalForm());
        ImageView unlockIconView = new ImageView(unlockIcon);
        unlockIconView.getStyleClass().add("icon"); // Estilo CSS
        unlockButton.setGraphic(unlockIconView);

        // Agregar el icono al botón de cancelar
        Image cancelIcon = new Image(DialogUtil.class.getResource("/passworld/images/cancel_icon.png").toExternalForm());
        ImageView cancelIconView = new ImageView(cancelIcon);
        cancelIconView.getStyleClass().add("icon");
        cancelButton.setGraphic(cancelIconView);

        // Establecer Tooltips para los botones
        unlockButton.setTooltip(new Tooltip(bundle.getString("unlock_button_tooltip")));
        cancelButton.setTooltip(new Tooltip(bundle.getString("cancel_button_tooltip")));

        // Botón de desbloquear por defecto
        unlockButton.setDefaultButton(true);

        // Crear contenedor principal VBox
        VBox vbox = new VBox(10); // Espaciado entre cada par de elementos

        // Añadir un texto con la información del desbloqueo
        Label unlockInfoLabel = new Label(bundle.getString("unlock_vault_info"));
        vbox.getChildren().add(unlockInfoLabel);

        // Crear un campo de texto para la clave maestra
        VBox masterKeyBox = new VBox(5);  // Espacio entre Label y PasswordField
        Label masterKeyLabel = new Label(bundle.getString("master_key_label") + ":");
        PasswordField masterKeyField = new PasswordField();
        masterKeyField.setPromptText(bundle.getString("master_key_field"));

        // Establecer Tooltip para el campo de clave maestra
        masterKeyField.setTooltip(new Tooltip(bundle.getString("master_key_field_tooltip")));

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

    public static void showPasswordInfoDialog(PasswordDTO password, MyPasswordsController passwordsController) {
        Dialog<PasswordDTO> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                DialogUtil.class.getResource("/passworld/styles/styles.css").toExternalForm()
        );
        dialog.setTitle("passworld");

        // Configurar botones
        ButtonType deleteButtonType = new ButtonType(bundle.getString("delete_button"), ButtonBar.ButtonData.NO);

        // Agregar los botones al cuadro de diálogo
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, deleteButtonType);

        // Estilo para el botón de Guardar
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.getStyleClass().add("primary");

        // Estilo para el botón de Eliminar
        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.getStyleClass().add("secondary");

        // Agregar el icono al botón de guardar
        Image saveIcon = new Image(DialogUtil.class.getResource("/passworld/images/save_icon_white.png").toExternalForm());
        ImageView saveIconView = new ImageView(saveIcon);
        saveIconView.setFitHeight(15);
        saveIconView.setFitWidth(15);
        saveButton.setGraphic(saveIconView);

        // Agregar el icono al botón de eliminar
        Image deleteIcon = new Image(DialogUtil.class.getResource("/passworld/images/trash_icon.png").toExternalForm());
        ImageView deleteIconView = new ImageView(deleteIcon);
        deleteIconView.setFitHeight(15);
        deleteIconView.setFitWidth(15);
        deleteButton.setGraphic(deleteIconView);

        // Crear contenedor principal VBox
        VBox vbox = new VBox(10); // Espaciado entre los campos

        // Crear campos para Descripción
        VBox descriptionBox = new VBox(5);
        Label descriptionLabel = new Label(bundle.getString("description_label") + ":");
        TextField descriptionField = new TextField(password.getDescription());
        descriptionField.setPromptText(bundle.getString("description_prompt"));
        descriptionBox.getChildren().addAll(descriptionLabel, descriptionField);

        // Crear campos para URL
        VBox urlBox = new VBox(5);
        Label urlLabel = new Label(bundle.getString("url_label") + ":");
        TextField urlField = new TextField(password.getUrl());
        urlField.setPromptText(bundle.getString("url_prompt"));
        urlBox.getChildren().addAll(urlLabel, urlField);

        // Crear campos para Contraseña
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label(bundle.getString("password_label2") + ":");

        // Crear contenedor para alternar entre PasswordField y TextField
        StackPane passwordFieldContainer = new StackPane();
        passwordFieldContainer.setPrefHeight(30);

        PasswordField passwordFieldHidden = new PasswordField();
        passwordFieldHidden.setText(password.getPassword());
        passwordFieldHidden.setPrefWidth(300);

        TextField passwordFieldVisible = new TextField(password.getPassword());
        passwordFieldVisible.setVisible(false);

        // Botón de copiar contraseña
        Image copyIcon = new Image(DialogUtil.class.getResource("/passworld/images/copy_icon.png").toExternalForm());
        ImageView copyIconView = new ImageView(copyIcon);
        copyIconView.setFitHeight(15);
        copyIconView.setFitWidth(15);

        Button copyButton = new Button();
        copyButton.setGraphic(copyIconView);
        copyButton.getStyleClass().add("icon-button");
        copyButton.setTooltip(new Tooltip(bundle.getString("toolTip_copyToClipboard")));
        copyButton.setVisible(false); // Inicialmente no visible

        // Acción del botón de copiar
        copyButton.setOnAction(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(passwordFieldVisible.getText());
            clipboard.setContent(content);

            // Mostrar notificación de copia
            Notifier.showNotification(copyButton.getScene().getWindow(), bundle.getString("toolTip_textCopiedToClipboard"));
        });

        // Mostrar contraseña y el botón de copiar al hacer clic en la contraseña oculta
        passwordFieldHidden.setOnMouseClicked(event -> {
            passwordFieldVisible.setText(passwordFieldHidden.getText());
            passwordFieldVisible.setVisible(true);
            passwordFieldHidden.setVisible(false);
            copyButton.setVisible(true); // Hacer visible el botón de copiar
            passwordFieldVisible.requestFocus();
        });

        // Añadir los campos al contenedor de contraseña
        passwordFieldContainer.getChildren().addAll(passwordFieldHidden, passwordFieldVisible, copyButton);
        StackPane.setAlignment(copyButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(copyButton, new Insets(0, 5, 0, 0)); // Margen derecho para el botón

        passwordBox.getChildren().addAll(passwordLabel, passwordFieldContainer);

        // Agregar los campos al VBox principal
        vbox.getChildren().addAll(descriptionBox, urlBox, passwordBox);

        // Establecer el contenido del cuadro de diálogo
        dialog.getDialogPane().setContent(vbox);

        // Manejar la respuesta del diálogo
        dialog.setResultConverter(dialogButton -> {
            // Si se pulsa el botón de eliminar
            if (dialogButton == deleteButtonType) {
                // Alerta para confirmación de eliminación
                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle(bundle.getString("delete_confirmation_title"));
                confirmationDialog.setHeaderText(bundle.getString("delete_confirmation_header"));
                confirmationDialog.setContentText(bundle.getString("delete_confirmation_message"));

                Optional<ButtonType> confirmationResult = confirmationDialog.showAndWait(); // Recoger selección

                // Si se confirma, se elimina
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    passwordsController.deletePassword(password);
                    dialog.close();  // Aquí cerramos el diálogo solo si se confirma
                }
                return null;
            } else if (dialogButton == saveButtonType) {
                // Verifica si se ha modificado algún dato
                if (!descriptionField.getText().equals(password.getDescription()) ||
                        !urlField.getText().equals(password.getUrl()) ||
                        !passwordFieldVisible.getText().equals(password.getPassword())) {
                    // Se actualiza la contraseña
                    passwordsController.updatePassword(password, descriptionField.getText(), urlField.getText(), passwordFieldVisible.getText());
                }
                dialog.close();  // Cierra el diálogo solo cuando se guarda
            }
            return null;
        });
        dialog.showAndWait();
    }
}
