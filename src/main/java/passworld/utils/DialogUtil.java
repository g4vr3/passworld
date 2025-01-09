package passworld.utils;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import passworld.data.PasswordDTO;
import passworld.service.LanguageManager;

import java.util.Optional;
import java.util.ResourceBundle;

public class DialogUtil {

    public static Optional<PasswordDTO> showPasswordDialog(String password) {
        ResourceBundle bundle = LanguageManager.getBundle();

        Dialog<PasswordDTO> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                DialogUtil.class.getResource("/passworld/styles/styles.css").toExternalForm()
        );
        dialog.setTitle("passworld - " + bundle.getString("dialog_title_save_password"));

        // Configurar botones
        ButtonType saveButtonType = new ButtonType(bundle.getString("save_button"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(bundle.getString("cancel_button"), ButtonBar.ButtonData.CANCEL_CLOSE);

        // Agregar los botones al cuadro de diálogo
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Estilo para el botón de Guardar
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.getStyleClass().add("primary");

        // Estilo para el botón de Cancelar
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("secondary");

        // Agregar el icono al botón de guardar
        Image saveIcon = new Image(DialogUtil.class.getResource("/passworld/images/save_icon_white.png").toExternalForm());
        ImageView saveIconView = new ImageView(saveIcon);
        saveIconView.setFitHeight(15); // Ajusta el tamaño del icono
        saveIconView.setFitWidth(15);
        saveButton.setGraphic(saveIconView); // Establecer el icono en el botón de guardar

        // Agregar el icono al botón de cancelar
        Image cancelIcon = new Image(DialogUtil.class.getResource("/passworld/images/cancel_icon.png").toExternalForm());
        ImageView cancelIconView = new ImageView(cancelIcon);
        cancelIconView.setFitHeight(15); // Ajusta el tamaño del icono
        cancelIconView.setFitWidth(15);
        cancelButton.setGraphic(cancelIconView); // Establecer el icono en el botón de cancelar

        // Establecer Tooltips para los botones
        saveButton.setTooltip(new Tooltip(bundle.getString("save_button_tooltip")));
        cancelButton.setTooltip(new Tooltip(bundle.getString("cancel_button_tooltip")));

        // Botón de guardar por defecto
        saveButton.setDefaultButton(true);

        // Crear contenedor principal VBox
        VBox vbox = new VBox(10); // Espaciado de 10 píxeles entre cada par de elementos

        // Crear etiquetas y campos de texto con espaciado entre ellos
        VBox descriptionBox = new VBox(5);  // Espacio de 5px entre Label y TextField
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

        VBox passwordBox = new VBox(5);  // Espacio de 5px entre Label y TextField
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
        ResourceBundle bundle = LanguageManager.getBundle();

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
        unlockIconView.setFitHeight(15);
        unlockIconView.setFitWidth(15);
        unlockButton.setGraphic(unlockIconView);

        // Agregar el icono al botón de cancelar
        Image cancelIcon = new Image(DialogUtil.class.getResource("/passworld/images/cancel_icon.png").toExternalForm());
        ImageView cancelIconView = new ImageView(cancelIcon);
        cancelIconView.setFitHeight(15);
        cancelIconView.setFitWidth(15);
        cancelButton.setGraphic(cancelIconView);

        // Establecer Tooltips para los botones
        unlockButton.setTooltip(new Tooltip(bundle.getString("unlock_button_tooltip")));
        cancelButton.setTooltip(new Tooltip(bundle.getString("cancel_button_tooltip")));

        // Botón de desbloquear por defecto
        unlockButton.setDefaultButton(true);

        // Crear contenedor principal VBox
        VBox vbox = new VBox(10); // Espaciado de 10 píxeles entre cada par de elementos

        // Añadir un texto con la información del desbloqueo
        Label unlockInfoLabel = new Label(bundle.getString("unlock_vault_info"));
        vbox.getChildren().add(unlockInfoLabel);

        // Crear un campo de texto para la clave maestra
        VBox masterKeyBox = new VBox(5);  // Espacio de 5px entre Label y PasswordField
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
}
