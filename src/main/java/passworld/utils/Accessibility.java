package passworld.utils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import passworld.controller.PassworldController;

public class Accessibility {

    // Método para seleccionar todo el texto cuando un campo de texto tenga focus
    public static void setSelectAllOnFocus(TextField textField) {
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                textField.selectAll();  // Selecciona todo el texto cuando recibe el foco
            }
        });
    }

    public static void setShowListOnFocus(ComboBox<String> comboBox) {
        // Muestra la lista de idiomas cuando se hace focus en el ComboBox
        comboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {  // Si el ComboBox obtiene el focus
                comboBox.show();  // Despliega la lista de opciones
            }
        });
    }

    // Atajo de teclado para copiar el contenido y mostrar notificación
    public static void addCopyShortcut(TextField textField, PassworldController controller) {
        textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            // Si el SO es macOS -> CMD + C
            // Si el SO es otro -> CTRL + C
            if (isMac()) {
                if (event.getCode() == KeyCode.C && event.isMetaDown()) {  // CMD + C
                    copyToClipboard(textField);  // Copia el texto
                    controller.notifyPasswordCopiedToClipboard(); // Muestra la notificación
                    event.consume();  // Evita que el evento se propague
                }
            } else {
                if (event.getCode() == KeyCode.C && event.isControlDown()) {  // CTRL + C
                    copyToClipboard(textField);  // Copia el texto
                    controller.notifyPasswordCopiedToClipboard(); // Muestra la notificación
                    event.consume();  // Evita que el evento se propague
                }
            }
        });
    }

    // Método para copiar el texto al portapapeles
    public static void copyToClipboard(TextField textField) {
        String text = textField.getText();  // Obtiene el texto del TextField
        ClipboardContent content = new ClipboardContent();
        content.putString(text);  // Coloca el texto en el portapapeles
        Clipboard.getSystemClipboard().setContent(content);  // Establece el contenido del portapapeles
    }


    // Método para verificar si el sistema operativo es macOS
    private static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
}
