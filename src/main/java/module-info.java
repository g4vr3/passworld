module g4vr3.passworld {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens passworld to javafx.fxml;
    exports passworld;
    exports passworld.utils;
    opens passworld.utils to javafx.fxml;
    exports passworld.controller;
    opens passworld.controller to javafx.fxml;
}