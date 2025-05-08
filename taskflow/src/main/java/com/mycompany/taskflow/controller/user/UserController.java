package com.mycompany.taskflow.controller.user;

import com.mycompany.taskflow.model.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class UserController {

    @FXML
    private StackPane contentArea;

    public DashboardController dashboardController;
    public Node dashboardView;

    @FXML
    public void initialize() {
        UserSession currentSession = UserSession.getInstance();
        if (currentSession != null) {
            loadInitialDashboard();
        }
    }
    public void loadInitialDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/User/DashboardView.fxml"));
            dashboardView = loader.load();
            this.dashboardController = loader.getController();

            UserSession currentSession = UserSession.getInstance();
            if (currentSession != null) {
                dashboardController.setUserData(currentSession.getUserId(), currentSession.getFirstName());
            }

            contentArea.getChildren().setAll(dashboardView);
            applyStyles();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void loadDashboardView() {
        UserSession currentSession = UserSession.getInstance();
        if (currentSession != null) {
            if (dashboardController != null) {
                dashboardController.setUserData(currentSession.getUserId(), currentSession.getFirstName());
                contentArea.getChildren().setAll(dashboardView);
            } else {
                loadInitialDashboard();
            }
        } else {
            loadLoginView();
        }
    }

    @FXML
    public void loadBoardView() {
        loadView("/com/mycompany/taskflow/view/User/BoardView.fxml");
    }

    @FXML
    public void loadCalendarView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/User/CalendarView.fxml"));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
            applyStyles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadNotificationsView() {
        loadView("/com/mycompany/taskflow/view/User/NotificationsView.fxml");
    }

    @FXML
    public void loadTasksView() {
        loadView("/com/mycompany/taskflow/view/User/TasksView.fxml");
    }

    @FXML
    public void loadReportView() {
        loadView("/com/mycompany/taskflow/view/User/ReportView.fxml");
    }

    @FXML
    public void logout() throws IOException {
        UserSession.clearSession();
        loadLoginView();
    }

    public void loadLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 300, 250));
            stage.setTitle("TaskFlow - Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadView(String fxmlPath) {
        UserSession currentSession = UserSession.getInstance();
        if (currentSession != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Node view = loader.load();
                contentArea.getChildren().setAll(view);
                applyStyles();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            loadLoginView();
        }
    }

    public void applyStyles() {
        if (contentArea.getScene() != null) {
            contentArea.getScene().getStylesheets().add(getClass().getResource("/com/mycompany/taskflow/styles.css").toExternalForm());
        }
    }
}