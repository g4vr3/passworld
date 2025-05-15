package passworld.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Window;
import javafx.util.Duration;
import passworld.data.PasswordDAO;
import passworld.data.PasswordDTO;
import passworld.data.session.UserSession;
import passworld.data.sync.SyncHandler;
import passworld.service.LanguageManager;
import passworld.service.SecurityFilterManager;
import passworld.utils.*;
import passworld.service.PasswordManager;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class MyPasswordsController {
    @FXML
    ImageView logoImageView;
    @FXML
    private TableView<PasswordDTO> passwordTable;
    @FXML
    private TableColumn<PasswordDTO, String> passwordEntryColumn;
    @FXML
    private TableColumn<PasswordDTO, Void> infoButtonColumn;
    @FXML
    private TableColumn<PasswordDTO, Void> warningIconColumn;
    @FXML
    private Button backButton;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private Label myPasswordsHeaderLabel;
    @FXML
    private Button showAllPasswordsButton;
    @FXML
    private Button showIssuePasswordsButton;
    @FXML
    private Label allPasswordsCountLabel;
    @FXML
    private Label issuePasswordsCountLabel;
    @FXML
    private ImageView allPasswordsIconView;
    @FXML
    private ImageView issuePasswordsIconView;
    @FXML
    private Label allPasswordsButtonLabel;
    @FXML
    private Tooltip allPasswordsButtonTooltip;
    @FXML
    private Label issuePasswordsButtonLabel;
    @FXML
    private Tooltip issuePasswordsButtonTooltip;
    @FXML
    private Button newPasswordButton;
    @FXML
    private ImageView newPasswordImageView;
    @FXML
    private ImageView searchImageView;
    @FXML
    private Button searchButton;
    @FXML
    private HBox tableHeaderHBox;
    @FXML
    private Region tableHeaderRegion;

    private Button activeFilterButton; // Variable para rastrear el filtro activo
    private final TextField searchField = new TextField(); // Campo de búsqueda

    private Image protectIcon;
    private Image issuePasswordsIcon;

    private final ObservableList<PasswordDTO> passwordList = FXCollections.observableArrayList(); // Almacena la lista original
    private final ObservableList<PasswordDTO> issuePasswordsList = FXCollections.observableArrayList(); // Almacena la lista de contraseñas con problemas

    // Auxiliar para obtener el ResourceBundle dinámicamente
    private static ResourceBundle getBundle() {
        return LanguageManager.getBundle();
    }

    public static void showView() {
        ViewManager.changeView("/passworld/my-passwords-view.fxml", String.format("passworld - " + getBundle().getString("my_passwords_title")));
    }

    @FXML
    public void initialize() {
        // Establecer imagen de logo
        Image logoImage = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/passworld_logo.png")).toExternalForm());
        logoImageView.setImage(logoImage);
        ThemeManager.applyThemeToImage(logoImageView);

        // Establecer imagen de nueva contraseña
        Image newPasswordIcon = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/plus_icon.png")).toExternalForm());
        newPasswordImageView.setImage(newPasswordIcon);
        ThemeManager.applyThemeToImage(newPasswordImageView);
        newPasswordImageView.getStyleClass().add("icon");

        // Configurar el botón de nueva contraseña
        newPasswordButton.setOnAction(_ -> createNewPassword());

        // Establecer el mensaje de marcador de posición cuando no hay datos
        passwordTable.setPlaceholder(new Label(getBundle().getString("no_data_to_display")));

        setBackButton(); // Configurar el botón de salir
        loadPasswords(); // Cargar las contraseñas en la tabla

        // Configurar el filtro activo inicial
        activeFilterButton = showAllPasswordsButton;

        // Resaltar el botón de todas las contraseñas
        highlightSelectedFilterButton(showAllPasswordsButton);

        // Mostrar u ocultar el ComboBox de ordenación según los datos
        sortComboBox.setVisible(!passwordList.isEmpty());

        // Añadir después de cargar las contraseñas
        setupSortComboBox(); // Configurar el ComboBox para mostrar solo un icono
        sortPasswords(passwordList); // Ordenar las contraseñas según la opción predeterminada

        addCustomCells(); // Configurar las celdas de la tabla
        addTableRowClickListener(); // Configurar clicado en registros de la tabla
        hideTableHeader(); // Ocultar el encabezado de la tabla

        // Etiqueta de descripción de los registros mostrados en la tabla
        myPasswordsHeaderLabel.setText(getBundle().getString("password_entry_header_all"));

        // Configurar el botón de búsqueda
        HBox searchBar = createSearchBar(searchButton, searchImageView, passwordList, issuePasswordsList, passwordTable);
        tableHeaderHBox.getChildren().add(2, searchBar);

        // Agregar el listener para el ComboBox
        sortComboBox.setOnAction(_ -> sortPasswords(passwordTable.getItems()));

        // Cargar los iconos según el tema
        String themeSuffix = ThemeManager.isDarkMode() ? "_dark_mode" : "";
        Image allPasswordsIcon = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/all_passwords_icon" + themeSuffix + ".png")).toExternalForm());
        protectIcon = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/protect_icon" + themeSuffix + ".png")).toExternalForm());
        issuePasswordsIcon = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/warning_icon" + themeSuffix + ".png")).toExternalForm());

        // Asignar el icono y texto al botón de mostrar todas las contraseñas
        allPasswordsIconView.setImage(allPasswordsIcon);

        // Establecer textos dinámicamente
        allPasswordsButtonLabel.setText(getBundle().getString("all_passwords_button_text"));
        allPasswordsButtonTooltip.setText(getBundle().getString("all_passwords_button_tooltip"));

        // Asignar el icono y texto al botón de mostrar contraseñas con problemas
        updateIssuePasswordsButton();

        // Asignación de botones
        showAllPasswordsButton.setOnAction(_ -> showAllPasswords());
        showIssuePasswordsButton.setOnAction(_ -> showIssuePasswords());

        // Inicializar los contadores de contraseñas
        allPasswordsCountLabel.setText(String.valueOf(passwordList.size()));
        issuePasswordsCountLabel.setText(String.valueOf(issuePasswordsList.size()));
        syncPasswordsPeriodically();
    }

    private void createNewPassword() {
        // Mostrar el cuadro de diálogo para crear una nueva contraseña
        DialogUtil.showPasswordCreationDialog("").ifPresent(passwordDTO -> {
            try {
                // Guardar la nueva contraseña en la base de datos
                boolean success = PasswordManager.savePassword(passwordDTO);
                if (success) {
                    // Recargar la lista de contraseñas
                    loadPasswords();
                    Notifier.showNotification(newPasswordButton.getScene().getWindow(), getBundle().getString("toolTip_password_saved"));
                } else {
                    Notifier.showNotification(newPasswordButton.getScene().getWindow(), getBundle().getString("toolTip_password_not_saved"));
                }
            } catch (SQLException e) {
                System.err.println("Error saving password: " + e.getMessage());
                Notifier.showNotification(newPasswordButton.getScene().getWindow(), getBundle().getString("toolTip_database_error"));
            }
        });
    }

    private void loadPasswords() {
        try {
            // Guardar el filtro actual
            String currentFilter = searchField.getText().toLowerCase();

            // Obtener datos de la base de datos y almacenarlos en la lista original
            List<PasswordDTO> passwords = PasswordDAO.readAllPasswordsDecrypted();
            passwordList.setAll(passwords); // Actualizar la lista observable
            issuePasswordsList.setAll(passwords.stream()
                    .filter(SecurityFilterManager::hasPasswordSecurityIssues)
                    .collect(Collectors.toList())); // Actualizar la lista de contraseñas con problemas

            // Restaurar la lista activa
            ObservableList<PasswordDTO> activeList = activeFilterButton == showIssuePasswordsButton ? issuePasswordsList : passwordList;
            passwordTable.setItems(activeList);

            // ordenar la lista activa
            sortPasswords(activeList);

            // Aplicar el filtro actual
            if (!currentFilter.isEmpty()) {
                FilteredList<PasswordDTO> filteredPasswords = new FilteredList<>(activeList, password ->
                        (password.getDescription() != null && password.getDescription().toLowerCase().startsWith(currentFilter)) ||
                        (password.getUsername() != null && password.getUsername().toLowerCase().startsWith(currentFilter)) ||
                        (password.getUrl() != null && password.getUrl().toLowerCase().startsWith(currentFilter)));
                passwordTable.setItems(filteredPasswords);
                adjustTableHeight(filteredPasswords.size());
            } else {
                adjustTableHeight(activeList.size());
            }

            // Actualizar el texto de los botones
            allPasswordsCountLabel.setText(String.valueOf(passwordList.size()));
            issuePasswordsCountLabel.setText(String.valueOf(issuePasswordsList.size()));

            // Mostrar u ocultar el ComboBox de ordenación
            sortComboBox.setVisible(!passwordList.isEmpty());

            passwordTable.refresh();

        } catch (SQLException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    @FXML
    private void showAllPasswords() {
        // Cerrar búsqueda si está abierta
        toggleSearchBar(false);

        passwordTable.setItems(passwordList); // Mostrar todas las contraseñas
        myPasswordsHeaderLabel.setText(getBundle().getString("password_entry_header_all"));
        sortPasswords(passwordList); // Ordenar la lista completa
        adjustTableHeight(passwordList.size()); // Ajustar la altura de la tabla

        // Actualizar el texto de los botones
        allPasswordsCountLabel.setText(String.valueOf(passwordList.size()));
        issuePasswordsCountLabel.setText(String.valueOf(issuePasswordsList.size()));

        // Ocultar el ComboBox si no hay contraseñas
        sortComboBox.setVisible(!passwordList.isEmpty());

        // Resaltar el botón de todas las contraseñas
        highlightSelectedFilterButton(showAllPasswordsButton);

        passwordTable.refresh(); // Actualizar la tabla para reflejar los cambios
    }

    @FXML
    private void showIssuePasswords() {
        // Cerrar búsqueda si está abierta
        toggleSearchBar(false);

        passwordTable.setItems(issuePasswordsList); // Mostrar solo las contraseñas con problemas
        myPasswordsHeaderLabel.setText(getBundle().getString("password_entry_header_issue"));
        sortPasswords(issuePasswordsList); // Ordenar la lista de contraseñas con problemas
        adjustTableHeight(issuePasswordsList.size()); // Ajustar la altura de la tabla

        // Actualizar el texto de los botones
        allPasswordsCountLabel.setText(String.valueOf(passwordList.size()));
        issuePasswordsCountLabel.setText(String.valueOf(issuePasswordsList.size()));

        // Ocultar el ComboBox si no hay contraseñas con problemas
        sortComboBox.setVisible(!issuePasswordsList.isEmpty());

        // Resaltar el botón de contraseñas con problemas
        highlightSelectedFilterButton(showIssuePasswordsButton);

        passwordTable.refresh(); // Actualizar la tabla para reflejar los cambios
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
        ThemeManager.applyThemeToImage(sortIcon);
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
        sortComboBox.setCellFactory(_ -> new ListCell<>() {
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

    private void sortPasswords(ObservableList<PasswordDTO> passwords) {
        String selectedSortOrder = sortComboBox.getValue();

        if (selectedSortOrder == null) {
            FXCollections.sort(passwords, (p1, p2) -> Integer.compare(p2.getId(), p1.getId())); // Ordenar por ID de forma descendente
            return;
        }

        // Obtener las cadenas de ordenación del archivo de recursos
        String sortAZ = getBundle().getString("sort_az");
        String sortZA = getBundle().getString("sort_za");
        String sortNewestToOldest = getBundle().getString("sort_newest_to_oldest");
        String sortOldestToNewest = getBundle().getString("sort_oldest_to_newest");

        if (selectedSortOrder.equals(sortAZ)) {
            FXCollections.sort(passwords, (p1, p2) -> p1.getDescription().compareToIgnoreCase(p2.getDescription()));
        } else if (selectedSortOrder.equals(sortZA)) {
            FXCollections.sort(passwords, (p1, p2) -> p2.getDescription().compareToIgnoreCase(p1.getDescription()));
        } else if (selectedSortOrder.equals(sortNewestToOldest)) {
            FXCollections.sort(passwords, (p1, p2) -> Integer.compare(p2.getId(), p1.getId())); // Ordenar por ID de forma descendente
        } else if (selectedSortOrder.equals(sortOldestToNewest)) {
            FXCollections.sort(passwords, Comparator.comparingInt(PasswordDTO::getId)); // Ordenar por ID de forma ascendente
        }

        passwordTable.setItems(passwords);
    }

    private void setBackButton() {
        // Configurar el icono y el estilo del botón de volver
        ViewManager.setBackButton(backButton);

        // Configurar la acción del botón de volver
        backButton.setOnAction(_ -> {
            // Volver a la vista anterior
            ViewManager.changeView("/passworld/main-view.fxml", "passworld");
        });
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
        warningIconColumn.setReorderable(false);
        warningIconColumn.setResizable(false);
        warningIconColumn.setSortable(false);

        // Configurar las celdas de la columna de entrada de contraseña
        passwordEntryColumn.setCellFactory(_ -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                PasswordDTO password = getTableRow().getItem();

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

        // Configurar las celdas de la columna de icono de advertencia
        warningIconColumn.setCellFactory(_ -> new TableCell<>() {
            private final ImageView warningIconView = new ImageView();

            {
                // Configurar el icono de advertencia
                Image warningIcon = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/warning_icon.png")).toExternalForm());
                warningIconView.setImage(warningIcon);
                ThemeManager.applyThemeToImage(warningIconView);
                warningIconView.getStyleClass().add("icon");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    PasswordDTO password = getTableRow().getItem();
                    if (SecurityFilterManager.hasPasswordSecurityIssues(password)) {
                        setGraphic(warningIconView);
                    } else {
                        setGraphic(null);
                    }
                    setAlignment(Pos.CENTER);
                    setPadding(new Insets(0, 0, 0, 0));
                }
            }
        });

        // Configurar las celdas de la columna de botones de información
        infoButtonColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button showInfoButton = new Button();

            {
                // Configurar el icono y el estilo del botón de información
                Image gtIcon = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/gt_icon.png")).toExternalForm());
                ImageView gtImageView = new ImageView(gtIcon);
                ThemeManager.applyThemeToImage(gtImageView);
                gtImageView.getStyleClass().add("icon");
                showInfoButton.setGraphic(gtImageView);

                showInfoButton.getStyleClass().add("icon-button");

                // Configurar la acción del botón de información
                showInfoButton.setOnAction(_ -> {
                    PasswordDTO password = getTableView().getItems().get(getIndex());
                    PasswordInfoController.showView(password, MyPasswordsController.this); // Llamar a la vista de detalles
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); // Ocultar el botón si la celda está vacía
                } else {
                    setGraphic(showInfoButton); // Mostrar el botón si hay un elemento
                    setAlignment(Pos.CENTER); // Centrar el botón
                }
            }
        });
    }

    private void addTableRowClickListener() {
        passwordTable.setRowFactory(_ -> {
            TableRow<PasswordDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) { // Detectar clic simple
                    PasswordDTO clickedPassword = row.getItem();
                    PasswordInfoController.showView(clickedPassword, this); // Llamar a la vista de detalles
                }
            });
            return row;
        });
    }

    private void hideTableHeader() {
        // Ocultar el encabezado de la tabla
        passwordTable.widthProperty().addListener((_, _, _) -> {
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
            boolean success = PasswordManager.deletePassword(password.getId());
            if (success) {
                Notifier.showNotification(window, getBundle().getString("password_deleted_successfully"));
                loadPasswords();

            } else {
                Notifier.showNotification(window, getBundle().getString("password_deleted_failed"));
            }
        } catch (SQLException e) {
            System.err.println("ERROR: " + e.getMessage());
            Notifier.showNotification(window, getBundle().getString("toolTip_database_error"));
        }
    }

    public void updatePassword(PasswordDTO passwordToUpdate, String description, String username, String url, String password) {
        Window window = passwordTable.getScene().getWindow();

        try {
            boolean success = PasswordManager.updatePassword(passwordToUpdate, description, username, url, password);
            if (success) {
                Notifier.showNotification(window, getBundle().getString("password_updated_successfully"));
                loadPasswords();
            } else {
                Notifier.showNotification(window, getBundle().getString("password_updated_failed"));
            }
        } catch (SQLException e) {
            System.err.println("ERROR: " + e.getMessage());
            Notifier.showNotification(window, getBundle().getString("toolTip_database_error"));
        }
    }

    private void updateIssuePasswordsButton() {
        String themeSuffix = ThemeManager.isDarkMode() ? "_dark_mode" : "";

        if (issuePasswordsList.isEmpty()) {
            protectIcon = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/protect_icon" + themeSuffix + ".png")).toExternalForm());
            issuePasswordsIconView.setImage(protectIcon);

            // Eliminar el estilo de advertencia y aplicar el normal
            issuePasswordsCountLabel.getStyleClass().remove("counter-label-issue");
            if (!issuePasswordsCountLabel.getStyleClass().contains("counter-label")) {
                issuePasswordsCountLabel.getStyleClass().add("counter-label");
            }
        } else {
            issuePasswordsIcon = new Image(Objects.requireNonNull(getClass().getResource("/passworld/images/warning_icon" + themeSuffix + ".png")).toExternalForm());
            issuePasswordsIconView.setImage(issuePasswordsIcon);

            // Aplicar el estilo de advertencia
            issuePasswordsCountLabel.getStyleClass().remove("counter-label");
            if (!issuePasswordsCountLabel.getStyleClass().contains("counter-label-issue")) {
                issuePasswordsCountLabel.getStyleClass().add("counter-label-issue");
            }
        }

        issuePasswordsButtonLabel.setText(getBundle().getString("issue_passwords_button_text"));
        issuePasswordsButtonTooltip.setText(issuePasswordsList.isEmpty()
                ? getBundle().getString("issue_passwords_button_ok_tooltip")
                : getBundle().getString("issue_passwords_button_issues_tooltip"));
    }

    private void highlightSelectedFilterButton(Button selectedButton) {
        // Remover la clase de todos los botones
        showAllPasswordsButton.getStyleClass().remove("selected-filter-button");
        showIssuePasswordsButton.getStyleClass().remove("selected-filter-button");

        // Agregar la clase al botón seleccionado
        selectedButton.getStyleClass().add("selected-filter-button");

        // Actualizar el filtro activo
        activeFilterButton = selectedButton;
    }

    public void syncPasswordsPeriodically() {
        new Thread(() -> {
            boolean lastOnlineStatus = false;
            boolean lastLoginStatus = false;

            while (true) {
                try {
                    boolean isOnline = SyncHandler.hasInternetConnection();
                    boolean isLoggedIn = UserSession.getInstance().isLoggedIn();

                    // Solo sincronizar si está online y logueado
                    if (isOnline && isLoggedIn) {
                        Thread.sleep(3000); // Esperar 3s antes de sincronizar
                        if(TimeSyncManager.getOffset().isZero()){
                            TimeSyncManager.syncTimeWithUtcServer();
                        }
                        List<PasswordDTO> localPasswords = PasswordDAO.readAllPasswords();
                        SyncHandler.syncPasswords(localPasswords);
                        Platform.runLater(this::loadPasswords);
                    }

                    // Detecta cambio de estado para debug/log (opcional)
                    if (lastOnlineStatus != isOnline || lastLoginStatus != isLoggedIn) {
                        System.out.println("Estado actualizado: online=" + isOnline + ", login=" + isLoggedIn);
                        lastOnlineStatus = isOnline;
                        lastLoginStatus = isLoggedIn;
                    }

                    Thread.sleep(7000); // Esperar 10s antes del siguiente intento
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (IOException | SQLException e) {
                    System.out.println("Error during synchronization: " + e.getMessage());
                }
            }
        }, "PasswordSyncThread").start();
    }

    public HBox createSearchBar(Button searchButton, ImageView searchImageView, ObservableList<PasswordDTO> passwordList, ObservableList<PasswordDTO> issuePasswordsList, TableView<PasswordDTO> passwordTable) {
        // Crear el TextField (campo de búsqueda)
        searchField.setPromptText(getBundle().getString("search_field_prompt"));
        searchField.setVisible(false); // Oculto inicialmente
        searchField.setManaged(false); // No ocupa espacio inicialmente
        searchField.setPrefWidth(0); // Ancho inicial 0
        searchField.getStyleClass().add("search-bar");

        // Crear el botón con el icono de lupa
        searchImageView.setImage(new Image(Objects.requireNonNull(MyPasswordsController.class.getResource("/passworld/images/search_icon.png")).toExternalForm()));
        searchImageView.getStyleClass().add("icon");
        ThemeManager.applyThemeToImage(searchImageView);

        searchButton.getStyleClass().add("icon-button");

        // Contenedor para el buscador
        HBox searchBox = new HBox(searchField, searchButton);
        searchBox.setSpacing(5);
        searchBox.setAlignment(javafx.geometry.Pos.CENTER); // Centrar verticalmente
        HBox.setHgrow(searchField, Priority.ALWAYS); // Permitir que el TextField ocupe espacio

        // Animación para expandir/colapsar el buscador
        searchButton.setOnAction(_ -> toggleSearchBar(!searchField.isVisible()));

        // Lógica de filtrado
        searchField.textProperty().addListener((_, _, newVal) -> {
            String filter = newVal.toLowerCase();

            // Determinar la lista activa
            ObservableList<PasswordDTO> activeList = passwordTable.getItems() == issuePasswordsList ? issuePasswordsList : passwordList;

            FilteredList<PasswordDTO> filteredPasswords = new FilteredList<>(activeList, password -> {
                if (filter.isEmpty()) return true;

                return (password.getDescription() != null && password.getDescription().toLowerCase().startsWith(filter)) ||
                        (password.getUsername() != null && password.getUsername().toLowerCase().startsWith(filter)) ||
                        (password.getUrl() != null && password.getUrl().toLowerCase().startsWith(filter));
            });

            passwordTable.setItems(filteredPasswords);
            passwordTable.refresh(); // Actualizar la tabla para reflejar los cambios
            adjustTableHeight(filteredPasswords.size()); // Ajustar la altura de la tabla
        });

        return searchBox;
    }

    private void toggleSearchBar(boolean expand) {
        if (expand) {
            searchField.setVisible(true);
            searchField.setManaged(true);

            Timeline show = new Timeline(
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(searchField.prefWidthProperty(), 380) // Ancho expandido
                    )
            );
            myPasswordsHeaderLabel.setVisible(false); // Ocultar el label al expandir
            myPasswordsHeaderLabel.setManaged(false);
            tableHeaderRegion.setVisible(false); // Ocultar el region de la tabla

            show.play();
            searchField.requestFocus();
        } else {
            Timeline collapse = new Timeline(
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(searchField.prefWidthProperty(), 0)
                    )
            );
            collapse.setOnFinished(_ -> {
                searchField.setVisible(false);
                searchField.setManaged(false);
                searchField.clear();

                myPasswordsHeaderLabel.setVisible(true); // Mostrar el label al colapsar
                myPasswordsHeaderLabel.setManaged(true);
                tableHeaderRegion.setVisible(true); // Mostrar el region de la tabla
                tableHeaderRegion.setManaged(true);

                // Restaurar la lista completa
                passwordTable.setItems(activeFilterButton == showIssuePasswordsButton ? issuePasswordsList : passwordList);
                passwordTable.refresh();
                adjustTableHeight(passwordTable.getItems().size()); // Ajustar la altura de la tabla
            });
            collapse.play();
        }
    }
}