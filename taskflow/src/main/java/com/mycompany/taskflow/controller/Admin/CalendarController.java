package com.mycompany.taskflow.controller.Admin;

import com.mycompany.taskflow.model.Admin.Deadline;
import com.mycompany.taskflow.model.Admin.Milestone; // Import, choć na razie nieużywany do deadline'ów
import com.mycompany.taskflow.model.Admin.Project;
import com.mycompany.taskflow.model.Admin.Subtask;
import com.mycompany.taskflow.model.Admin.Task;
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

import java.sql.Date;
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
    private ListView<String> scheduleListView;

    private YearMonth currentYearMonth;
    List<Deadline> allDeadlines;

    public void initialize() {
        currentYearMonth = YearMonth.now();
        allDeadlines = loadDeadlines();
        drawCalendar();
        populateSchedule();

        calendarRoot.setOnMouseClicked(this::handleCalendarRootClick);
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

    Color getDeadlineColor(String type) {
        switch (type.toLowerCase()) {
            case "cząstka":
                return Color.GREEN;
            case "zadanie":
                return Color.YELLOW;
            case "milestone":
                return Color.BLUE;
            case "projekt":
                return Color.RED;
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
        if (allDeadlines != null && !allDeadlines.isEmpty()) {
            List<Deadline> sortedDeadlines = allDeadlines.stream()
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

    String formatDeadline(Deadline deadline) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String formattedDeadline = deadline.getDeadline().format(formatter);
        return deadline.getType() + " (" + deadline.getName() + ") - Termin: " + formattedDeadline +
                (deadline.getUser() != null ? " - Użytkownik: " + deadline.getUser() : "") +
                (deadline.getTeam() != null ? " - Zespół: " + String.join(", ", deadline.getTeam()) : "");
    }

    List<Deadline> getDeadlinesForDate(LocalDate date) {
        List<Deadline> filteredDeadlines = allDeadlines.stream()
                .filter(deadline -> deadline.getDeadline().equals(date))
                .collect(Collectors.toList());
        System.out.println("Sprawdzam datę: " + date + ", znaleziono deadline'ów: " + filteredDeadlines.size()); // Dodaj tę linię
        return filteredDeadlines;
    }

    List<Deadline> loadDeadlines() {
        List<Deadline> deadlines = new ArrayList<>();

        // Pobierz projekty
        List<Project> projects = Project.getAllProjects();
        System.out.println("Pobrano projektów: " + (projects != null ? projects.size() : 0)); // Dodaj tę linię
        if (projects != null) {
            for (Project project : projects) {
                if (project.getEndDate() != null) {
                    deadlines.add(new Deadline(project.getName(), project.getEndDate().toLocalDate(), "projekt", null, null));
                }
            }
        }

        // Pobierz zadania
        List<Task> tasks = Task.getAllTasks();
        System.out.println("Pobrano zadań: " + (tasks != null ? tasks.size() : 0)); // Dodaj tę linię
        if (tasks != null) {
            for (Task task : tasks) {
                if (task.getDueDate() != null) {
                    deadlines.add(new Deadline(task.getName(), task.getDueDate().toLocalDate(), "zadanie", null, null));
                }
            }
        }

        // Pobierz podzadania (cząstki)
        List<Subtask> subtasks = Subtask.getAllSubtasks();
        System.out.println("Pobrano cząstek: " + (subtasks != null ? subtasks.size() : 0)); // Dodaj tę linię
        if (subtasks != null) {
            for (Subtask subtask : subtasks) {
                if (subtask.getDueDate() != null) {
                    deadlines.add(new Deadline(subtask.getName(), subtask.getDueDate().toLocalDate(), "cząstka", null, null));
                }
            }
        }

        System.out.println("Łączna liczba deadline'ów: " + deadlines.size()); // Dodaj tę linię

        return deadlines;
    }
}