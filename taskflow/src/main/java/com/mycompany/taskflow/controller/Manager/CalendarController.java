package com.mycompany.taskflow.controller.Manager;

import com.mycompany.taskflow.model.DatabaseModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CalendarController implements Initializable {

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label monthYearLabel;

    private YearMonth currentYearMonth = YearMonth.now();
    private Map<LocalDate, List<String>> dailyEvents = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAllEvents();
        populateCalendar(currentYearMonth);
    }

    private void loadAllEvents() {
        dailyEvents.clear();
        loadSubtaskEvents();
        loadTaskEvents();
        loadMilestoneEvents();
    }

    private void loadSubtaskEvents() {
        String query = "SELECT s.name AS subtask_name, s.due_date FROM subtask s";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String subtaskName = resultSet.getString("subtask_name");
                LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
                dailyEvents.computeIfAbsent(dueDate, k -> new ArrayList<>()).add("Cząstka: " + subtaskName);
            }
        } catch (SQLException e) {
            System.err.println("Error loading subtask events: " + e.getMessage());
        }
    }

    private void loadTaskEvents() {
        String query = "SELECT t.name AS task_name, t.due_date FROM task t";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String taskName = resultSet.getString("task_name");
                LocalDate dueDate = resultSet.getDate("due_date").toLocalDate();
                dailyEvents.computeIfAbsent(dueDate, k -> new ArrayList<>()).add("Zadanie: " + taskName);
            }
        } catch (SQLException e) {
            System.err.println("Error loading task events: " + e.getMessage());
        }
    }

    private void loadMilestoneEvents() {
        String query = "SELECT name, created_at FROM milestone";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String milestoneName = resultSet.getString("name");
                LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
                LocalDate eventDate = createdAt.toLocalDate(); // Używamy daty utworzenia jako punktu na kalendarzu
                dailyEvents.computeIfAbsent(eventDate, k -> new ArrayList<>()).add("Kamień Milowy: " + milestoneName);
            }
        } catch (SQLException e) {
            System.err.println("Error loading milestone events: " + e.getMessage());
        }
    }

    void populateCalendar(YearMonth yearMonth) {
        Platform.runLater(() -> {
            calendarGrid.getChildren().clear();
            monthYearLabel.setText(yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("pl", "PL"))));

            LocalDate firstDayOfMonth = yearMonth.atDay(1);
            int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); // 1 = Monday, ..., 7 = Sunday
            int daysInMonth = yearMonth.lengthOfMonth();

            int row = 0;
            int col = dayOfWeek - 1; // Adjust for 0-based grid

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate currentDate = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), day);
                Label dayLabel = new Label(String.valueOf(day));
                dayLabel.setMinWidth(50);
                dayLabel.setMinHeight(50);
                dayLabel.setStyle("-fx-border-color: lightgray; -fx-alignment: top-left; -fx-padding: 5px;");

                List<String> events = dailyEvents.get(currentDate);
                if (events != null && !events.isEmpty()) {
                    StringBuilder eventsText = new StringBuilder();
                    for (String event : events) {
                        eventsText.append("- ").append(event).append("\n");
                    }
                    Label eventsLabel = new Label(eventsText.toString());
                    eventsLabel.setStyle("-fx-font-size: 9px;");
                    VBox dayContainer = new VBox(dayLabel, eventsLabel);
                    calendarGrid.add(dayContainer, col, row);
                } else {
                    calendarGrid.add(dayLabel, col, row);
                }

                col++;
                if (col > 6) {
                    col = 0;
                    row++;
                }
            }
        });
    }

    @FXML
    void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        populateCalendar(currentYearMonth);
    }

    @FXML
    void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        populateCalendar(currentYearMonth);
    }
}