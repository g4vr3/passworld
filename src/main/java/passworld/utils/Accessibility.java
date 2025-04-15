package passworld.utils;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import passworld.controller.PassworldController;
import passworld.service.LanguageManager;

import java.util.ResourceBundle;

public class Accessibility {

    // Método para seleccionar todo el texto cuando un campo de texto tenga focus
    public static void setSelectAllOnFocus(TextField textField) {
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                textField.selectAll();  // Selecciona todo el texto cuando recibe el foco
            }
        });
    }

    public static void setShowLanguageListOnFocus(ComboBox<String> comboBox) {
        // Muestra la lista de idiomas cuando se hace focus en el ComboBox
        comboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {  // Si el ComboBox obtiene el focus
                comboBox.show();  // Despliega la lista de opciones
            }
        });
    }

    // Atajo de teclado para copiar el contenido y mostrar notificación
    public static void addCopyShortcut(TextField passwordField, Runnable copyAction) {
        passwordField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            // Verificar si el campo de texto no está vacío
            if (passwordField.getText().isEmpty()) {
                return; // No hacer nada si el campo está vacío
            }

            // Si el SO es macOS -> CMD + C
            // Si el SO es otro -> CTRL + C
            if (isMac()) {
                if (event.getCode() == KeyCode.C && event.isMetaDown()) { // CMD + C
                    copyAction.run();
                    event.consume(); // Evitar la propagación del evento
                }
            } else {
                if (event.getCode() == KeyCode.C && event.isControlDown()) { // CTRL + C
                    copyAction.run();
                    event.consume(); // Evitar la propagación del evento
                }
            }
        });
    }

    // Atajo de teclado para guardar la contraseña
    public static void addSavePasswordShortcut(Scene scene, Runnable saveAction) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (isMac()) {
                // CMD + G (Mac)
                if (event.getCode() == KeyCode.G && event.isMetaDown()) {
                    saveAction.run();
                    event.consume(); // Prevent event propagation
                }
            } else {
                // CTRL + G (Windows/Linux)
                if (event.getCode() == KeyCode.G && event.isControlDown()) {
                    saveAction.run();
                    event.consume(); // Prevent event propagation
                }
            }
        });
    }

    // Método para verificar si el sistema operativo es macOS
    private static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
}
