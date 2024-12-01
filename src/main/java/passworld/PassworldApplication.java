package passworld;

import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class PassworldApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Cargar splash screen al inicio
        FXMLLoader splashLoader = new FXMLLoader(PassworldApplication.class.getResource("splash-view.fxml"));
        Scene splashScene = new Scene(splashLoader.load());
        Stage splashStage = new Stage();
        splashStage.setScene(splashScene);
        splashStage.setTitle("passworld");
        splashStage.initStyle(StageStyle.UNDECORATED);

        // Desactiva el cierre por teclado
        splashStage.setOnCloseRequest(Event::consume);
        splashStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}