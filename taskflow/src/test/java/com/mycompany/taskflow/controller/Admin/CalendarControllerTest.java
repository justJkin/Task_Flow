package com.mycompany.taskflow.controller.Admin;

import com.mycompany.taskflow.model.Admin.Deadline;
import com.mycompany.taskflow.model.Admin.Project;
import com.mycompany.taskflow.model.Admin.Subtask;
import com.mycompany.taskflow.model.Admin.Task;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class CalendarControllerTest {

    private CalendarController controller;

    @BeforeEach
    void setUp() {
        controller = new CalendarController();
    }

    @Test
    void testGetDeadlineColor_ReturnsCorrectColor() {
        assertEquals(Color.GREEN, controller.getDeadlineColor("cząstka"));
        assertEquals(Color.YELLOW, controller.getDeadlineColor("zadanie"));
        assertEquals(Color.BLUE, controller.getDeadlineColor("milestone"));
        assertEquals(Color.RED, controller.getDeadlineColor("projekt"));
        assertEquals(Color.GRAY, controller.getDeadlineColor("nieznany"));
    }

    @Test
    void testGetDeadlinesForDate_FiltersCorrectly() {
        LocalDate today = LocalDate.now();
        Deadline d1 = new Deadline("Task 1", today, "zadanie", null, null);
        Deadline d2 = new Deadline("Task 2", today.minusDays(1), "projekt", null, null);
        controller.allDeadlines = Arrays.asList(d1, d2);

        List<Deadline> result = controller.getDeadlinesForDate(today);
        assertEquals(1, result.size());
        assertEquals("Task 1", result.get(0).getName());
    }

    @Test
    void testFormatDeadline_ReturnsCorrectString() {
        LocalDate date = LocalDate.of(2025, 5, 20);
        Deadline deadline = new Deadline("Task XYZ", date, "zadanie", "Kowalski", List.of("DevTeam"));
        String formatted = controller.formatDeadline(deadline);
        assertTrue(formatted.contains("Task XYZ"));
        assertTrue(formatted.contains("20.05.2025"));
        assertTrue(formatted.contains("Kowalski"));
        assertTrue(formatted.contains("DevTeam"));
    }

    @Test
    void testLoadDeadlines_ReturnsCombinedData() {
        Project p = new Project();
        p.setName("Project A");
        p.setEndDate(java.sql.Date.valueOf(LocalDate.of(2025, 6, 1)));

        Task t = new Task();
        t.setName("Task A");
        t.setDueDate(java.sql.Date.valueOf(LocalDate.of(2025, 6, 2)));

        Subtask s = new Subtask();
        s.setName("Subtask A");
        s.setDueDate(java.sql.Date.valueOf(LocalDate.of(2025, 6, 3)));

        try (MockedStatic<Project> projectMock = mockStatic(Project.class);
             MockedStatic<Task> taskMock = mockStatic(Task.class);
             MockedStatic<Subtask> subtaskMock = mockStatic(Subtask.class)) {

            projectMock.when(Project::getAllProjects).thenReturn(List.of(p));
            taskMock.when(Task::getAllTasks).thenReturn(List.of(t));
            subtaskMock.when(Subtask::getAllSubtasks).thenReturn(List.of(s));

            List<Deadline> result = controller.loadDeadlines();
            assertEquals(3, result.size());
            assertTrue(result.stream().anyMatch(d -> d.getName().equals("Project A") && d.getType().equals("projekt")));
            assertTrue(result.stream().anyMatch(d -> d.getName().equals("Task A") && d.getType().equals("zadanie")));
            assertTrue(result.stream().anyMatch(d -> d.getName().equals("Subtask A") && d.getType().equals("cząstka")));
        }
    }

    @Test
    void testGetDeadlinesForDate_WithEmptyListReturnsEmpty() {
        controller.allDeadlines = List.of();
        List<Deadline> result = controller.getDeadlinesForDate(LocalDate.now());
        assertTrue(result.isEmpty());
    }

    @Test
    void testFormatDeadline_WithoutOptionalFields() {
        Deadline deadline = new Deadline("Minimal", LocalDate.of(2025, 12, 1), "zadanie", null, null);
        String result = controller.formatDeadline(deadline);
        assertTrue(result.contains("Minimal"));
        assertTrue(result.contains("01.12.2025"));
        assertFalse(result.contains("Użytkownik"));
        assertFalse(result.contains("Zespół"));
    }
}
