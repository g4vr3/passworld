package passworld.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class SplashScreenController {

    @FXML
    private MediaView mediaView;
    @FXML
    private ProgressBar loadingBar;

    private MediaPlayer mediaPlayer;


    @FXML
    public void initialize() {
        // Cargar el video de la Splash Screen y reproducirlo en un MediaView
        String videoPath = Objects.requireNonNull(getClass().getResource("/passworld/videos/splash_screen.mp4")).toExternalForm();
        Media media = new Media(videoPath);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        mediaPlayer.setAutoPlay(true); // Reproducir el video automáticamente

        // Crear Timeline para actualizar la barra de progreso gradualmente
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(loadingBar.progressProperty(), 0)), // Empieza en 0
                new KeyFrame(Duration.seconds(3), new KeyValue(loadingBar.progressProperty(), 1)) // En 3 segundos llega al 100%
        );

        timeline.setOnFinished(event -> closeSplashScreen()); // Cerrar la Splash Screen al terminar timeline
        timeline.play();
    }

    // Cierra la ventana de la Splash Screen y muestra la ventana principal
    private void closeSplashScreen() {
        mediaPlayer.stop();  // Detener el video
        Stage splashStage = (Stage) mediaView.getScene().getWindow();
        splashStage.close();  // Cerrar la Splash Screen
        showMainApplication();
    }


    // Método para mostrar la ventana principal
    private void showMainApplication() {
        // Cargar ventana principal y mostrarla
        Stage mainStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/passworld/main-view.fxml"));
        try {
            Scene scene = new Scene(loader.load(), 600, 400);
            mainStage.setTitle("passworld");
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
