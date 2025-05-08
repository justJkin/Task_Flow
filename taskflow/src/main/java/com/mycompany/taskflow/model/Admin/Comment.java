package com.mycompany.taskflow.model.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Comment {
    private int id;
    private Integer taskId;
    private Integer subtaskId;
    private int userId;
    private String content;
    private Timestamp createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }
    public Integer getSubtaskId() { return subtaskId; }
    public void setSubtaskId(Integer subtaskId) { this.subtaskId = subtaskId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public static Comment getCommentById(int id) {
        Comment comment = null;
        String query = "SELECT task_id, subtask_id, user_id, content, created_at FROM comment WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            // Zmiana: Dodano kolejny blok try-with-resources dla ResultSet
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    comment = new Comment();
                    comment.setId(id);
                    // Użyj getObject dla kolumn, które mogą być NULL w bazie (np. task_id, subtask_id)
                    // getInt zwraca 0 dla NULL, co może być mylące.
                    // Jeśli Integer jest dozwolony w modelu, lepiej użyć getObject i rzutować na Integer.
                    Integer taskId = (Integer) resultSet.getObject("task_id");
                    Integer subtaskId = (Integer) resultSet.getObject("subtask_id");

                    comment.setTaskId(taskId);
                    comment.setSubtaskId(subtaskId);
                    comment.setUserId(resultSet.getInt("user_id"));
                    comment.setContent(resultSet.getString("content"));
                    comment.setCreatedAt(resultSet.getTimestamp("created_at"));
                }
            } // ResultSet zostanie automatycznie zamknięty tutaj
        } catch (SQLException e) {
            e.printStackTrace();
        } // Connection i PreparedStatement zostaną automatycznie zamknięte tutaj
        return comment;
    }
}