package passworld.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import passworld.data.session.PersistentSessionManager;
import passworld.data.sync.SyncHandler;
import passworld.utils.LogUtils;
import passworld.utils.PasswordEvaluator;
import passworld.utils.TimeSyncManager;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SplashScreenController {
    private volatile boolean splashClosed = false;
    private boolean lastOnlineStatus = false;
    private static ScheduledExecutorService connectionMonitorExecutor;

    @FXML
    private MediaView mediaView;
    @FXML
    private ProgressBar loadingBar;

    private MediaPlayer mediaPlayer;
    private Stage splashStage;

    public void setSplashStage(Stage stage) {
        this.splashStage = stage;
    }

    @FXML
    public void initialize() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            ImageView imageView = new ImageView(new Image(
                    Objects.requireNonNull(getClass().getResource("/passworld/images/passworld_logo.png")).toExternalForm()
            ));
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);
            imageView.setFitWidth(300);
            imageView.setFitHeight(300);

            Parent parent = mediaView.getParent();
            if (parent instanceof Pane pane) {
                int index = pane.getChildren().indexOf(mediaView);
                if (index != -1) {
                    pane.getChildren().set(index, imageView);
                } else {
                    pane.getChildren().add(imageView);
                    mediaView.setVisible(false);
                }
            } else {
                LogUtils.LOGGER.warning("SplashScreenController called with unsupported parent");
                mediaView.setVisible(false);
            }
        } else {
            String videoPath = Objects.requireNonNull(
                    getClass().getResource("/passworld/videos/splash_screen.mp4")
            ).toExternalForm();
            mediaPlayer = new MediaPlayer(new Media(videoPath));
            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        }

        // Crear un Task para cargar el Trie en segundo plano
        Task<Void> loadTrieTask = new Task<>() {
            @Override
            protected Void call() {
                PasswordEvaluator.loadCommonWords(); // Cargar Trie
                return null;
            }
        };

        Timeline progressTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(loadingBar.progressProperty(), 0.1)),
                new KeyFrame(Duration.seconds(3), new KeyValue(loadingBar.progressProperty(), 0.9))
        );
        progressTimeline.setCycleCount(1);
        progressTimeline.play();

        loadTrieTask.setOnSucceeded(_ -> {
            progressTimeline.stop();
            loadingBar.setProgress(1.0);

            Timeline delay = new Timeline(new KeyFrame(Duration.seconds(0.1), _ -> onSplashFinished()));
            delay.play();
            LogUtils.LOGGER.info("Trie loaded successfully");
        });

        loadTrieTask.setOnFailed(_ -> {
            if (mediaPlayer != null) mediaPlayer.stop();
            LogUtils.LOGGER.severe("Failed to load trie file");
            throw new RuntimeException("Error al cargar el Trie", loadTrieTask.getException());
        });

        Thread thread = new Thread(loadTrieTask);
        thread.setDaemon(true);
        thread.start();

        connectionMonitorExecutor = Executors.newSingleThreadScheduledExecutor();
        connectionMonitorExecutor.scheduleAtFixedRate(createConnectionMonitorTask(), 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Se dispara al terminar el timeline de la Splash.
     */
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
        boolean online = PersistentSessionManager.hasInternet();
        boolean session = PersistentSessionManager.tokenSavedLocally();

        if (online) {
            TimeSyncManager.syncTimeWithUtcServer();
            if (session) {
                // Si hay sesión y conexión, refrescamos el token
                PersistentSessionManager.refreshToken();
                SyncHandler.startTokenRefreshThread();
                VaultProtectionController.showView();
            } else {
                AuthController.showView();
            }
        } else {
            PersistentSessionManager.setUserId();
            VaultProtectionController.showView();
        }

        splashClosed = true;
        lastOnlineStatus = online;
    }

    /**
     * Tarea periódica que solo reacciona tras cerrar la Splash
     */
    private Runnable createConnectionMonitorTask() {
        return () -> {
            if (!splashClosed) return;  // aún estamos en la Splash, ignorar

            boolean online = PersistentSessionManager.hasInternet();
            // reconexión
            if (!lastOnlineStatus && online) {
                Platform.runLater(() -> {
                    boolean session = PersistentSessionManager.tokenSavedLocally();
                    if (session) {
                        LogUtils.LOGGER.info("Session found after reconnection");
                        // Si hay sesión y conexión, refrescamos el token
                        PersistentSessionManager.refreshToken();
                        SyncHandler.startTokenRefreshThread();

                    } else {
                        LogUtils.LOGGER.warning("No session found after reconnection");
                        AuthController.showView();
                    }
                });
            }
            // desconexión
            else if (lastOnlineStatus && !online) {
                LogUtils.LOGGER.warning("Lost connection");
                System.out.println("Se perdió la conexión");
                // podrías mostrar un banner o notificación aquí
            }
            lastOnlineStatus = online;
        };
    }

    public static ScheduledExecutorService getExecutorService() {
        return connectionMonitorExecutor;
    }
}