package com.mycompany.taskflow.model.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMilestoneId() { return milestoneId; }
    public void setMilestoneId(int milestoneId) { this.milestoneId = milestoneId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public short getPriority() { return priority; }
    public void setPriority(short priority) { this.priority = priority; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public short getProgress() { return progress; }
    public void setProgress(short progress) { this.progress = progress; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public static List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT id, name, due_date FROM task";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Task task = new Task();
                task.setId(resultSet.getInt("id"));
                task.setName(resultSet.getString("name"));
                task.setDueDate(resultSet.getDate("due_date"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // Metody do interakcji z bazą danych
    public static Task getTaskById(int id) {
        Task task = null;
        String query = "SELECT milestone_id, name, description, status, priority, weight, progress, due_date, created_at, updated_at FROM task WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return task;
    }
}