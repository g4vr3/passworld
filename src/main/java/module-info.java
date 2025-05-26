module passworld {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;
    requires jbcrypt;
    requires java.sql;
    requires okhttp3;
    requires org.json;
    requires com.fasterxml.jackson.databind;
    requires jdk.jshell;
    requires java.net.http;
    requires org.apache.commons.net;
    requires annotations;


    opens passworld to javafx.fxml;

    exports passworld;
    exports passworld.utils;
    opens passworld.utils;
    exports passworld.controller;
    opens passworld.controller to javafx.fxml;
    exports passworld.data;
    opens passworld.data to javafx.fxml;
    exports passworld.service;
    opens passworld.service to javafx.fxml;
    exports passworld.data.apiclients;
    opens passworld.data.apiclients;
    exports passworld.data.session;
    opens passworld.data.session;
    exports passworld.data.exceptions;
    opens passworld.data.exceptions;
}