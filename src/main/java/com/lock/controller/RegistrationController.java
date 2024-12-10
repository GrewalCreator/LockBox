package com.lock.controller;

import java.io.IOException;
import java.util.Arrays;

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
    private void register() throws IOException {
        setupSessionFactory();
        switchToLogin();
    }

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }


    private void setupSessionFactory(){
        try {
            boolean isSecure = SecureUtil.setAppPassword(passwordField.getText().toCharArray());

            if(!isSecure){
                alert.setContentText("Invalid Username");
                alert.showAndWait();

                return;
            }
        } catch (Exception e) {
            System.err.println("Error while initializing the SessionFactory: " + e.getMessage());
            e.printStackTrace();

            alert.setContentText("Error Creating Account");
            alert.showAndWait();
        } finally {
            passwordField.clear();
            usernameField.clear();
        }
    }

}
