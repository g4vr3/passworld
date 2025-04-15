package passworld.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void changeView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            primaryStage.getScene().setRoot(root);
            primaryStage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}