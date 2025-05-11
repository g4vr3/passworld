package passworld.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import passworld.data.session.PersistentSessionManager;
import passworld.utils.ViewManager;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SplashScreenController {
    private volatile boolean splashClosed = false;      // Indica si la Splash ya se cerró
    private boolean lastOnlineStatus = false;           // Para detectar cambios de conexión

    @FXML
    private MediaView mediaView;
    @FXML
    private ProgressBar loadingBar;

    private MediaPlayer mediaPlayer;
    private Stage splashStage;  // Inyectado desde tu Application principal

    /** Llamar desde tu Application tras cargar el FXMLLoader:
     *   controller.setSplashStage(primaryStageSplash);
     */
    public void setSplashStage(Stage stage) {
        this.splashStage = stage;
    }

    @FXML
    public void initialize() {
        // 1) Reproducir vídeo + barra de progreso
        String videoPath = Objects.requireNonNull(
                getClass().getResource("/passworld/videos/splash_screen.mp4")
        ).toExternalForm();
        mediaPlayer = new MediaPlayer(new Media(videoPath));
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.setAutoPlay(true);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,     new KeyValue(loadingBar.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(1), new KeyValue(loadingBar.progressProperty(), 1))
        );
        timeline.setOnFinished(e -> onSplashFinished());
        timeline.play();

        // 2) Programar el monitor de sesión / conexión
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(createConnectionMonitorTask(), 0, 10, TimeUnit.SECONDS);
    }

    /** Se dispara al terminar el timeline de la Splash. */
    private void onSplashFinished() {
        // 1. Detener vídeo y cerrar Splash
        if (mediaPlayer != null) mediaPlayer.stop();
        if (splashStage != null) {
            splashStage.close();
        } else if (mediaView.getScene() != null) {
            Stage st = (Stage) mediaView.getScene().getWindow();
            st.close();
        }
        // 2. Enrutamiento inicial
        boolean online  = PersistentSessionManager.hasInternet();
        boolean session = PersistentSessionManager.tokenSavedLocally();

        if (online) {
            if (session) {
                // Si hay sesión y conexión, refrescamos el token
                PersistentSessionManager.refreshToken();
                VaultProtectionController.showView();
            } else {
                AuthController.showView();
            }
        } else {
            // offline: abrimos Vault en modo solo local
            VaultProtectionController.showView();
        }

        splashClosed = true;
        lastOnlineStatus = online;
    }

    /** Tarea periódica que solo reacciona tras cerrar la Splash */
    private Runnable createConnectionMonitorTask() {
        return () -> {
            if (!splashClosed) return;  // aún estamos en la Splash, ignorar

            boolean online = PersistentSessionManager.hasInternet();
            // reconexión
            if (!lastOnlineStatus && online) {
                Platform.runLater(() -> {
                    boolean session = PersistentSessionManager.tokenSavedLocally();
                    if (session) {
                        System.out.println("Sesión encontrada tras reconexión");

                    } else {
                        System.out.println("Sin sesión tras reconexión, redirigiendo a login");
                        AuthController.showView();
                    }
                });
            }
            // desconexión
            else if (lastOnlineStatus && !online) {
                System.out.println("Se perdió la conexión");
                // podrías mostrar un banner o notificación aquí
            }
            lastOnlineStatus = online;
        };
    }
}