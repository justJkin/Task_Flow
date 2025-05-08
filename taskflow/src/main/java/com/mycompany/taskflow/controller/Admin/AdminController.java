package com.mycompany.taskflow.controller.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        loadDashboardView();
    }

    @FXML
    public void loadDashboardView() {
        loadView("/com/mycompany/taskflow/view/Admin/DashboardView.fxml");
    }

    @FXML
    public void loadAdminPanel() {
        loadView("/com/mycompany/taskflow/view/Admin/AdminPanelView.fxml");
    }

    @FXML
    public void loadCalendarView() {
        loadView("/com/mycompany/taskflow/view/Admin/CalendarView.fxml");
    }

    @FXML
    public void loadNotificationsView() {
        loadView("/com/mycompany/taskflow/view/Admin/NotificationsView.fxml");
    }

    @FXML
    public void loadReportsView() {
        loadView("/com/mycompany/taskflow/view/Admin/ReportsView.fxml");
    }

    @FXML
    public void logout() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/LoginView.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        stage.setScene(new Scene(root, 300, 250));
        stage.setTitle("TaskFlow - Logowanie");
        stage.show();
        System.out.println("Wylogowano");
    }

    private void loadView(String fxmlPath) {
        try {
            if (!fxmlPath.startsWith("/")) {
                System.err.println("Warning: FXML path does not start with '/': " + fxmlPath);
            }

            java.net.URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("Error: FXML resource not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();
            contentArea.getChildren().setAll(view);

            Scene scene = contentArea.getScene();
            if (scene != null) {
                String cssPath = "/com/mycompany/taskflow/styles.css";
                java.net.URL cssUrl = getClass().getResource(cssPath);
                if (cssUrl != null) {
                    String cssExternalForm = cssUrl.toExternalForm();
                    if (!scene.getStylesheets().contains(cssExternalForm)) {
                        scene.getStylesheets().add(cssExternalForm);
                    }
                } else {
                    System.err.println("Warning: Stylesheet not found: " + cssPath);
                }
            }

        } catch (IOException e) {
            System.err.println("Error loading FXML view: " + fxmlPath);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error loading view: " + fxmlPath);
            e.printStackTrace();
        }
    }
}