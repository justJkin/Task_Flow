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

public class Project {
    private int id;
    private String name;
    private String description;
    private int totalWeight;
    private String status;
    private Date startDate;
    private Date endDate;
    private int adminId;
    private Timestamp createdAt;
    private short progress;

    public Project() {

    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getTotalWeight() { return totalWeight; }
    public void setTotalWeight(int totalWeight) { this.totalWeight = totalWeight; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public short getProgress() { return progress; }
    public void setProgress(short progress) { this.progress = progress; }

    public static List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        String query = "SELECT id, name, end_date FROM project";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                Project project = new Project();
                project.setId(resultSet.getInt("id"));
                project.setName(resultSet.getString("name"));
                project.setEndDate(resultSet.getDate("end_date"));
                projects.add(project);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    // Metody do interakcji z bazą danych
    public static Project getProjectById(int id) {
        Project project = null;
        String query = "SELECT name, description, total_weight, status, start_date, end_date, admin_id, created_at, progress FROM project WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    project = new Project();
                    project.setId(id);
                    project.setName(resultSet.getString("name"));
                    project.setDescription(resultSet.getString("description"));
                    project.setTotalWeight(resultSet.getInt("total_weight"));
                    project.setStatus(resultSet.getString("status"));
                    project.setStartDate(resultSet.getDate("start_date"));
                    project.setEndDate(resultSet.getDate("end_date"));
                    project.setAdminId(resultSet.getInt("admin_id"));
                    project.setCreatedAt(resultSet.getTimestamp("created_at"));
                    project.setProgress(resultSet.getShort("progress"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return project;
    }
}