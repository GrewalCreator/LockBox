package com.lock;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;

import java.io.IOException;

import org.hibernate.SessionFactory;

import com.lock.util.DatabaseUtil;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static DatabaseUtil databaseUtil = new DatabaseUtil();
    private SessionFactory sessionFactory;


    public static void main(String[] args)
    {
        launch();
    }

//TODO: Install and setup mysql script

    @Override
    public void start(Stage stage) throws IOException
    {
        try{
            this.sessionFactory = databaseUtil.getSessionFactory();
        } catch (Exception e){
            System.err.println("Error while initializing the SessionFactory: " + e.getMessage());
            e.printStackTrace();
        }


        scene = new Scene(loadFXML("login"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception
    {
        super.stop();
        if (databaseUtil != null) {
            databaseUtil.closeSessionFactory();
        }
    }


    protected static void setRoot(String fxml) throws IOException
    {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }






}