package com.mycompany.taskflow.controller.Manager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ManagerMainController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void loadDashboardView() {
        loadView("DashboardView.fxml");
    }

    @FXML
    public void loadManagerPanelView() {
        loadView("ManagerPanelView.fxml");
    }

    @FXML
    public void loadCalendarView() {
        loadView("CalendarView.fxml");
    }

    @FXML
    public void loadNotificationsView() {
        loadView("NotificationsView.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        try {
            System.out.println("Wyczyszczono dane sesji.");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            System.out.println("Wylogowano i powrócono do ekranu logowania.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadTasksView() {
        loadView("TaskManagerView.fxml");
    }

    @FXML
    public void loadRaportyView() {
        loadView("RaportyView.fxml");
    }

    private void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/Manager/" + fxmlFileName));
            Parent view = loader.load(); // Zmieniono na Parent, aby obsłużyć różne typy korzeni

            contentArea.getChildren().setAll(view);
            Object controller = loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}