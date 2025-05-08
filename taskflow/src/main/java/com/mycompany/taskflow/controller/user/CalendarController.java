package com.mycompany.taskflow.controller.user;

import com.mycompany.taskflow.model.DatabaseModel;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarController {

    @FXML
    private BorderPane calendarRoot;
    @FXML
    private Label monthYearLabel;
    @FXML
    private GridPane calendarGrid;
    @FXML
    public ListView<String> scheduleListView;
    private static List<CalendarController.Deadline> userDeadlines;
    private YearMonth currentYearMonth;
    private int userId;
    public CalendarController() {
        this.userId=userId;
    }

    public void initialize() {
        currentYearMonth = YearMonth.now();
        userDeadlines = this.loadUserDeadlines();
        drawCalendar();
        populateSchedule();

        calendarRoot.setOnMouseClicked(this::handleCalendarRootClick);
    }



    private List<Deadline> loadUserDeadlines() {
        List<Deadline> loadedDeadlines = new ArrayList<>();
        String taskQuery = "SELECT name, due_date FROM task WHERE due_date IS NOT NULL";
        String subtaskQuery = "SELECT description, due_date FROM subtask WHERE due_date IS NOT NULL";

        try (Connection conn = DatabaseModel.connect();
             PreparedStatement taskStmt = conn.prepareStatement(taskQuery);
             ResultSet taskRs = taskStmt.executeQuery();
             PreparedStatement subtaskStmt = conn.prepareStatement(subtaskQuery);
             ResultSet subtaskRs = subtaskStmt.executeQuery()) {

            while (taskRs.next()) {
                String name = taskRs.getString("name");
                LocalDate dueDate = taskRs.getDate("due_date").toLocalDate();
                loadedDeadlines.add(new Deadline(name, dueDate, "zadanie"));
            }

            while (subtaskRs.next()) {
                String description = subtaskRs.getString("description");
                LocalDate dueDate = subtaskRs.getDate("due_date").toLocalDate();
                loadedDeadlines.add(new Deadline(description, dueDate, "cząstka"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return loadedDeadlines;
    }




    private void drawCalendar() {
        calendarGrid.getChildren().clear();
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();
        int daysInMonth = currentYearMonth.lengthOfMonth();
        monthYearLabel.setText(DateTimeFormatter.ofPattern("MMMM yyyy").format(currentYearMonth));

        int row = 1;
        int column = firstDayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = LocalDate.of(currentYearMonth.getYear(), currentYearMonth.getMonth(), day);
            Button dayButton = new Button(String.valueOf(day));
            dayButton.getStyleClass().add("calendar-day");
            dayButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            GridPane.setFillWidth(dayButton, true);
            GridPane.setFillHeight(dayButton, true);

            if (currentDate.equals(today)) {
                dayButton.getStyleClass().add("today");
            }

            List<Deadline> dailyDeadlines = getDeadlinesForDate(currentDate);
            if (!dailyDeadlines.isEmpty()) {
                VBox indicators = new VBox(2);
                indicators.setMouseTransparent(true);
                for (Deadline deadline : dailyDeadlines) {
                    Circle indicator = new Circle(5);
                    indicator.setFill(getDeadlineColor(deadline.getType()));
                    indicators.getChildren().add(indicator);
                }
                StackPane dayPane = new StackPane(dayButton, indicators);
                calendarGrid.add(dayPane, column, row);
            } else {
                calendarGrid.add(dayButton, column, row);
            }

            final LocalDate finalCurrentDate = currentDate;
            dayButton.setOnAction(event -> showDailySchedule(finalCurrentDate));

            column++;
            if (column > 6) {
                column = 0;
                row++;
            }
        }
    }

    private Color getDeadlineColor(String type) {
        switch (type.toLowerCase()) {
            case "cząstka":
                return Color.GREEN;
            case "zadanie":
                return Color.YELLOW;
            default:
                return Color.GRAY;
        }
    }

    @FXML
    private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        drawCalendar();
    }


    @FXML
    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        drawCalendar();
    }

    private void populateSchedule() {
        scheduleListView.getItems().clear();
        if (userDeadlines != null && !userDeadlines.isEmpty()) {
            List<Deadline> sortedDeadlines = userDeadlines.stream()
                    .sorted(Comparator.comparing(Deadline::getDeadline))
                    .collect(Collectors.toList());
            for (Deadline deadline : sortedDeadlines) {
                scheduleListView.getItems().add(formatDeadline(deadline));
            }
        } else {
            scheduleListView.getItems().add("Brak nadchodzących terminów.");
        }
    }

    private void showDailySchedule(LocalDate date) {
        scheduleListView.getItems().clear();
        List<Deadline> dailyDeadlines = getDeadlinesForDate(date);
        if (!dailyDeadlines.isEmpty()) {
            for (Deadline deadline : dailyDeadlines) {
                scheduleListView.getItems().add(formatDeadline(deadline));
            }
        } else {
            scheduleListView.getItems().add("Brak terminów na ten dzień.");
        }
    }

    @FXML
    private void handleCalendarRootClick(MouseEvent event) {
        if (!isInsideCalendar(event.getTarget())) {
            populateSchedule();
        }
    }

    private boolean isInsideCalendar(Object target) {
        if (target instanceof Node) {
            Node node = (Node) target;
            while (node != null) {
                if (node == calendarGrid) {
                    return true;
                }
                node = node.getParent();
            }
        }
        return false;
    }

    private String formatDeadline(Deadline deadline) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String formattedDeadline = deadline.getDeadline().format(formatter);
        return deadline.getType() + " (" + deadline.getName() + ") - Termin: " + formattedDeadline;
    }

    private List<Deadline> getDeadlinesForDate(LocalDate date) {
        return userDeadlines.stream()
                .filter(deadline -> deadline.getDeadline().equals(date))
                .collect(Collectors.toList());
    }

    private List<Deadline> loadUserDeadlinesFromDatabase(int userId) {
        List<Deadline> loadedDeadlines = new ArrayList<>();

        String taskQuery = "SELECT t.name, t.due_date " +
                "FROM task_assignment ta " +
                "JOIN task t ON ta.task_id = t.id " +
                "WHERE ta.user_id = ? AND t.due_date IS NOT NULL";

        String subtaskQuery = "SELECT st.description, st.due_date " +
                "FROM subtask st " +
                "JOIN task t ON st.task_id = t.id " +
                "JOIN task_assignment ta ON t.id = ta.task_id " +
                "WHERE ta.user_id = ? AND st.due_date IS NOT NULL";

        try (Connection conn = DatabaseModel.connect();
             PreparedStatement taskStmt = conn.prepareStatement(taskQuery);
             PreparedStatement subtaskStmt = conn.prepareStatement(subtaskQuery)) {

            taskStmt.setInt(1, userId);
            subtaskStmt.setInt(1, userId);

            try (ResultSet taskRs = taskStmt.executeQuery()) {
                while (taskRs.next()) {
                    String name = taskRs.getString("name");
                    LocalDate dueDate = taskRs.getDate("due_date").toLocalDate();
                    loadedDeadlines.add(new Deadline(name, dueDate, "zadanie"));
                }
            }

            try (ResultSet subtaskRs = subtaskStmt.executeQuery()) {
                while (subtaskRs.next()) {
                    String description = subtaskRs.getString("description");
                    LocalDate dueDate = subtaskRs.getDate("due_date").toLocalDate();
                    loadedDeadlines.add(new Deadline(description, dueDate, "cząstka"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas ładowania terminów: " + e.getMessage());
            e.printStackTrace();
        }
        return loadedDeadlines;
    }
    private static class Deadline {
        private String name;
        private LocalDate deadline;
        private String type;

        public Deadline(String name, LocalDate deadline, String type) {
            this.name = name;
            this.deadline = deadline;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public LocalDate getDeadline() {
            return deadline;
        }

        public String getType() {
            return type;
        }
    }
}