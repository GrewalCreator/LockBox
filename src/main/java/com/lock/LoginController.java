package com.lock;

import java.io.IOException;
import javafx.fxml.FXML;

public class LoginController {

    @FXML
    private void login() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void switchToRegistration() throws IOException {
        App.setRoot("register");
    }

    
}
