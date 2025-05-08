package com.mycompany.taskflow.model.Manager;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.Role;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private Role role;
    private Team team;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // Konstruktory
    public User() {
    }

    public User(int id, String firstName, String lastName, String email, String passwordHash, Role role, Team team, LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.team = team;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    public static List<User> getUsersFromDatabaseByTeamId(int teamId) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM \"user\" WHERE team_id = ?";

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, teamId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String email = rs.getString("email");
                    String passwordHash = rs.getString("password_hash");
                    Role role = Role.valueOf(rs.getString("role").toUpperCase());

                    Team team = null;
                    int teamIdFromDB = rs.getInt("team_id");
                    if (!rs.wasNull()) {
                        team = new Team();
                        team.setId(teamIdFromDB);
                    }

                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    LocalDateTime lastLogin = null;
                    Timestamp lastLoginTS = rs.getTimestamp("last_login");
                    if (lastLoginTS != null) {
                        lastLogin = lastLoginTS.toLocalDateTime();
                    }

                    users.add(new User(id, firstName, lastName, email, passwordHash, role, team, createdAt, lastLogin));
                }
            }
        }
        return users;
    }


    public static User getManager() throws SQLException {
        User manager = null;
        String query = "SELECT * FROM \"user\" WHERE role = 'manager' LIMIT 1";

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                int id = rs.getInt("id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String passwordHash = rs.getString("password_hash");
                Role role = Role.valueOf(rs.getString("role").toUpperCase());

                Team team = null;
                int teamIdFromDB = rs.getInt("team_id");
                if (!rs.wasNull()) {
                    team = new Team();
                    team.setId(teamIdFromDB);
                }

                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                LocalDateTime lastLogin = null;
                Timestamp lastLoginTS = rs.getTimestamp("last_login");
                if (lastLoginTS != null) {
                    lastLogin = lastLoginTS.toLocalDateTime();
                }

                manager = new User(id, firstName, lastName, email, passwordHash, role, team, createdAt, lastLogin);
            }
        }
        return manager;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", role=" + role +
                ", team=" + team +
                ", createdAt=" + createdAt +
                ", lastLogin=" + lastLogin +
                '}';
    }
}