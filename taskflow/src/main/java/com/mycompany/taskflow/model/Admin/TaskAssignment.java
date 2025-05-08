package com.mycompany.taskflow.model.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TaskAssignment {
    private int userId;
    private int taskId;
    private Timestamp assignedAt;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
    public Timestamp getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Timestamp assignedAt) { this.assignedAt = assignedAt; }

    // Metody do interakcji z bazą danych
    public static TaskAssignment getAssignment(int userId, int taskId) {
        TaskAssignment assignment = null;
        String query = "SELECT assigned_at FROM task_assignment WHERE user_id = ? AND task_id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, taskId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    assignment = new TaskAssignment();
                    assignment.setUserId(userId);
                    assignment.setTaskId(taskId);
                    assignment.setAssignedAt(resultSet.getTimestamp("assigned_at"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assignment;
    }
}