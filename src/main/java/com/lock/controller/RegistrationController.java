package com.lock.controller;

import java.io.IOException;

import com.lock.App;
import com.lock.util.DatabaseUtil;
import com.lock.util.SecureUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistrationController {
    private Alert alert = new Alert(Alert.AlertType.INFORMATION);

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private void register() throws IOException {
        setupSessionFactory();
    }

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }


    private void setupSessionFactory(){
        try {

            if (!confirmPasswordField.getText().equals(passwordField.getText())){
                alert.setContentText("Passwords Do Not Match");
                alert.showAndWait();
                return;
            }

            boolean isSecure = SecureUtil.setNewAppPassword(passwordField.getText().toCharArray());

            if(!isSecure){
                alert.setContentText("Insecure Password");
                alert.showAndWait();
                return;
            }

            DatabaseUtil.initHibernate(usernameField.getText());
            switchToLogin();

        } catch (Exception e) {
            System.err.println("Error while initializing the SessionFactory: " + e.getMessage());
            e.printStackTrace();

            alert.setContentText("Error Creating Account");
            alert.showAndWait();
        } finally {
            passwordField.clear();
            usernameField.clear();
            confirmPasswordField.clear();
        }
    }

}
