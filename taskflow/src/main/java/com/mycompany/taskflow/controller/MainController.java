package com.mycompany.taskflow.controller;

import com.mycompany.taskflow.model.DatabaseModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class MainController {
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        try {
            DatabaseModel.connect();
            statusLabel.setText("Połączono z bazą danych!");
        } catch (SQLException e) {
            statusLabel.setText("Błąd połączenia: " + e.getMessage());
        }
    }

    @FXML
    public void handleLogout() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/LoginView.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.setScene(new Scene(root, 300, 250));
        stage.show();
    }
}