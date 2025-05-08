package com.mycompany.taskflow.controller.Admin;

import com.mycompany.taskflow.model.Admin.Notification;
import com.mycompany.taskflow.model.Admin.Team;
import com.mycompany.taskflow.model.Admin.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label dateTimeLabel;

    @FXML
    private ListView<String> notificationsListView;

    @FXML
    private Label noNotificationsLabel;

    @FXML
    private TextArea teamsOverviewTextArea;

    @FXML
    private Label noTeamsOverviewLabel;

    private final User userModel = new User();
    private final Notification notificationModel = new Notification();
    private final Team teamModel = new Team();

    public void initialize() {
        // Pobierz ID zalogowanego administratora z sesji
        int adminId = getLoggedInAdminId();

        // Pobierz imię zalogowanego administratora
        User admin = User.getUserById(adminId);
        if (admin != null) {
            welcomeLabel.setText("Witaj, " + admin.getFirstName());
        } else {
            welcomeLabel.setText("Witaj, Admin");
        }

        // Wyświetl aktualną datę i czas
        updateDateTime();

        // Pobierz i wyświetl ostatnie 5 powiadomień z bazy danych
        loadNotifications();

        // Pobierz i wyświetl przegląd zespołów z bazy danych
        loadTeamsOverview();
    }

    private int getLoggedInAdminId() {
        return 1; // Tymczasowa wartość ID
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        dateTimeLabel.setText(now.format(formatter));
    }

    private void loadNotifications() {
        List<String> notifications = Notification.getRecentNotifications(5); // Używamy metody z modelu Notification
        if (notifications == null || notifications.isEmpty()) {
            notificationsListView.setVisible(false);
            noNotificationsLabel.setVisible(true);
        } else {
            notificationsListView.getItems().addAll(notifications);
            notificationsListView.setVisible(true);
            noNotificationsLabel.setVisible(false);
        }
    }

    private void loadTeamsOverview() {
        List<Team> teams = Team.getAllTeams(); // Używamy metody z modelu Team
        if (teams == null || teams.isEmpty()) {
            teamsOverviewTextArea.setVisible(false);
            noTeamsOverviewLabel.setVisible(true);
        } else {
            String teamsOverviewData = teams.stream()
                    .map(Team::getName)
                    .collect(Collectors.joining("\n"));
            teamsOverviewTextArea.setText(teamsOverviewData);
            teamsOverviewTextArea.setVisible(true);
            noTeamsOverviewLabel.setVisible(false);
        }
    }
}