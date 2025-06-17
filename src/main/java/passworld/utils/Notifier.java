package passworld.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class Notifier {
    
    // Lista para gestionar popups activos y evitar acumulación
    private static final java.util.List<Popup> activePopups = new java.util.ArrayList<>();

    public static void showNotification(Window window, String message) {
        // Limpiar popups anteriores para evitar superposición
        clearPreviousNotifications();

        // Crear un Popup en lugar de un Tooltip para evitar bloqueos de la UI
        Popup popup = new Popup();
        popup.setAutoHide(false);
        popup.setAutoFix(false);
        popup.setHideOnEscape(false);
        popup.setConsumeAutoHidingEvents(false);

        // Crear el contenido de la notificación
        Label messageLabel = new Label(message);
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        messageLabel.getStyleClass().add("notification-label");

        StackPane container = new StackPane();
        container.getChildren().add(messageLabel);
        container.setPadding(new Insets(10, 15, 10, 15));
        container.setBackground(new Background(new BackgroundFill(
                Color.rgb(50, 50, 50, 0.9),
                new CornerRadii(8),
                Insets.EMPTY
        )));
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("notification-container");

        popup.getContent().add(container);
        activePopups.add(popup);

        // Mostrar el popup siempre centrado en la ventana
        if (window != null && window.getScene() != null) {
            double centerX = window.getX() + window.getWidth() / 2 - container.getWidth() / 2;
            double centerY = window.getY() + window.getHeight() / 2 - container.getHeight() / 2;

            popup.show(window, centerX, centerY);
        } else {
            popup.show(window);
        }

        // Animación de entrada (fade in)
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), container);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // Programar el ocultado automático después de 2.5 segundos
        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        pause.setOnFinished(_ -> hideNotification(popup, container));
        pause.play();
    }
    
    /**
     * Oculta una notificación específica con animación
     */
    private static void hideNotification(Popup popup, StackPane container) {
        if (popup.isShowing()) {
            // Animación de salida (fade out)
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), container);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(__ -> {
                popup.hide();
                activePopups.remove(popup);
            });
            fadeOut.play();
        } else {
            activePopups.remove(popup);
        }
    }
    
    /**
     * Limpia todas las notificaciones activas anteriores
     */
    private static void clearPreviousNotifications() {
        // Crear una copia de la lista para evitar ConcurrentModificationException
        java.util.List<Popup> popupsToHide = new java.util.ArrayList<>(activePopups);
        
        for (Popup popup : popupsToHide) {
            if (popup.isShowing()) {
                popup.hide();
            }
        }
        activePopups.clear();
    }
    
    /**
     * Método para limpiar todas las notificaciones inmediatamente (útil al cambiar de vista)
     */
    public static void clearAllNotifications() {
        clearPreviousNotifications();
    }
}
