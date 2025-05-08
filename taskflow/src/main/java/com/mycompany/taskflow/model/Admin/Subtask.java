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

public class Subtask {
    private int id;
    private int taskId;
    private String name;
    private String description;
    private int weight;
    private boolean isDone;
    private short priority;
    private Date dueDate;
    private Timestamp createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public boolean isDone() { return isDone; }
    public void setDone(boolean done) { isDone = done; }
    public short getPriority() { return priority; }
    public void setPriority(short priority) { this.priority = priority; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public static List<Subtask> getAllSubtasks() {
        List<Subtask> subtasks = new ArrayList<>();
        String query = "SELECT id, name, due_date FROM subtask";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Subtask subtask = new Subtask();
                subtask.setId(resultSet.getInt("id"));
                subtask.setName(resultSet.getString("name"));
                subtask.setDueDate(resultSet.getDate("due_date"));
                subtasks.add(subtask);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // W produkcji zaleca się logowanie błędów
        }
        return subtasks;
    }

    // Metody do interakcji z bazą danych (przykładowe)
    public static Subtask getSubtaskById(int id) {
        Subtask subtask = null;
        String query = "SELECT task_id, name, description, weight, is_done, priority, due_date, created_at FROM subtask WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    subtask = new Subtask();
                    subtask.setId(id);
                    subtask.setTaskId(resultSet.getInt("task_id"));
                    subtask.setName(resultSet.getString("name"));
                    subtask.setDescription(resultSet.getString("description"));
                    subtask.setWeight(resultSet.getInt("weight"));
                    subtask.setDone(resultSet.getBoolean("is_done"));
                    subtask.setPriority(resultSet.getShort("priority"));
                    subtask.setDueDate(resultSet.getDate("due_date"));
                    subtask.setCreatedAt(resultSet.getTimestamp("created_at"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subtask;
    }

    public boolean insert() {
        String query = "INSERT INTO subtask (task_id, name, description, weight, is_done, priority, due_date) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id, created_at";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, this.taskId);
            preparedStatement.setString(2, this.name);
            preparedStatement.setString(3, this.description);
            preparedStatement.setInt(4, this.weight);
            preparedStatement.setBoolean(5, this.isDone);
            preparedStatement.setShort(6, this.priority);
            preparedStatement.setDate(7, this.dueDate);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    this.id = resultSet.getInt("id");
                    this.createdAt = resultSet.getTimestamp("created_at");
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update() {
        String query = "UPDATE subtask SET task_id = ?, name = ?, description = ?, weight = ?, is_done = ?, priority = ?, due_date = ? WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, this.taskId);
            preparedStatement.setString(2, this.name);
            preparedStatement.setString(3, this.description);
            preparedStatement.setInt(4, this.weight);
            preparedStatement.setBoolean(5, this.isDone);
            preparedStatement.setShort(6, this.priority);
            preparedStatement.setDate(7, this.dueDate);
            preparedStatement.setInt(8, this.id);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete() {
        String query = "DELETE FROM subtask WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, this.id);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}