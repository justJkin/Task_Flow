package com.mycompany.taskflow.model.user;

import com.mycompany.taskflow.model.DatabaseModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Dashboard {

    public static class Deadline {
        private String type;
        private String name;
        private LocalDate deadline;

        public Deadline(String type, String name, LocalDate deadline) {
            this.type = type;
            this.name = name;
            this.deadline = deadline;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public LocalDate getDeadline() {
            return deadline;
        }
    }

    public static class NotificationData {
        private int id;
        private int userId;
        private Integer taskId;
        private Integer subtaskId;
        private String type;
        private String content;
        private boolean isRead;
        private LocalDateTime timestamp;

        public NotificationData(int id, int userId, Integer taskId, Integer subtaskId, String type, String content, boolean isRead, LocalDateTime timestamp) {
            this.id = id;
            this.userId = userId;
            this.taskId = taskId;
            this.subtaskId = subtaskId;
            this.type = type;
            this.content = content;
            this.isRead = isRead;
            this.timestamp = timestamp;
        }

        public int getId() {
            return id;
        }

        public int getUserId() {
            return userId;
        }

        public Integer getTaskId() {
            return taskId;
        }

        public Integer getSubtaskId() {
            return subtaskId;
        }

        public String getType() {
            return type;
        }

        public String getContent() {
            return content;
        }

        public boolean isRead() {
            return isRead;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    public static List<Deadline> loadUserDeadlines(int userId) {
        List<Deadline> deadlines = new ArrayList<>();
        String query = "SELECT t.name AS task_name, t.due_date " +
                "FROM task_assignment ta " +
                "JOIN task t ON ta.task_id = t.id " +
                "WHERE ta.user_id = ? AND t.due_date IS NOT NULL";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String taskName = resultSet.getString("task_name");
                LocalDate deadline = resultSet.getDate("due_date").toLocalDate();
                deadlines.add(new Deadline("Zadanie", taskName, deadline));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deadlines;
    }

    public static List<String> getUserNotifications(int userId) {
        List<String> notifications = new ArrayList<>();
        String query = "SELECT content " +
                "FROM notification " +
                "WHERE user_id = ? " +
                "ORDER BY created_at DESC " +
                "LIMIT 7";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String content = resultSet.getString("content");
                notifications.add(content);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public static void markNotificationAsRead(int notificationId) {
        String query = "UPDATE notification SET is_read = TRUE WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, notificationId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static double calculateUserProgress(int userId) {
        double progress = 0.0;
        String query = "SELECT COUNT(t.id) AS total_tasks, " +
                "SUM(CASE WHEN t.status = 'Done' THEN 1 ELSE 0 END) AS completed_tasks " +
                "FROM task_assignment ta " +
                "JOIN task t ON ta.task_id = t.id " +
                "WHERE ta.user_id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int totalTasks = resultSet.getInt("total_tasks");
                int completedTasks = resultSet.getInt("completed_tasks");
                if (totalTasks > 0) {
                    progress = (double) completedTasks / totalTasks;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return progress;
    }

    public static int getUserNotificationsCount(int userId) {
        int count = 0;
        String query = "SELECT COUNT(id) FROM notification WHERE user_id = ? AND is_read = false";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}