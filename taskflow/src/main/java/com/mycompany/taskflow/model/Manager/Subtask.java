package com.mycompany.taskflow.model.Manager;

import com.mycompany.taskflow.model.DatabaseModel;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Subtask {
    private int id;
    private Task task; // Relacja ManyToOne
    private String name;
    private String description;
    private int weight;
    private boolean isDone;
    private Short priority;
    private LocalDate dueDate;
    private LocalDateTime createdAt;

    // Konstruktory, Gettery i Settery
    public Subtask() {
    }

    public Subtask(int id, Task task, String name, String description, int weight, boolean isDone, Short priority, LocalDate dueDate, LocalDateTime createdAt) {
        this.id = id;
        this.task = task;
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.isDone = isDone;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
    }

    public static List<Subtask> getRecentSubtasksForTeam(int teamId) {
        List<Subtask> recentSubtasks = new ArrayList<>();
        String query = "SELECT st.*, t.id as task_id, t.name as task_name FROM subtask st " +
                "JOIN task t ON st.task_id = t.id " +
                "JOIN milestone m ON t.milestone_id = m.id " +
                "WHERE m.team_id = ? AND st.is_done = true " +
                "ORDER BY st.created_at DESC LIMIT 5";

        System.out.println("Executing query: " + query); // Debug

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, teamId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Subtask subtask = new Subtask();
                subtask.setId(rs.getInt("id"));
                subtask.setName(rs.getString("name"));
                Date dueDateSql = rs.getDate("due_date");
                LocalDate dueDate = (dueDateSql != null) ? dueDateSql.toLocalDate() : null;

                // Task
                Task task = new Task();
                task.setId(rs.getInt("task_id"));
                task.setName(rs.getString("task_name"));
                subtask.setTask(task);

                recentSubtasks.add(subtask);
                System.out.println("Found subtask: " + subtask.getName()); // Debug
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getRecentSubtasksForTeam: " + e.getMessage());
            e.printStackTrace();
        }
        return recentSubtasks;
    }

    public static List<Subtask> getUpcomingSubtasksForTeam(int teamId) {
        List<Subtask> upcomingSubtasks = new ArrayList<>();
        LocalDate nextWeekStart = LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.MONDAY);
        LocalDate nextWeekEnd = nextWeekStart.plusDays(6);
        String query = "SELECT st.*, t.id as task_id, t.name as task_name, m.team_id " +
                "FROM subtask st " +
                "JOIN task t ON st.task_id = t.id " +
                "JOIN milestone m ON t.milestone_id = m.id " +
                "WHERE m.team_id = ? AND st.due_date >= ? AND st.due_date <= ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, teamId);
            pstmt.setDate(2, java.sql.Date.valueOf(nextWeekStart));
            pstmt.setDate(3, java.sql.Date.valueOf(nextWeekEnd));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Subtask subtask = new Subtask();
                subtask.setId(rs.getInt("id"));
                subtask.setName(rs.getString("name"));
                subtask.setDescription(rs.getString("description"));
                subtask.setWeight(rs.getInt("weight"));
                subtask.setDone(rs.getBoolean("is_done"));
                subtask.setPriority(rs.getShort("priority"));
                subtask.setDueDate(rs.getDate("due_date").toLocalDate());
                subtask.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                // Task
                Task task = new Task();
                task.setId(rs.getInt("task_id"));
                task.setName(rs.getString("task_name"));
                subtask.setTask(task);
                upcomingSubtasks.add(subtask);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return upcomingSubtasks;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
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

    public Short getPriority() {
        return priority;
    }

    public void setPriority(Short priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", task=" + task +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", weight=" + weight +
                ", isDone=" + isDone +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", createdAt=" + createdAt +
                '}';
    }
}