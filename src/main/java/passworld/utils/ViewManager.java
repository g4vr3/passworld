package passworld.utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import passworld.controller.SplashScreenController;

import java.io.IOException;
import java.util.Objects;

public class ViewManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(false); // Deshabilitar el redimensionamiento de la ventana

        // Asegurar que la aplicación se cierre completamente al cerrar la ventana
        primaryStage.setOnCloseRequest(_ -> {
            Platform.exit(); // Detiene todos los hilos de JavaFX
            System.exit(0); // Finaliza
            //Detiene todos los executores
            SplashScreenController.getExecutorService().shutdownNow();

        });
    }
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void changeView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            if (primaryStage.getScene() == null) {
                // Si no hay Scene, la creamos
                primaryStage.setScene(new Scene(root));
            } else {
                // Si ya existe, solo cambiamos el root
                primaryStage.getScene().setRoot(root);
            }
            primaryStage.getIcons().add(new Image(Objects.requireNonNull(ViewManager.class.getResourceAsStream("/passworld/images/app_icon.png"))));
            primaryStage.setTitle(title);
            primaryStage.show(); // Aseguramos que esté visible

        } catch (IOException e) {
            System.err.println("Error loading FXML file: " + e.getMessage());
            LogUtils.LOGGER.severe("Error loading FXML file: " + fxmlPath + "/n" + e);
        }
    }

    public static void setBackButton(Button backButton) {
        // Configurar el icono y el estilo del botón de volver
        Image ltIcon = new Image(Objects.requireNonNull(ViewManager.class.getResource("/passworld/images/lt_icon.png")).toExternalForm());
        ImageView ltImageView = new ImageView(ltIcon);
        ThemeManager.applyThemeToImage(ltImageView);
        ltImageView.getStyleClass().add("icon");
        backButton.setGraphic(ltImageView);

        backButton.getStyleClass().add("icon-button");
    }
}