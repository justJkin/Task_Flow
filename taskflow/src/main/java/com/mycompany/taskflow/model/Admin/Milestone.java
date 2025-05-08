package com.mycompany.taskflow.model.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Milestone {
    private int id;
    private int projectId;
    private String name;
    private String description;
    private int weight;
    private short progress;
    private int teamId;
    private Timestamp createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public short getProgress() { return progress; }
    public void setProgress(short progress) { this.progress = progress; }
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public static Milestone getMilestoneById(int id) {
        Milestone milestone = null;
        String query = "SELECT project_id, name, description, weight, progress, team_id, created_at FROM milestone WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    milestone = new Milestone();
                    milestone.setId(id);
                    milestone.setProjectId(resultSet.getInt("project_id"));
                    milestone.setName(resultSet.getString("name"));
                    milestone.setDescription(resultSet.getString("description"));
                    milestone.setWeight(resultSet.getInt("weight"));
                    milestone.setProgress(resultSet.getShort("progress"));
                    milestone.setTeamId(resultSet.getInt("team_id"));
                    milestone.setCreatedAt(resultSet.getTimestamp("created_at"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return milestone;
    }

    public static List<Milestone> getMilestonesByProject(int projId) {
        List<Milestone> out = new ArrayList<>();
        String sql = "SELECT id, project_id, name, description, weight, progress, team_id, created_at "
                + "FROM milestone WHERE project_id=?";
        try (Connection c = DatabaseModel.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, projId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Milestone m = new Milestone();
                    m.setId(rs.getInt("id"));
                    m.setProjectId(projId);
                    m.setName(rs.getString("name"));
                    m.setDescription(rs.getString("description"));
                    m.setWeight(rs.getInt("weight"));
                    m.setProgress(rs.getShort("progress"));
                    m.setTeamId(rs.getInt("team_id"));
                    m.setCreatedAt(rs.getTimestamp("created_at"));
                    out.add(m);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }
}