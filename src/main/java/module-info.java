module g4vr3.passworld {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens g4vr3.passworld to javafx.fxml;
    exports g4vr3.passworld;
}