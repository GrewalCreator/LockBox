package com.lock;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private final String appName = "LockBox";
    private DatabaseUtil databaseUtil = new DatabaseUtil(this.appName);

    @Override
    public void start(Stage stage) throws IOException {
        Connection conn = databaseUtil.initDatabase(); // Add Error checking later
        scene = new Scene(loadFXML("login"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }




}