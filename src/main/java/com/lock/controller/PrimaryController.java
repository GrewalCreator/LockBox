package com.lock.controller;

import java.io.IOException;

import com.lock.App;

import javafx.fxml.FXML;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("login");
    }
}
