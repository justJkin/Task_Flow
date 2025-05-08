package com.mycompany.taskflow.model.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Team {
    private int id;
    private String name;

    public Team(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Team() {

    }
    // Gettery

    // + gettery/settery …

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name; // lub inna właściwość zespołu, np. id + name
    }

    /** Zwraca jeden zespół po id */
    public static Team getTeamById(int id) {
        Team team = null;
        String sql = "SELECT id, name FROM team WHERE id = ?";
        try (Connection c = DatabaseModel.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    team = new Team();
                    team.setId(rs.getInt("id"));
                    team.setName(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return team;
    }

    /** Zwraca listę wszystkich zespołów */
    public static List<Team> getAllTeams() {
        List<Team> teams = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseModel.connect();
            ps = conn.prepareStatement("SELECT id, name FROM team");
            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                teams.add(new Team(id, name));
            }
        } catch (SQLException e) {
            // Można dodać logger, tutaj tylko e.printStackTrace dla uproszczenia
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return teams;
    }

}