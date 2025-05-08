package com.mycompany.taskflow.model.user;

import com.mycompany.taskflow.model.DatabaseModel;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

public class Task {
    private int id;
    private int milestoneId;
    private String name;
    private String description;
    private String status;
    private short priority;
    private int weight;
    private short progress;
    private Date dueDate;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<Subtask> subtasks = new ArrayList<>();

    public Task() {
    }

    public Task(int i, String s, String inProgress) {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(int milestoneId) {
        this.milestoneId = milestoneId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public short getPriority() {
        return priority;
    }

    public void setPriority(short priority) {
        this.priority = priority;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public short getProgress() {
        return progress;
    }

    public void setProgress(short progress) {
        this.progress = progress;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public static Task getTaskById(int id) {
        Task task = null;
        String query = "SELECT milestone_id, name, description, status, priority, weight, progress, due_date, created_at, updated_at FROM task WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                task = new Task();
                task.setId(id);
                task.setMilestoneId(resultSet.getInt("milestone_id"));
                task.setName(resultSet.getString("name"));
                task.setDescription(resultSet.getString("description"));
                task.setStatus(resultSet.getString("status"));
                task.setPriority(resultSet.getShort("priority"));
                task.setWeight(resultSet.getInt("weight"));
                task.setProgress(resultSet.getShort("progress"));
                task.setDueDate(resultSet.getDate("due_date"));
                task.setCreatedAt(resultSet.getTimestamp("created_at"));
                task.setUpdatedAt(resultSet.getTimestamp("updated_at"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return task;
    }
    public void updateStatusInDatabase() {
        String query = "UPDATE task SET status = CAST(? AS task_status_enum), updated_at = NOW() WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, this.status);
            preparedStatement.setInt(2, this.id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateTaskInDatabase() {
        String query = "UPDATE task SET milestone_id = ?, name = ?, description = ?, status = CAST(? AS task_status_enum), priority = ?, weight = ?, progress = ?, due_date = ?, updated_at = NOW() WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, this.milestoneId);
            preparedStatement.setString(2, this.name);
            preparedStatement.setString(3, this.description);
            preparedStatement.setString(4, this.status);
            preparedStatement.setShort(5, this.priority);
            preparedStatement.setInt(6, this.weight);
            preparedStatement.setShort(7, this.progress);
            preparedStatement.setDate(8, this.dueDate);
            preparedStatement.setInt(9, this.id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}