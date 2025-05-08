package com.mycompany.taskflow.controller.user;

import com.mycompany.taskflow.model.UserSession;
import com.mycompany.taskflow.model.user.Dashboard;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML
    public Label welcomeLabel;
    @FXML
    public ListView<String> taskListView;
    @FXML
    public ProgressBar progressBar;
    @FXML
    public Label progressLabel;
    @FXML
    public Label notificationsCountLabel;
    @FXML
    public ListView<String> notificationsListView;

    public String userName;
    public Integer userId;
    public boolean isInitialized = false;
    private final ObservableList<String> tasks = FXCollections.observableArrayList();
    public ObservableList<String> notificationItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println("DashboardController initialized");
        isInitialized = true;
        UserSession currentSession = UserSession.getInstance();
        if (currentSession != null) {
            this.userId = currentSession.getUserId();
            this.userName = currentSession.getFirstName();
            updateView();
        } else {
            System.err.println("Brak aktywnej sesji użytkownika.");
        }
        notificationsListView.setItems(notificationItems);
    }

    public void setUserData(int id, String name) {
        System.out.println("setUserData wywołane: " + id + ", " + name);
        this.userId = id;
        this.userName = name;

        if (isInitialized) {
            Platform.runLater(this::updateView);
        }
    }

    public void updateView() {
        if (userId != null) {
            initializeWelcomeLabel();
            loadTasksForNextWeek(userId);
            updateProgressBar(userId);
            loadNotificationsCount(userId);
            loadNotifications(userId);
        } else {
            System.err.println("Nie można zaktualizować widoku, brak ID użytkownika.");
        }
    }

    public void initializeWelcomeLabel() {
        if (userName == null) {
            welcomeLabel.setText("Witaj użytkowniku!");
        } else {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            welcomeLabel.setText("Witaj " + userName + "! " + now.format(formatter));
        }
    }

    public void loadTasksForNextWeek(int userId) {
        tasks.clear();
        List<Dashboard.Deadline> userDeadlines = Dashboard.loadUserDeadlines(userId);
        if (userDeadlines != null) {
            tasks.addAll(userDeadlines.stream()
                    .map(deadline -> deadline.getName() + " - Termin: " + deadline.getDeadline())
                    .collect(Collectors.toList()));
        } else {
            System.err.println("Nie udało się pobrać terminów zadań dla użytkownika o ID: " + userId);
        }
        taskListView.setItems(tasks);
    }

    public void updateProgressBar(int userId) {
        double progress = Dashboard.calculateUserProgress(userId);
        progressBar.setProgress(progress);
        progressLabel.setText(String.format("%.0f%%", progress * 100));
    }

    public void loadNotificationsCount(int userId) {
        int notificationsCount = Dashboard.getUserNotificationsCount(userId);
        notificationsCountLabel.setText("(" + notificationsCount + ")");
    }

    public void loadNotifications(int userId) {
        notificationItems.clear(); // Clear existing items

        List<String> userNotifications = Dashboard.getUserNotifications(userId);
        System.out.println("Loaded notifications: " + userNotifications); // Debug

        if (userNotifications == null || userNotifications.isEmpty()) {
            System.out.println("No notifications - adding empty state"); // Debug
            notificationItems.add("Brak powiadomień.");
        } else {
            System.out.println("Adding " + userNotifications.size() + " notifications"); // Debug
            notificationItems.addAll(userNotifications.stream()
                    .limit(10)
                    .collect(Collectors.toList()));
        }

        System.out.println("Current notificationItems: " + notificationItems); // Debug
    }

    public Node getView() {
        return welcomeLabel.getParent();
    }
}