package passworld.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import passworld.data.PasswordDAO;
import passworld.data.PasswordDTO;
import passworld.service.LanguageManager;
import passworld.utils.DialogUtil;
import passworld.utils.Notifier;
import passworld.service.PasswordManager;

import java.io.InputStream;
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
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private Label MyPasswordsHeaderLabel;

    private ObservableList<PasswordDTO> passwordList = FXCollections.observableArrayList(); // Almacena la lista original

    // Auxiliar para obtener el ResourceBundle dinámicamente
    private static ResourceBundle getBundle() {
        return LanguageManager.getBundle();
    }

    public static void showView() {
        try {
            FXMLLoader loader = new FXMLLoader(MyPasswordsController.class.getResource("/passworld/my-passwords-view.fxml"));
            Stage myPasswordsStage = new Stage();
            Scene scene = new Scene(loader.load(), 600, 450);
            scene.getStylesheets().add(MyPasswordsController.class.getResource("/passworld/styles/styles.css").toExternalForm());
            myPasswordsStage.getIcons().add(new Image(MyPasswordsController.class.getResourceAsStream("/passworld/images/app_icon.png")));
            myPasswordsStage.setTitle("passworld - " + getBundle().getString("my_passwords_title"));
            myPasswordsStage.setScene(scene);
            myPasswordsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Establecer el mensaje de marcador de posición cuando no hay datos
        passwordTable.setPlaceholder(new Label(getBundle().getString("no_data_to_display")));

        setBackButton(); // Configurar el botón de salir
        loadPasswords(); // Cargar las contraseñas en la tabla

        // Mostrar u ocultar el ComboBox de ordenación según los datos
        sortComboBox.setVisible(!passwordList.isEmpty());

        // Añadir después de cargar las contraseñas
        setupSortComboBox(); // Configurar el ComboBox para mostrar solo un icono
        sortPasswords(); // Ordenar las contraseñas según la opción predeterminada

        addCustomCells(); // Configurar las celdas de la tabla
        addTableRowClickListener(); // Configurar clicado en registros de la tabla
        hideTableHeader(); // Ocultar el encabezado de la tabla

        // Etiqueta de descripción de los registros mostrados en la tabla
        MyPasswordsHeaderLabel.setText(getBundle().getString("password_entry_header_all"));

        // Agregar el listener para el ComboBox
        sortComboBox.setOnAction(event -> sortPasswords());
    }


    private void setupSortComboBox() {
        // Establecer el icono para el ComboBox
        Image sortImage;
        InputStream iconStream = getClass().getResourceAsStream("/passworld/images/sort_icon.png");
        if (iconStream == null) {
            System.out.println("El recurso del ícono no se encontró.");
            return;
        }
        sortImage = new Image(iconStream);
        ImageView sortIcon = new ImageView(sortImage);
        sortIcon.getStyleClass().add("sort-icon");

        // Aplicar el estilo de icono al botón del ComboBox
        sortComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null); // No mostrar texto
                setGraphic(empty ? null : sortIcon); // Mostrar solo el icono
            }
        });

        // Aplicar estilo al ComboBox para ocultar la flecha y el fondo
        sortComboBox.getStyleClass().add("sort-box");

        // Configurar las celdas de la lista del ComboBox
        sortComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
            }
        });

        // Añadir opciones al ComboBox
        sortComboBox.setItems(FXCollections.observableArrayList(
                getBundle().getString("sort_newest_to_oldest"),
                getBundle().getString("sort_oldest_to_newest"),
                getBundle().getString("sort_az"),
                getBundle().getString("sort_za")
        ));

        // Seleccionar por defecto la opción "Más reciente a más antigua"
        sortComboBox.getSelectionModel().select(getBundle().getString("sort_newest_to_oldest"));
    }

    private void sortPasswords() {
        String selectedSortOrder = sortComboBox.getValue();

        if (selectedSortOrder == null) {
            return;
        }

        // Obtener las cadenas de ordenación del archivo de recursos
        String sortAZ = getBundle().getString("sort_az");
        String sortZA = getBundle().getString("sort_za");
        String sortNewestToOldest = getBundle().getString("sort_newest_to_oldest");
        String sortOldestToNewest = getBundle().getString("sort_oldest_to_newest");

        if (selectedSortOrder.equals(sortAZ)) {
            FXCollections.sort(passwordList, (p1, p2) -> p1.getDescription().compareToIgnoreCase(p2.getDescription()));
        } else if (selectedSortOrder.equals(sortZA)) {
            FXCollections.sort(passwordList, (p1, p2) -> p2.getDescription().compareToIgnoreCase(p1.getDescription()));
        } else if (selectedSortOrder.equals(sortNewestToOldest)) {
            // Volver a ordenar por defecto (más antigua - más reciente)
            loadPasswords();
            // Revertir orden por defecto
            FXCollections.reverse(passwordList);
        } else if (selectedSortOrder.equals(sortOldestToNewest)) {
            loadPasswords();
            return;
        }

        passwordTable.setItems(passwordList);
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

    private void loadPasswords() {
        try {
            // Obtener datos de la base de datos y almacenarlos en la lista original
            List<PasswordDTO> passwords = PasswordDAO.readAllPasswords();
            passwordList = FXCollections.observableArrayList(passwords);
            passwordTable.setItems(passwordList);

            // Mostrar u ocultar el ComboBox de ordenación
            sortComboBox.setVisible(!passwordList.isEmpty());

            adjustTableHeight(passwordList.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void adjustTableHeight(int rowCount) {
        // Ajustar la altura de la tabla dinámicamente
        double rowHeight = 42;
        double totalHeight = rowCount * rowHeight;
        passwordTable.setPrefHeight(totalHeight + 35);
    }

    private void addCustomCells() {
        // Configurar propiedades de las columnas
        passwordEntryColumn.setReorderable(false);
        infoButtonColumn.setReorderable(false);
        passwordEntryColumn.setResizable(false);
        infoButtonColumn.setResizable(false);
        passwordEntryColumn.setSortable(false);
        infoButtonColumn.setSortable(false);

        // Configurar las celdas de la columna de entrada de contraseña
        passwordEntryColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                PasswordDTO password = (PasswordDTO) getTableRow().getItem();

                // Mostrar descripción
                Label descriptionLabel = new Label(password.getDescription());
                descriptionLabel.getStyleClass().add("description-label");

                // Mostrar nombre de usuario
                Label usernameLabel = new Label(password.getUsername() != null ? password.getUsername() : getBundle().getString("no_username_provided"));
                usernameLabel.getStyleClass().add("username-label");

                // Crear layout para ambas etiquetas
                VBox vBox = new VBox(descriptionLabel, usernameLabel);
                vBox.setSpacing(0);

                // Contenedor para centrado y espaciado
                HBox hBox = new HBox(vBox);
                hBox.setSpacing(10);
                setGraphic(hBox);
            }
        });

        // Configurar las celdas de la columna de botones de información
        infoButtonColumn.setCellFactory(column -> new TableCell<>() {
            private final Button showInfoButton = new Button();

            {
                // Configurar el icono y el estilo del botón de información
                Image gtIcon = new Image(getClass().getResource("/passworld/images/gt_icon.png").toExternalForm());
                ImageView gtImageView = new ImageView(gtIcon);
                gtImageView.getStyleClass().add("icon");
                showInfoButton.setGraphic(gtImageView);

                showInfoButton.getStyleClass().add("icon-button");

                // Configurar la acción del botón de información
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
                    setAlignment(Pos.CENTER);
                    setPadding(new Insets(0, 20, 0, 0)); // Padding derecho
                }
            }
        });
    }

    private void addTableRowClickListener() {
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

    private void hideTableHeader() {
        // Ocultar el encabezado de la tabla
        passwordTable.widthProperty().addListener((observable, oldValue, newValue) -> {
            Pane header = (Pane) passwordTable.lookup("TableHeaderRow");
            if (header != null && header.isVisible()) {
                header.setMaxHeight(0);
                header.setMinHeight(0);
                header.setPrefHeight(0);
                header.setVisible(false);
            }
        });
    }

    public void deletePassword(PasswordDTO password) {
        Window window = passwordTable.getScene().getWindow();

        try {
            // Llamar al PasswordManager para eliminar la contraseña
            boolean success = PasswordManager.deletePassword(password);
            if (success) {
                Notifier.showNotification(window, getBundle().getString("password_deleted_successfully"));
                loadPasswords();

                // Actualizar la visibilidad del ComboBox de ordenación
                sortComboBox.setVisible(!passwordList.isEmpty());
            } else {
                Notifier.showNotification(window, getBundle().getString("password_deleted_failed"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Notifier.showNotification(window, getBundle().getString("toolTip_database_error"));
        }
    }


    public void updatePassword(PasswordDTO passwordToUpdate, String description, String username, String url, String password) {
        Window window = passwordTable.getScene().getWindow();

        try {
            // Llamar al PasswordManager para actualizar la contraseña
            boolean success = PasswordManager.updatePassword(passwordToUpdate, description, username, url, password);
            if (success) {
                Notifier.showNotification(window, getBundle().getString("password_updated_successfully"));
                loadPasswords();
            } else {
                Notifier.showNotification(window, getBundle().getString("password_updated_failed"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Notifier.showNotification(window, getBundle().getString("toolTip_database_error"));
        }
    }
}