package passworld;

import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import passworld.controller.SplashScreenController;
import passworld.data.DDL;
import passworld.utils.ViewManager;

import java.io.IOException;
import java.util.Objects;

public class PassworldApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Crear la base de datos si no existe
        DDL.createDatabase();

        // Guardar la referencia del primaryStage en el ViewManager
        ViewManager.setPrimaryStage(stage);

        // Cargar el SplashScreen
        FXMLLoader splashLoader = new FXMLLoader(getClass().getResource("splash-view.fxml"));
        Parent root = splashLoader.load();

        // Creamos y configuramos el Stage del SplashScreen
        Stage splash = new Stage();
        splash.setScene(new Scene(root));
        splash.initStyle(StageStyle.UNDECORATED);
        splash.setResizable(false);

        // Agregar icon al SplashScreen
        splash.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/passworld/images/app_icon.png")
        )));

        // Enviamos el stage al controlador para cerrarlo desde ahí
        SplashScreenController ctrl = splashLoader.getController();
        ctrl.setSplashStage(splash);

        // Mostrar el splash
        splash.show();
    }

    public static void main(String[] args) {
        launch();
    }
}