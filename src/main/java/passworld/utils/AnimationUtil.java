package passworld.utils;

import javafx.animation.TranslateTransition;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class AnimationUtil {
    // Agitar un campo de texto
    public static void shakeField(TextField field) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), field);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }
}
