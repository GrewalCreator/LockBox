package com.lock;

import java.io.IOException;

import javafx.fxml.FXML;

public class RegistrationController {
    @FXML
    private void register() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }
}
