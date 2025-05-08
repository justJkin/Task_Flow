package com.mycompany.taskflow.model.Manager;

import com.mycompany.taskflow.model.TaskStatus;
import com.mycompany.taskflow.model.DatabaseModel;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private int id;
    private Milestone milestone;
    private String name;
    private String description;
    private TaskStatus status;
    private Short priority;
    private int weight;
    private short progress;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Task(int id, Milestone milestone, String name, String description,
                TaskStatus status, Short priority, int weight, short progress,
                LocalDate dueDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.milestone = milestone;
        this.name = name;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.weight = weight;
        this.progress = progress;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Task() {
        this.status = TaskStatus.TO_DO;
        this.priority = 0;
        this.progress = 0;
        this.weight = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Metody statyczne pozostają bez zmian (jak w poprzednim kodzie)
    // ...

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Short getPriority() {
        return priority;
    }

    public void setPriority(Short priority) {
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", milestone=" + milestone +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", weight=" + weight +
                ", progress=" + progress +
                ", dueDate=" + dueDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // Metody statyczne (te same co w poprzednim rozwiązaniu)

    public static void saveTaskToDatabase(Task task) throws SQLException {
        if (task.getStatus() == null) {
            throw new SQLException("Task status cannot be null");
        }

        String query = "INSERT INTO task (milestone_id, name, description, status, priority, weight, progress, due_date, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?::task_status_enum, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, task.getMilestone().getId());
            pstmt.setString(2, task.getName());
            pstmt.setString(3, task.getDescription());
            pstmt.setString(4, task.getStatus().getDbValue());
            pstmt.setShort(5, task.getPriority());
            pstmt.setInt(6, task.getWeight());
            pstmt.setShort(7, task.getProgress());
            pstmt.setDate(8, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
            pstmt.setTimestamp(9, Timestamp.valueOf(task.getCreatedAt()));
            pstmt.setTimestamp(10, Timestamp.valueOf(task.getUpdatedAt()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating task failed, no ID obtained.");
                }
            }
        }
    }

    public static int getUsedWeightForMilestone(int milestoneId) throws SQLException {
        String query = "SELECT COALESCE(SUM(weight), 0) as used_weight FROM task WHERE milestone_id = ?";

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, milestoneId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("used_weight");
                }
                return 0;
            }
        }
    }

    public static List<Task> getRecentTasksForTeam(int teamId) {
        List<Task> recentTasks = new ArrayList<>();
        String query = "SELECT t.*, m.id as milestone_id, m.name as milestone_name FROM task t " +
                "JOIN milestone m ON t.milestone_id = m.id " +
                "WHERE m.team_id = ? AND t.status = ?::task_status_enum " +
                "ORDER BY t.updated_at DESC LIMIT 5";

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, teamId);
            pstmt.setString(2, TaskStatus.DONE.getDbValue());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("id"));
                    task.setName(rs.getString("name"));

                    Date dueDate = rs.getDate("due_date");
                    task.setDueDate(dueDate != null ? dueDate.toLocalDate() : null);

                    Milestone milestone = new Milestone();
                    milestone.setId(rs.getInt("milestone_id"));
                    milestone.setName(rs.getString("milestone_name"));
                    task.setMilestone(milestone);

                    recentTasks.add(task);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getRecentTasksForTeam: " + e.getMessage());
        }
        return recentTasks;
    }

    public static List<Task> getUpcomingTasksForTeam(int teamId) {
        List<Task> upcomingTasks = new ArrayList<>();
        LocalDate nextWeekStart = LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.MONDAY);
        LocalDate nextWeekEnd = nextWeekStart.plusDays(6);

        String query = "SELECT t.*, m.id as milestone_id, m.name as milestone_name FROM task t " +
                "JOIN milestone m ON t.milestone_id = m.id " +
                "JOIN team tm ON m.team_id = tm.id " +
                "WHERE tm.id = ? AND t.due_date >= ? AND t.due_date <= ?";

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, teamId);
            pstmt.setDate(2, Date.valueOf(nextWeekStart));
            pstmt.setDate(3, Date.valueOf(nextWeekEnd));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("id"));
                    task.setName(rs.getString("name"));
                    task.setDescription(rs.getString("description"));

                    String statusValue = rs.getString("status");
                    task.setStatus(statusValue != null ? TaskStatus.fromDbValue(statusValue) : null);

                    task.setPriority(rs.getShort("priority"));
                    task.setWeight(rs.getInt("weight"));
                    task.setProgress(rs.getShort("progress"));

                    Date dueDate = rs.getDate("due_date");
                    task.setDueDate(dueDate != null ? dueDate.toLocalDate() : null);

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    task.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

                    Milestone milestone = new Milestone();
                    milestone.setId(rs.getInt("milestone_id"));
                    milestone.setName(rs.getString("milestone_name"));
                    task.setMilestone(milestone);

                    upcomingTasks.add(task);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getUpcomingTasksForTeam: " + e.getMessage());
        }
        return upcomingTasks;
    }
}