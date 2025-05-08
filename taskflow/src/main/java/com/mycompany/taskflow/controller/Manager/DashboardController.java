package com.mycompany.taskflow.controller.Manager;

import com.mycompany.taskflow.model.UserSession;
import com.mycompany.taskflow.model.Manager.Subtask;
import com.mycompany.taskflow.model.Manager.Task;
import com.mycompany.taskflow.model.Manager.Notification;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ListView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private Label welcomeLabel;

    // Tabele zadań i podzadań
    @FXML private TableView<Task> recentTasksTableView;
    @FXML private TableColumn<Task, String> recentTaskNameColumn;
    @FXML private TableColumn<Task, LocalDate> recentTaskDueDateColumn;

    @FXML private TableView<Subtask> recentSubtasksTableView;
    @FXML private TableColumn<Subtask, String> recentSubtaskNameColumn;
    @FXML private TableColumn<Subtask, LocalDate> recentSubtaskDueDateColumn;

    @FXML private TableView<Task> upcomingTasksTableView;
    @FXML private TableColumn<Task, String> upcomingTaskNameColumn;
    @FXML private TableColumn<Task, LocalDate> upcomingTaskDueDateColumn;

    @FXML private TableView<Subtask> upcomingSubtasksTableView;
    @FXML private TableColumn<Subtask, String> upcomingSubtaskNameColumn;
    @FXML private TableColumn<Subtask, LocalDate> upcomingSubtaskDueDateColumn;

    // Lista powiadomień
    @FXML private ListView<String> notificationsListView;

    private int loggedInUserId;
    private int loggedInTeamId;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        // Pobierz dane zalogowanego użytkownika z sesji
        UserSession userSession = UserSession.getInstance();
        String firstName = userSession.getFirstName();
        String lastName = userSession.getLastName();
        loggedInUserId = userSession.getUserId();
        loggedInTeamId = userSession.getTeamId();

        // Ustaw tekst powitalny
        welcomeLabel.setText("Witaj, " + firstName + " " + lastName + "!");

        // Inicjalizacja tabel
        initializeTables();

        // Pobierz i wyświetl dane
        loadData();
    }

    private void initializeTables() {
        // Konfiguracja kolumn dla tabel zadań
        recentTaskNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        recentTaskDueDateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDueDate()));

        recentSubtaskNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        recentSubtaskDueDateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDueDate()));

        upcomingTaskNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        upcomingTaskDueDateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDueDate()));

        upcomingSubtaskNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        upcomingSubtaskDueDateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDueDate()));
    }

    private void loadData() {
        try {
            // Ładowanie ostatnich zadań i podzadań
            List<Task> recentTasks = Task.getRecentTasksForTeam(loggedInTeamId);
            List<Subtask> recentSubtasks = Subtask.getRecentSubtasksForTeam(loggedInTeamId);

            // Ładowanie nadchodzących zadań i podzadań
            List<Task> upcomingTasks = Task.getUpcomingTasksForTeam(loggedInTeamId);
            List<Subtask> upcomingSubtasks = Subtask.getUpcomingSubtasksForTeam(loggedInTeamId);

            // Ładowanie powiadomień
            List<Notification> notifications = Notification.getLatestNotificationsForUser(loggedInUserId, 5);

            // Aktualizacja widoków
            recentTasksTableView.getItems().setAll(recentTasks);
            recentSubtasksTableView.getItems().setAll(recentSubtasks);
            upcomingTasksTableView.getItems().setAll(upcomingTasks);
            upcomingSubtasksTableView.getItems().setAll(upcomingSubtasks);

            // Formatowanie i wyświetlanie powiadomień
            notificationsListView.getItems().clear();
            for (Notification notification : notifications) {
                String formattedNotification = String.format("[%s] %s: %s",
                        notification.getCreatedAt().format(dateFormatter),
                        notification.getType(),
                        notification.getContent());
                notificationsListView.getItems().add(formattedNotification);
            }

        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}