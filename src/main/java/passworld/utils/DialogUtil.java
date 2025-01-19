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

import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class DialogUtil {

    // Auxiliar para obtener el ResourceBundle dinámicamente
    private static ResourceBundle getBundle() {
        return LanguageManager.getBundle();
    }

    // Botones genéricos
    private static final ButtonType saveButtonType = new ButtonType(getBundle().getString("save_button"), ButtonBar.ButtonData.CANCEL_CLOSE);
    private static final ButtonType cancelButtonType = new ButtonType(getBundle().getString("cancel_button"), ButtonBar.ButtonData.CANCEL_CLOSE);

    public static Optional<PasswordDTO> showPasswordCreationDialog(String password) {
        Dialog<PasswordDTO> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(DialogUtil.class.getResource("/passworld/styles/styles.css")).toExternalForm()
        );
        dialog.setTitle("passworld - " + getBundle().getString("dialog_title_save_password"));
        dialog.setWidth(400);

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
        Image saveIcon = new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/save_icon_white.png")).toExternalForm());
        ImageView saveIconView = new ImageView(saveIcon);
        saveIconView.getStyleClass().add("icon"); // Estilo css
        saveButton.setGraphic(saveIconView); // Establecer el icono en el botón de guardar

        // Agregar el icono al botón de cancelar
        Image cancelIcon = new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/cancel_icon.png")).toExternalForm());
        ImageView cancelIconView = new ImageView(cancelIcon);
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
        Label passwordLabel = new Label(getBundle().getString("password_label2") + ":");
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
        ChangeListener<String> fieldValidationListener = (observable, oldValue, newValue) -> {
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
                Objects.requireNonNull(DialogUtil.class.getResource("/passworld/styles/styles.css")).toExternalForm()
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
        Image unlockIcon = new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/unlock_icon_white.png")).toExternalForm());
        ImageView unlockIconView = new ImageView(unlockIcon);
        unlockIconView.getStyleClass().add("icon"); // Estilo CSS
        unlockButton.setGraphic(unlockIconView);

        // Agregar el icono al botón de cancelar
        Image cancelIcon = new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/cancel_icon.png")).toExternalForm());
        ImageView cancelIconView = new ImageView(cancelIcon);
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

    // Mostrar información de una contraseña y permitir operaciones
    public static void showPasswordInfoDialog(PasswordDTO password, MyPasswordsController passwordsController) {
        Dialog<PasswordDTO> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(DialogUtil.class.getResource("/passworld/styles/styles.css")).toExternalForm()
        );
        dialog.setTitle("passworld");
        dialog.setHeight(360);

        // Añadir un ButtonType oculto para permitir que el diálogo se cierre
        ButtonType hiddenButtonType = new ButtonType("");
        dialog.getDialogPane().getButtonTypes().add(hiddenButtonType);
        Button hide = (Button) dialog.getDialogPane().lookupButton(hiddenButtonType);
        hide.getStyleClass().add("hidden");

        // Label para mostrar mensaje de error cuando hay varios campos obligatorios vacíos
        Label mandatoryFieldsLabel = new Label(getBundle().getString("mandatory_fields_label"));
        mandatoryFieldsLabel.getStyleClass().add("mandatoryFieldsLabel");
        mandatoryFieldsLabel.setVisible(false);  // Inicialmente no visible
        mandatoryFieldsLabel.setManaged(false); // No gestionado cuando no se muestra

        // Label para mostrar mensaje de error debajo de la descripción
        Label mandatoryDescriptionLabel = new Label(getBundle().getString("mandatory_description_label"));
        mandatoryDescriptionLabel.getStyleClass().add("mandatoryFieldsLabel");
        mandatoryDescriptionLabel.setVisible(false);  // Inicialmente no visible
        mandatoryDescriptionLabel.setManaged(false); // No gestionado cuando no se muestra

        // Crear contenedor principal VBox
        VBox vbox = new VBox(10);
        VBox.setVgrow(vbox, Priority.ALWAYS);  // Hacer que el VBox principal crezca para acomodar el contenido

        // Crear campos para Descripción
        VBox descriptionBox = new VBox(5);
        Label descriptionLabel = new Label(getBundle().getString("description_label") + ": *");
        TextField descriptionField = new TextField(password.getDescription());
        descriptionField.setPromptText(getBundle().getString("description_prompt"));
        descriptionBox.getChildren().addAll(descriptionLabel, descriptionField, mandatoryDescriptionLabel);

        // Crear campos para Username
        VBox usernameBox = new VBox(5);
        Label usernameLabel = new Label(getBundle().getString("username_label") + ":");
        TextField usernameField = new TextField(password.getUsername());
        usernameField.setPromptText(getBundle().getString("username_prompt"));
        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Crear campos para URL
        VBox urlBox = new VBox(5);
        Label urlLabel = new Label(getBundle().getString("url_label") + ":");
        TextField urlField = new TextField(password.getUrl());
        urlField.setPromptText(getBundle().getString("url_prompt"));
        urlBox.getChildren().addAll(urlLabel, urlField);

        // Crear campos para Contraseña
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label(getBundle().getString("password_label2") + ": *");

        // Crear contenedor para alternar entre PasswordField y TextField
        StackPane passwordFieldContainer = new StackPane();
        passwordFieldContainer.setPrefHeight(30);

        PasswordField passwordFieldHidden = new PasswordField();
        passwordFieldHidden.setText(password.getPassword());
        passwordFieldHidden.setPrefWidth(300);

        TextField passwordFieldVisible = new TextField(password.getPassword());
        passwordFieldVisible.setVisible(false);

        // Label para mostrar mensaje de error debajo de la contraseña
        Label mandatoryPasswordLabel = new Label(getBundle().getString("mandatory_password_label"));
        mandatoryPasswordLabel.getStyleClass().add("mandatoryFieldsLabel");
        mandatoryPasswordLabel.setVisible(false);  // Inicialmente no visible
        mandatoryPasswordLabel.setManaged(false); // No gestionado cuando no se muestra

        // Botón de copiar contraseña
        Image copyIcon = new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/copy_icon.png")).toExternalForm());
        ImageView copyIconView = new ImageView(copyIcon);
        copyIconView.getStyleClass().add("icon");

        Button copyButton = new Button();
        copyButton.setGraphic(copyIconView);
        copyButton.getStyleClass().add("icon-button");
        copyButton.setTooltip(new Tooltip(getBundle().getString("toolTip_copyToClipboard")));
        copyButton.setVisible(false); // Inicialmente no visible

        // Acción del botón de copiar
        copyButton.setOnAction(_ -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(passwordFieldVisible.getText());
            clipboard.setContent(content);

            // Mostrar notificación de copia
            Notifier.showNotification(copyButton.getScene().getWindow(), getBundle().getString("toolTip_textCopiedToClipboard"));
        });

        // Mostrar contraseña y el botón de copiar al hacer clic en la contraseña oculta
        passwordFieldHidden.setOnMouseClicked(_ -> {
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

        passwordBox.getChildren().addAll(passwordLabel, passwordFieldContainer, mandatoryPasswordLabel);

        // Agregar los campos al VBox principal en el orden adecuado
        vbox.getChildren().addAll(mandatoryFieldsLabel, descriptionBox, usernameBox, urlBox, passwordBox);

        // Crear HBox para los botones
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Crear y configurar el botón de eliminar
        Button deleteButton = new Button(getBundle().getString("delete_button"));
        deleteButton.getStyleClass().add("secondary");

        // Agregar el icono al botón de eliminar
        Image deleteIcon = new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/trash_icon.png")).toExternalForm());
        ImageView deleteIconView = new ImageView(deleteIcon);
        deleteIconView.getStyleClass().add("icon");
        deleteButton.setGraphic(deleteIconView);

        // Crear y configurar el botón de guardar
        Button saveButton = new Button(getBundle().getString("save_button"));
        saveButton.getStyleClass().add("primary");

        // Agregar el icono al botón de guardar
        Image saveIcon = new Image(Objects.requireNonNull(DialogUtil.class.getResource("/passworld/images/save_icon_white.png")).toExternalForm());
        ImageView saveIconView = new ImageView(saveIcon);
        saveIconView.getStyleClass().add("icon");
        saveButton.setGraphic(saveIconView);

        // Agregar botones al HBox
        buttonBox.getChildren().addAll(deleteButton, saveButton);

        // Agregar el VBox principal y los botones al contenido del diálogo
        VBox mainContent = new VBox(10);
        mainContent.getChildren().addAll(vbox, buttonBox);

        // Establecer el contenido del cuadro de diálogo
        dialog.getDialogPane().setContent(mainContent);

        // Listener para la validación de campos en tiempo real
        ChangeListener<String> fieldValidationListener = (observable, oldValue, newValue) -> {
            boolean isDescriptionValid = !(descriptionField.getText() == null || descriptionField.getText().trim().isEmpty());
            boolean isPasswordValid = !(passwordFieldVisible.getText() == null || passwordFieldVisible.getText().trim().isEmpty());

            // Validación de los campos
            if (!isDescriptionValid && !isPasswordValid) {
                // Si ambos campos están vacíos, mostrar mensaje general de error
                mandatoryFieldsLabel.setVisible(true);
                mandatoryFieldsLabel.setManaged(true);

                descriptionField.getStyleClass().add("error-border");  // Mostrar borde rojo en la descripción
                mandatoryDescriptionLabel.setVisible(false);
                mandatoryDescriptionLabel.setManaged(false);

                passwordFieldVisible.getStyleClass().add("error-border");  // Mostrar borde rojo en la contraseña
                mandatoryPasswordLabel.setVisible(false);
                mandatoryPasswordLabel.setManaged(false);
                copyButton.setVisible(false);  // Ocultar botón de copiar

                saveButton.setDisable(true);  // Deshabilitar el botón de guardar

                dialog.setHeight(390);
            } else {
                // Si la descripción tiene un valor, eliminar el borde de error
                if (isDescriptionValid) {
                    descriptionField.getStyleClass().remove("error-border");  // Eliminar borde rojo de la descripción
                    mandatoryDescriptionLabel.setVisible(false);  // Ocultar mensaje de error para descripción
                    mandatoryDescriptionLabel.setManaged(false);  // No gestionar el label cuando no se muestra
                }

                // Si la contraseña tiene un valor, eliminar el borde de error
                if (isPasswordValid) {
                    passwordFieldVisible.getStyleClass().remove("error-border");  // Eliminar borde rojo de la contraseña
                    mandatoryPasswordLabel.setVisible(false);  // Ocultar mensaje de error para contraseña
                    mandatoryPasswordLabel.setManaged(false);  // No gestionar el label cuando no se muestra
                }

                // Si solo la descripción está vacía, mostrar mensaje específico para la descripción
                if (!isDescriptionValid) {
                    // Ocultar error general
                    mandatoryFieldsLabel.setVisible(false);
                    mandatoryFieldsLabel.setManaged(false);

                    descriptionField.getStyleClass().add("error-border");  // Mostrar borde rojo en la descripción
                    mandatoryDescriptionLabel.setVisible(true);  // Mostrar mensaje de error para descripción
                    mandatoryDescriptionLabel.setManaged(true);  // Asegurar que el label sea gestionado
                    saveButton.setDisable(true);  // Deshabilitar el botón de guardar

                    dialog.setHeight(380);
                }

                // Si solo la contraseña está vacía, mostrar mensaje específico para la contraseña
                if (isDescriptionValid && !isPasswordValid) {
                    // Ocultar error general
                    mandatoryFieldsLabel.setVisible(false);
                    mandatoryFieldsLabel.setManaged(false);

                    passwordFieldVisible.getStyleClass().add("error-border");  // Mostrar borde rojo en la contraseña
                    mandatoryPasswordLabel.setVisible(true);  // Mostrar mensaje de error para contraseña
                    mandatoryPasswordLabel.setManaged(true);  // Asegurar que el label sea gestionado
                    saveButton.setDisable(true);  // Deshabilitar el botón de guardar
                    copyButton.setVisible(false);  // Ocultar botón de copiar

                    dialog.setHeight(380);
                }

                // Si ambos campos son válidos, habilitar el botón de guardar
                if (isDescriptionValid && isPasswordValid) {
                    saveButton.setDisable(false);  // Habilitar el botón de guardar

                    dialog.setHeight(360);
                }
            }

            // Deshabilitar el botón de guardar si algún campo no es válido
            saveButton.setDisable(!isDescriptionValid || !isPasswordValid);
        };

        // Agregar listeners a los campos de entrada
        descriptionField.textProperty().addListener(fieldValidationListener);
        passwordFieldVisible.textProperty().addListener(fieldValidationListener);

        // Manejar la acción del botón de guardar
        saveButton.setOnAction(_ -> {
            // Si se ha presionado el botón de guardar, actualizamos la contraseña
            String newDescription = descriptionField.getText();
            String newUsername = usernameField.getText();
            String newUrl = urlField.getText();
            String newPassword = passwordFieldVisible.getText();
            passwordsController.updatePassword(password, newDescription, newUsername, newUrl, newPassword);
            dialog.close();
        });

        // Mostrar el diálogo
        dialog.showAndWait();
    }




}
