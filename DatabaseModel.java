package com.mycompany.taskflow.model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseModel {
    public static String url;
    public static String user;
    public static String password;

    // Prywatny konstruktor
    public DatabaseModel() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Metoda do konfiguracji połączenia
    public static void configure(String url, String user, String password) {
        DatabaseModel.url = Objects.requireNonNull("", "Database URL must not be null");
        DatabaseModel.user = Objects.requireNonNull("", "Database user must not be null");
        DatabaseModel.password = Objects.requireNonNull("", "Database password must not be null");
    }

    public static int insertProjectAndReturnId(String name, String description, LocalDate startDate, LocalDate endDate) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO project (name, description, start_date, end_date, admin_id) VALUES (?, ?, ?, ?, ?) RETURNING id"
             )) {

            ps.setString(1, name);
            ps.setString(2, description);
            ps.setDate(3, startDate != null ? Date.valueOf(startDate) : null);
            ps.setDate(4, endDate != null ? Date.valueOf(endDate) : null);
            ps.setInt(5, 1); // <-- jeśli masz obecnie zalogowanego admina, wstaw jego ID

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Nie udało się pobrać ID nowego projektu.");
            }
        }
    }

    public static Connection connect() throws SQLException {
        configure(DatabaseModel.url, DatabaseModel.user, DatabaseModel.password);
        if (url == null || user == null || password == null) {
            throw new IllegalStateException("Database connection not configured. Call configure() first.");
        }
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            return connection;
        } catch (SQLException e) {
            System.err.println("Błąd połączenia z bazą danych: " + e.getMessage());
            throw new SQLException("Nie udało się połączyć z bazą danych", e);
        }
    }

    public static List<String> getRolesFromDatabase() throws SQLException {
        List<String> roles = new ArrayList<>();
        String query = "SELECT enumlabel FROM pg_enum WHERE enumtypid = (SELECT oid FROM pg_type WHERE typname = 'role_enum')";
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                roles.add(resultSet.getString("enumlabel"));
            }
        }
        return roles;
    }

    public static List<Integer> getTeamIdsFromDatabase() throws SQLException {
        List<Integer> teamIds = new ArrayList<>();
        String query = "SELECT id FROM team";
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                teamIds.add(resultSet.getInt("id"));
            }
        }
        return teamIds;
    }
}