package passworld;

import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import passworld.controller.SplashScreenController;
import passworld.data.DDL;
import passworld.utils.ViewManager;

import java.io.IOException;

public class PassworldApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        DDL.createDatabase();
        ViewManager.setPrimaryStage(stage);

        FXMLLoader splashLoader = new FXMLLoader(getClass().getResource("splash-view.fxml"));
        Parent root = splashLoader.load();
        Stage splash = new Stage();
        splash.setScene(new Scene(root));
        SplashScreenController ctrl = splashLoader.getController();
        ctrl.setSplashStage(splash);
        splash.show();
    }

    public static void main(String[] args) {
        launch();
    }
}