package com.mycompany.taskflow.model.Manager;

import com.mycompany.taskflow.model.DatabaseModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Notification {
    private int id;
    private int userId;
    private Integer taskId;
    private Integer subtaskId; // <-- Pole istnieje
    private String type;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;

    // Konstruktor
    public Notification(int id, int userId, Integer taskId, Integer subtaskId, String type, String content, boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.taskId = taskId;
        this.subtaskId = subtaskId;
        this.type = type;
        this.content = content;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Metoda statyczna pobierająca dane z bazy
    public static List<Notification> getLatestNotificationsForUser(int userId, int limit) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT id, user_id, task_id, subtask_id, type, content, is_read, created_at FROM notification " +
                "WHERE user_id = ? " +
                "ORDER BY created_at DESC " +
                "LIMIT ?";

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int currentUserId = rs.getInt("user_id"); // Pobieramy user_id z ResultSet
                Integer taskId = rs.getObject("task_id") != null ? rs.getInt("task_id") : null;
                Integer subtaskId = rs.getObject("subtask_id") != null ? rs.getInt("subtask_id") : null;
                String type = rs.getString("type");
                String content = rs.getString("content");
                boolean isRead = rs.getBoolean("is_read");
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

                notifications.add(new Notification(
                        id, currentUserId, taskId, subtaskId, type, content, isRead, createdAt
                ));
            }
        } // Obsługa SQLException została pominięta w tym fragmencie, ale powinna być obecna
        catch (SQLException e) {
            System.err.println("Error fetching latest notifications: " + e.getMessage());
            // Możesz obsłużyć błąd inaczej, np. rzucić go dalej lub zwrócić pustą listę
            // Zgodnie z poprzednimi testami, metoda łapie błąd i zwraca pustą listę.
            return new ArrayList<>();
        }
        return notifications;
    }


    // Gettery i Settery

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }

    // DODAJ TĘ METODĘ: Getter dla subtaskId
    public Integer getSubtaskId() {
        return subtaskId;
    }

    // DODAJ TĘ METODĘ: Setter dla subtaskId (choć może nie być używany, warto go mieć dla pełności)
    public void setSubtaskId(Integer subtaskId) {
        this.subtaskId = subtaskId;
    }


    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }


    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", taskId=" + taskId +
                ", subtaskId=" + subtaskId +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}