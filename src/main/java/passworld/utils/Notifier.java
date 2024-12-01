package passworld.utils;

import javafx.animation.PauseTransition;
import javafx.scene.control.Tooltip;
import javafx.stage.Window;
import javafx.util.Duration;

public class Notifier {

    public static void showNotification(Window window, String message) {
        Tooltip copiedTooltip = new Tooltip(message);
        copiedTooltip.setAutoHide(true);
        copiedTooltip.show(window);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> copiedTooltip.hide());
        pause.play();
    }
}
