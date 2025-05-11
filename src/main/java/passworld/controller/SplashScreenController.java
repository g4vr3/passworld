package passworld.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import passworld.utils.PasswordEvaluator;

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
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Repetir el video indefinidamente

        // Crear un Task para cargar el Trie en segundo plano
        Task<Void> loadTrieTask = new Task<>() {
            @Override
            protected Void call() {
                updateProgress(0.1, 1); // Inicializar progreso con un valor mínimo visible

                // Cargar palabras comunes en el Trie
                PasswordEvaluator.loadCommonWords();

                updateProgress(1, 1); // Completar progreso
                return null;
            }
        };

        // Vincular el progreso del Task al ProgressBar
        loadingBar.progressProperty().bind(loadTrieTask.progressProperty());

        // Cerrar la pantalla de carga cuando el Task termine
        loadTrieTask.setOnSucceeded(event -> {
            // Asegurarse de que la barra de progreso se vea llena antes de cerrar
            Timeline delay = new Timeline(new KeyFrame(Duration.seconds(0.2), e -> closeSplashScreen()));
            delay.play();
        });

        // Manejar errores en caso de fallo
        loadTrieTask.setOnFailed(event -> {
            mediaPlayer.stop();
            throw new RuntimeException("Error al cargar el Trie", loadTrieTask.getException());
        });

        // Ejecutar el Task en un hilo separado
        Thread trieThread = new Thread(loadTrieTask);
        trieThread.setDaemon(true); // Asegurar que el hilo no bloquee la aplicación al cerrarse
        trieThread.start();
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
        AuthController.showView();
    }
}
