package com.mycompany.taskflow.model.user;

import com.mycompany.taskflow.model.DatabaseModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.Objects;

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
    private String taskName;


    public Subtask() {

    }

    public Subtask(int id, int taskId, String name, String description, int weight,
                   boolean isDone, short priority, Date dueDate, Timestamp createdAt, String taskName) {
        this.id = id;
        this.taskId = taskId;
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.isDone = isDone;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.taskName = taskName;
    }

    public Subtask(Subtask other) {
        this.id = other.id;
        this.taskId = other.taskId;
        this.name = other.name;
        this.description = other.description;
        this.weight = other.weight;
        this.isDone = other.isDone;
        this.priority = other.priority;
        this.dueDate = other.dueDate;
        this.createdAt = other.createdAt;
        this.taskName = other.taskName;
    }


    // Gettery i settery
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
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

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public short getPriority() {
        return priority;
    }

    public void setPriority(short priority) {
        this.priority = priority;
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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }


    public boolean insert() {
        final String query = """
    INSERT INTO subtask (task_id, name, description, weight,
                        is_done, priority, due_date)
    VALUES (?, ?, ?, ?, ?, ?, ?)
    RETURNING id, created_at""";

        try (Connection conn = DatabaseModel.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            setStatementParameters(stmt);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.id = rs.getInt("id");
                this.createdAt = rs.getTimestamp("created_at");
                return true;
            }
        } catch (SQLException e) {
            handleDatabaseError("Failed to insert subtask", e, query);
        }
        return false;
    }

    private static void handleDatabaseError(String message, SQLException e, String query) {
        System.err.println(message);
        System.err.println("SQL Error: " + e.getMessage());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Query: " + query);
        e.printStackTrace();
    }

    public boolean update() {
        final String query = """
            UPDATE subtask
            SET task_id = ?, name = ?, description = ?, weight = ?,
                is_done = ?, priority = ?, due_date = ?
            WHERE id = ?""";

        try (Connection conn = DatabaseModel.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            setStatementParameters(stmt);
            stmt.setInt(8, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleDatabaseError("Failed to update subtask", e, query);
        }
        return false;
    }

    private void setStatementParameters(PreparedStatement stmt) throws SQLException {
        stmt.setInt(1, taskId);
        stmt.setString(2, name);
        stmt.setString(3, description);
        stmt.setInt(4, weight);
        stmt.setBoolean(5, isDone);
        stmt.setShort(6, priority);
        stmt.setDate(7, dueDate);
    }

    // equals, hashCode i toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subtask subtask = (Subtask) o;
        return id == subtask.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", taskName='" + taskName + '\'' +
                ", isDone=" + isDone +
                ", dueDate=" + dueDate +
                '}';
    }

    public static Subtask getSubtaskById(int id) {
        final String query = """
            SELECT s.id, s.task_id, s.name, s.description, s.weight,
                   s.is_done, s.priority, s.due_date, s.created_at,
                   t.name AS task_name
            FROM subtask s
            JOIN task t ON s.task_id = t.id
            WHERE s.id = ?""";

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Subtask(
                        rs.getInt("id"),
                        rs.getInt("task_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("weight"),
                        rs.getBoolean("is_done"),
                        rs.getShort("priority"),
                        rs.getDate("due_date"),
                        rs.getTimestamp("created_at"),
                        rs.getString("task_name")
                );
            }
        } catch (SQLException e) {
            handleDatabaseError("Failed to get subtask by ID", e, query);
        }
        return null;
    }

    public static ObservableList<Subtask> getSubtasksByTaskId(int taskId) {
        ObservableList<Subtask> subtasks = FXCollections.observableArrayList();
        String query = "SELECT s.id, s.task_id, s.name, s.description, s.weight, s.is_done, s.priority, s.due_date, s.created_at, t.name AS task_name " +
                "FROM subtask s " +
                "JOIN task t ON s.task_id = t.id " +
                "WHERE s.task_id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Subtask subtask = new Subtask(
                        rs.getInt("id"),
                        rs.getInt("task_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("weight"),
                        rs.getBoolean("is_done"),
                        rs.getShort("priority"),
                        rs.getDate("due_date"),
                        rs.getTimestamp("created_at"),
                        rs.getString("task_name")
                );
                subtasks.add(subtask);
            }
        } catch (SQLException e) {
            handleDatabaseError("Failed to get subtasks by task ID", e, query);
        }
        return subtasks;
    }

}