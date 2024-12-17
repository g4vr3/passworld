package passworld.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import passworld.data.PasswordDAO;
import passworld.data.PasswordDTO;
import passworld.service.LanguageManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class MyPasswordsController {

    @FXML
    private TableView<PasswordDTO> passwordTable;
    @FXML
    private TableColumn<PasswordDTO, String> descriptionColumn;
    @FXML
    private TableColumn<PasswordDTO, String> urlColumn;
    @FXML
    private TableColumn<PasswordDTO, String> passwordColumn;

    private ObservableList<PasswordDTO> passwordList;

    // Método para cargar la vista
    public static void showView() {
        // Obtener el ResourceBundle para manejar los textos en el idioma adecuado
        ResourceBundle bundle = LanguageManager.getBundle();
        try {
            // Cargar el archivo FXML
            FXMLLoader loader = new FXMLLoader(MyPasswordsController.class.getResource("/passworld/my-passwords-view.fxml"));
            Stage myPasswordsStage = new Stage();
            Scene scene = new Scene(loader.load(), 600, 450);
            scene.getStylesheets().add(MyPasswordsController.class.getResource("/passworld/styles/styles.css").toExternalForm());
            myPasswordsStage.setTitle("passworld - " + bundle.getString("my_passwords_title"));
            myPasswordsStage.setScene(scene);
            myPasswordsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método de inicialización que se llama cuando el FXML se cargar
    @FXML
    public void initialize() {
        // Obtener el ResourceBundle para manejar los textos en el idioma adecuado
        ResourceBundle bundle = LanguageManager.getBundle();

        // Configurar los encabezados de las columnas del TableView desde el ResourceBundle
        descriptionColumn.setText(bundle.getString("column_description"));
        urlColumn.setText(bundle.getString("column_url"));
        passwordColumn.setText(bundle.getString("column_password"));

        // Configurar columnas del TableView
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));

        // Configurar el tamaño fijo de las celdas
        passwordTable.setFixedCellSize(25); // Altura de cada fila

        // Placeholder dinámico
        passwordTable.setPlaceholder(new Label(bundle.getString("no_data_to_display")));

        // Cargar las contraseñas desde la base de datos
        loadPasswords();
    }

    private void loadPasswords() {
        try {
            // Obtener todas las contraseñas de la base de datos
            List<PasswordDTO> passwords = PasswordDAO.readAllPasswords();
            passwordList = FXCollections.observableArrayList(passwords);

            // Establecer la lista de contraseñas en el TableView
            passwordTable.setItems(passwordList);

            // Ajustar la altura del TableView según el número de filas
            adjustTableHeight(passwordList.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void adjustTableHeight(int rowCount) {
        // Altura total = altura de las filas + encabezado
        double totalHeight = rowCount * passwordTable.getFixedCellSize() + passwordTable.getFixedCellSize();
        passwordTable.setPrefHeight(totalHeight - 1);
    }
}
