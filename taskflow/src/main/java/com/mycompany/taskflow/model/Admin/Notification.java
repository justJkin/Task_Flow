package com.mycompany.taskflow.model.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Notification {
    private int id;
    private int userId;
    private Integer taskId;
    private Integer subtaskId;
    private String type;
    private String content;
    private boolean isRead;
    private Timestamp createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }
    public Integer getSubtaskId() { return subtaskId; }
    public void setSubtaskId(Integer subtaskId) { this.subtaskId = subtaskId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public static List<Notification> getAllNotifications() throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT id, user_id, task_id, subtask_id, type, content, is_read, created_at FROM notification ORDER BY created_at DESC";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Notification notification = new Notification();
                notification.setId(resultSet.getInt("id"));
                notification.setUserId(resultSet.getInt("user_id"));
                notification.setTaskId((Integer) resultSet.getObject("task_id"));
                notification.setSubtaskId((Integer) resultSet.getObject("subtask_id"));
                notification.setType(resultSet.getString("type"));
                notification.setContent(resultSet.getString("content"));
                notification.setRead(resultSet.getBoolean("is_read"));
                notification.setCreatedAt(resultSet.getTimestamp("created_at"));
                notifications.add(notification);
            }
        }
        return notifications;
    }

    public static void markAsRead(int notificationId) throws SQLException {
        String query = "UPDATE notification SET is_read = TRUE WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, notificationId);
            preparedStatement.executeUpdate();
        }
    }

    public static void deleteNotification(int notificationId) throws SQLException {
        String query = "DELETE FROM notification WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, notificationId);
            preparedStatement.executeUpdate();
        }
    }

    public static List<String> getRecentNotifications(int limit) {
        List<String> notifications = new ArrayList<>();
        String query = "SELECT content FROM notification ORDER BY created_at DESC LIMIT ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, limit);
            // Zmiana: Dodano blok try-with-resources dla ResultSet
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(resultSet.getString("content"));
                }
            } // ResultSet zostanie automatycznie zamknięty tutaj
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    // Metody do interakcji z bazą danych (inne) można dodać tutaj
}