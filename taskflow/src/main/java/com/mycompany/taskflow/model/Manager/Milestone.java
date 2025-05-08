package com.mycompany.taskflow.model.Manager;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.Manager.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Milestone {
    private int id;
    private Project project;
    private String name;
    private String description;
    private int weight;
    private short progress;
    private Team team;
    private LocalDateTime createdAt;

    // Konstruktory

    public Milestone(int id, Project project, String name, String description, int weight, short progress, Team team, LocalDateTime createdAt) {
        this.id = id;
        this.project = project;
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.progress = progress;
        this.team = team;
        this.createdAt = createdAt;
    }

    public Milestone(int id, int projectId, String name, String description, int weight, short progress, int teamId, LocalDateTime createdAt) {
    }

    public Milestone() {
    }

    public static List<Milestone> getMilestonesFromDatabase() throws SQLException {
        List<Milestone> milestones = new ArrayList<>();
        String query = "SELECT m.*, p.id as project_id, p.name as project_name, t.id as team_id, t.name as team_name " +
                "FROM milestone m " +
                "JOIN project p ON m.project_id = p.id " +
                "JOIN team t ON m.team_id = t.id";

        try (Connection connection = DatabaseModel.connect();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");

                // Pobieranie informacji o projekcie
                int projectId = rs.getInt("project_id");
                String projectName = rs.getString("project_name");
                Project project = new Project(); // Utwórz obiekt Project
                project.setId(projectId);
                project.setName(projectName);

                String name = rs.getString("name");
                String description = rs.getString("description");
                int weight = rs.getInt("weight");
                short progress = rs.getShort("progress");

                // Pobieranie informacji o zespole
                int teamId = rs.getInt("team_id");
                String teamName = rs.getString("team_name");
                Team team = new Team(); // Utwórz obiekt Team
                team.setId(teamId);
                team.setName(teamName);

                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

                milestones.add(new Milestone(id, project, name, description, weight, progress, team, createdAt));
            }
        }
        return milestones;
    }

    public static int getUsedWeightForMilestone(int milestoneId) throws SQLException {
        String query = "SELECT COALESCE(SUM(weight), 0) as used_weight FROM task WHERE milestone_id = ?";
        int usedWeight = 0;

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, milestoneId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                usedWeight = rs.getInt("used_weight");
            }
        }
        return usedWeight;
    }

    public static List<Milestone> getMilestonesFromDatabaseByManagerId(int managerId) throws SQLException {
        List<Milestone> milestones = new ArrayList<>();
        // Pobierz ID zespołu managera, a następnie kamienie milowe przypisane do tego zespołu
        String query = "SELECT m.*, p.id as project_id, p.name as project_name, " +
                "t.id as team_id, t.name as team_name " +
                "FROM milestone m " +
                "JOIN project p ON m.project_id = p.id " +
                "JOIN team t ON m.team_id = m.team_id " +
                "JOIN \"user\" u ON t.id = u.team_id " + // Dołączamy tabelę user
                "WHERE u.id = ?"; // Warunek: id managera

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, managerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");

                // Pobieranie informacji o projekcie
                int projectId = rs.getInt("project_id");
                String projectName = rs.getString("project_name");
                Project project = new Project();
                project.setId(projectId);
                project.setName(projectName);

                String name = rs.getString("name");
                String description = rs.getString("description");
                int weight = rs.getInt("weight");
                short progress = rs.getShort("progress");

                // Pobieranie informacji o zespole
                int teamId = rs.getInt("team_id");
                String teamName = rs.getString("team_name");
                Team team = new Team();
                team.setId(teamId);
                team.setName(teamName);

                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

                milestones.add(new Milestone(id, project, name, description, weight, progress, team, createdAt));
            }
        }
        return milestones;
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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

    public short getProgress() {
        return progress;
    }

    public void setProgress(short progress) {
        this.progress = progress;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Milestone{" +
                "id=" + id +
                ", project=" + project +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", weight=" + weight +
                ", progress=" + progress +
                ", team=" + team +
                ", createdAt=" + createdAt +
                '}';
    }
}