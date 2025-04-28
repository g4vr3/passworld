module passworld {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;
    requires jbcrypt;
    requires java.sql;
    requires org.json;
    requires java.prefs;
    requires java.net.http;


    opens passworld to javafx.fxml;
    exports passworld;
    exports passworld.utils;
    opens passworld.utils to javafx.fxml;
    exports passworld.controller;
    opens passworld.controller to javafx.fxml;
    exports passworld.data;
    opens passworld.data to javafx.fxml;
    exports passworld.service;
    opens passworld.service to javafx.fxml;
}