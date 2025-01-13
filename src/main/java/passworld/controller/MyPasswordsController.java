package passworld.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import passworld.data.PasswordDAO;
import passworld.data.PasswordDTO;
import passworld.service.LanguageManager;
import passworld.utils.DialogUtil;
import passworld.utils.Notifier;
import passworld.service.PasswordManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class MyPasswordsController {
    @FXML
    private TableView<PasswordDTO> passwordTable;
    @FXML
    private TableColumn<PasswordDTO, String> passwordEntryColumn;
    @FXML
    private TableColumn<PasswordDTO, Void> infoButtonColumn;
    @FXML
    private Button backButton;

    static ResourceBundle bundle = LanguageManager.getBundle();

    public static void showView() {
        try {
            FXMLLoader loader = new FXMLLoader(MyPasswordsController.class.getResource("/passworld/my-passwords-view.fxml"));
            Stage myPasswordsStage = new Stage();
            Scene scene = new Scene(loader.load(), 600, 450);
            scene.getStylesheets().add(MyPasswordsController.class.getResource("/passworld/styles/styles.css").toExternalForm());
            myPasswordsStage.getIcons().add(new Image(MyPasswordsController.class.getResourceAsStream("/passworld/images/app_icon.png")));
            myPasswordsStage.setTitle("passworld - " + bundle.getString("my_passwords_title"));
            myPasswordsStage.setScene(scene);
            myPasswordsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        passwordTable.setPlaceholder(new Label(bundle.getString("no_data_to_display")));

        // Inicializar encabezado de la columna al inicio con "Todas"
        passwordEntryColumn.setText(bundle.getString("password_entry_header_all"));

        setBackButton();
        loadPasswords();
        addCustomCells();

        // Manejar clics en filas de la tabla
        passwordTable.setRowFactory(tableView -> {
            TableRow<PasswordDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) { // Detectar clic simple
                    PasswordDTO clickedPassword = row.getItem();
                    DialogUtil.showPasswordInfoDialog(clickedPassword, this); // Pasar el controlador a DialogUtil
                }
            });
            return row;
        });
    }

    private void setBackButton() {
        // Icono
        Image ltIcon = new Image(getClass().getResource("/passworld/images/lt_icon.png").toExternalForm());
        ImageView ltImageView = new ImageView(ltIcon);
        ltImageView.getStyleClass().add("icon");
        backButton.setGraphic(ltImageView);

        // Estilo de botón de icono
        backButton.getStyleClass().add("icon-button");

        backButton.setOnAction(event -> {
            // Obtener el Stage desde el botón y cerrarlo
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.close();
        });
    }

    private void loadPasswords() {
        try {
            List<PasswordDTO> passwords = PasswordDAO.readAllPasswords();  // Obtener datos de la base de datos
            ObservableList<PasswordDTO> passwordList = FXCollections.observableArrayList(passwords);  // Convertir a lista observable
            passwordTable.setItems(passwordList);  // Establecer los elementos en la tabla
            adjustTableHeight(passwordList.size());  // Ajustar la altura de la tabla según el número de registros
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ajustar la altura de la tabla dinámicamente para mostrar tantas filas como registros haya
    private void adjustTableHeight(int rowCount) {
        double rowHeight = 40; // Establecer una altura fija por fila
        double totalHeight = rowCount * rowHeight; // Calcular la altura total
        passwordTable.setMaxHeight(totalHeight + 30); // Ajustar la altura máxima de la tabla contando con encabezado y margen
    }

    private void addCustomCells() {
        // Evitar reordenación de columnas y redimensionado
        passwordEntryColumn.setReorderable(false);
        infoButtonColumn.setReorderable(false);
        passwordEntryColumn.setResizable(false);
        infoButtonColumn.setResizable(false);

        // Columna combinada para descripción y URL
        passwordEntryColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                PasswordDTO password = (PasswordDTO) getTableRow().getItem();

                Label descriptionLabel = new Label(password.getDescription());
                descriptionLabel.getStyleClass().add("description-label");
                Label urlLabel = new Label(password.getUrl());
                urlLabel.getStyleClass().add("url-label");

                VBox vBox = new VBox(descriptionLabel, urlLabel);
                vBox.setSpacing(0);

                HBox hBox = new HBox(vBox);
                hBox.setSpacing(10); // Espaciado entre la columna de texto y el botón
                setGraphic(hBox);
            }
        });

        // Columna para el botón de información
        infoButtonColumn.setCellFactory(column -> new TableCell<>() {
            private final Button showInfoButton = new Button();

            {
                Image gtIcon = new Image(getClass().getResource("/passworld/images/gt_icon.png").toExternalForm());
                ImageView gtImageView = new ImageView(gtIcon);
                gtImageView.getStyleClass().add("icon");
                showInfoButton.setGraphic(gtImageView);

                // Estilo de botón de icono
                showInfoButton.getStyleClass().add("icon-button");

                showInfoButton.setOnAction(event -> {
                    PasswordDTO password = getTableView().getItems().get(getIndex());
                    DialogUtil.showPasswordInfoDialog(password, MyPasswordsController.this);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(showInfoButton);
                    // Asegurarse de que el botón esté centrado verticalmente en la celda
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    public void deletePassword(PasswordDTO password) {
        Window window = passwordTable.getScene().getWindow();

        try {
            boolean success = PasswordManager.deletePassword(password); // Llamar al PasswordManager para eliminar
            if (success) {
                Notifier.showNotification(window, bundle.getString("password_deleted_successfully"));
                loadPasswords();  // Recargar los passwords después de la eliminación
            } else {
                Notifier.showNotification(window, bundle.getString("password_deleted_failed"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Notifier.showNotification(window, bundle.getString("toolTip_database_error"));
        }
    }

    // Método para actualizar una contraseña
    public void updatePassword(PasswordDTO passwordToUpdate, String description, String url, String password) {
        Window window = passwordTable.getScene().getWindow();

        try {
            boolean success = PasswordManager.updatePassword(passwordToUpdate, description, url, password); // Llamar al PasswordManager para actualizar
            if (success) {
                Notifier.showNotification(window, bundle.getString("password_updated_successfully"));
                loadPasswords();  // Recargar los passwords después de la actualización
            } else {
                Notifier.showNotification(window, bundle.getString("password_updated_failed"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Notifier.showNotification(window, bundle.getString("toolTip_database_error"));
        }
    }
}
