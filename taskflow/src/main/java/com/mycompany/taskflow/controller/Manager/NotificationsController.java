package com.mycompany.taskflow.controller.Manager;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.Manager.Notification;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationsController {

    @FXML
    private ListView<Notification> latestNotificationsListView;
    @FXML
    private ListView<Notification> unreadNotificationsListView;
    @FXML
    private ListView<Notification> readNotificationsListView;
    @FXML
    private ListView<Notification> deletedNotificationsListView;
    @FXML
    private Button markAsReadButton;
    @FXML
    private Button deleteSelectedButton;
    @FXML
    private Button restoreSelectedButton;

    private ObservableList<Notification> latestNotifications = FXCollections.observableArrayList();
    private ObservableList<Notification> unreadNotifications = FXCollections.observableArrayList();
    private ObservableList<Notification> readNotifications = FXCollections.observableArrayList();
    private ObservableList<Notification> deletedNotifications = FXCollections.observableArrayList();

    public void initialize() {
        // Enable multiple selection
        latestNotificationsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        unreadNotificationsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        readNotificationsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        deletedNotificationsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Set up list views
        latestNotificationsListView.setItems(latestNotifications);
        unreadNotificationsListView.setItems(unreadNotifications);
        readNotificationsListView.setItems(readNotifications);
        deletedNotificationsListView.setItems(deletedNotifications);

        // Set cell factory to display notification content
        latestNotificationsListView.setCellFactory(lv -> new NotificationListCell());
        unreadNotificationsListView.setCellFactory(lv -> new NotificationListCell());
        readNotificationsListView.setCellFactory(lv -> new NotificationListCell());
        deletedNotificationsListView.setCellFactory(lv -> new NotificationListCell());

        // Load notifications from the database
        loadDatabaseNotifications();
    }

    private void loadDatabaseNotifications() {
        loadLatestNotifications();
        loadUnreadNotifications();
        loadReadNotifications();
    }

    private void loadLatestNotifications() {
        latestNotifications.clear();
        String sql = "SELECT id, user_id, task_id, subtask_id, type, content, is_read, created_at FROM notification ORDER BY created_at DESC LIMIT 10";
        loadNotifications(sql, latestNotifications);
    }

    private void loadUnreadNotifications() {
        unreadNotifications.clear();
        String sql = "SELECT id, user_id, task_id, subtask_id, type, content, is_read, created_at FROM notification WHERE is_read = FALSE";
        loadNotifications(sql, unreadNotifications);
    }

    private void loadReadNotifications() {
        readNotifications.clear();
        String sql = "SELECT id, user_id, task_id, subtask_id, type, content, is_read, created_at FROM notification WHERE is_read = TRUE";
        loadNotifications(sql, readNotifications);
    }

    private void loadNotifications(String sql, ObservableList<Notification> targetList) {
        try (Connection conn = DatabaseModel.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Notification notification = createNotificationFromResultSet(rs);
                // Sprawdź czy powiadomienie nie jest już w usuniętych
                if (!isNotificationDeleted(notification)) {
                    targetList.add(notification);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error
        }
    }

    private boolean isNotificationDeleted(Notification notification) {
        return deletedNotifications.stream()
                .anyMatch(n -> n.getId() == notification.getId());
    }

    private Notification createNotificationFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        Integer taskId = rs.getObject("task_id") != null ? rs.getInt("task_id") : null;
        Integer subtaskId = rs.getObject("subtask_id") != null ? rs.getInt("subtask_id") : null;
        String type = rs.getString("type");
        String content = rs.getString("content");
        boolean isRead = rs.getBoolean("is_read");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        return new Notification(id, userId, taskId, subtaskId, type, content, isRead, createdAt);
    }

    @FXML
    private void markSelectedAsRead(ActionEvent event) {
        List<Notification> selectedNotifications = new ArrayList<>();
        selectedNotifications.addAll(latestNotificationsListView.getSelectionModel().getSelectedItems());
        selectedNotifications.addAll(unreadNotificationsListView.getSelectionModel().getSelectedItems());
        selectedNotifications.addAll(readNotificationsListView.getSelectionModel().getSelectedItems());

        if (selectedNotifications.isEmpty()) {
            return;
        }

        String sql = "UPDATE notification SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DatabaseModel.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Notification notification : selectedNotifications) {
                pstmt.setInt(1, notification.getId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();

            // Refresh the lists
            loadDatabaseNotifications();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error
        }
    }

    @FXML
    private void deleteSelected(ActionEvent event) {
        List<Notification> selectedNotifications = new ArrayList<>();
        selectedNotifications.addAll(latestNotificationsListView.getSelectionModel().getSelectedItems());
        selectedNotifications.addAll(unreadNotificationsListView.getSelectionModel().getSelectedItems());
        selectedNotifications.addAll(readNotificationsListView.getSelectionModel().getSelectedItems());

        if (selectedNotifications.isEmpty()) {
            return;
        }

        // Przenieś do listy usuniętych
        deletedNotifications.addAll(selectedNotifications);

        // Usuń z pozostałych list
        latestNotifications.removeAll(selectedNotifications);
        unreadNotifications.removeAll(selectedNotifications);
        readNotifications.removeAll(selectedNotifications);
    }

    @FXML
    private void restoreSelected(ActionEvent event) {
        List<Notification> selectedNotifications = new ArrayList<>();
        selectedNotifications.addAll(deletedNotificationsListView.getSelectionModel().getSelectedItems());

        if (selectedNotifications.isEmpty()) {
            return;
        }

        // Usuń z listy usuniętych
        deletedNotifications.removeAll(selectedNotifications);

        // Załaduj ponownie z bazy danych, aby pokazać w odpowiednich listach
        loadDatabaseNotifications();
    }
}