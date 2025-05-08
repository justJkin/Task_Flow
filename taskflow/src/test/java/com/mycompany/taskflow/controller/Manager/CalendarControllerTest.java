package com.mycompany.taskflow.controller.Manager;

import com.mycompany.taskflow.model.DatabaseModel;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll; // Importuj BeforeAll
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarControllerTest {

    @Mock
    private GridPane mockCalendarGrid;
    @Mock
    private Label mockMonthYearLabel;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private ObservableList<Node> mockGridChildren; // To mock calendarGrid.getChildren()

    @InjectMocks
    private CalendarController calendarController;

    // STATYCZNA METODA DO INICJALIZACJI PLATFORMY JAVAFX
    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platforma mogła już być zainicjowana w innym miejscu,
            // np. przez inne testy w tej samej testowej przebudowie.
            // Możemy zignorować ten błąd, jeśli platforma już działa.
            if (!e.getMessage().contains("Toolkit not initialized")) {
                throw e; // Rzuć wyjątek, jeśli to inny błąd IllegalStateException
            }
            // Sprawdź, czy platforma jest już uruchomiona
            if (!Platform.isFxApplicationThread()) {
                // Jeśli nie jesteśmy w wątku aplikacji FX, spróbujmy ją uruchomić
                // lub poczekać, aż będzie gotowa, jeśli uruchomiono ją gdzie indziej
                // Ta prosta obsługa może wymagać dostosowania w bardziej złożonych scenariuszach
                try {
                    Platform.runLater(() -> {}); // Proba wykonania na wątku FX, by sprawdzić gotowość
                } catch (IllegalStateException ex) {
                    // Nadal problem z inicjalizacją, coś jest nie tak
                    System.err.println("JavaFX Platform still not initialized after catching IllegalStateException.");
                    ex.printStackTrace();
                    throw ex;
                }
            }
        }
    }


    @BeforeEach
    void setUp() {
        // Initialize mocks. @ExtendWith(MockitoExtension.class) does this automatically.
        // We also need to mock the static methods. This will be done within the tests
        // that call them, using MockedStatic.
        lenient().when(mockCalendarGrid.getChildren()).thenReturn(mockGridChildren);
    }

    @Test
    void testInitialize() {
        // Mock static methods within this test
        try (MockedStatic<DatabaseModel> mockedDatabaseModel = mockStatic(DatabaseModel.class);
             MockedStatic<Platform> mockedPlatform = mockStatic(Platform.class)) { // Nadal mockujemy Platformę, aby przechwycić Runnable

            // Configure mocks for loadAllEvents
            mockedDatabaseModel.when(DatabaseModel::connect).thenReturn(mockConnection);
            // Configure behavior for executeQuery(). We'll use lenient for methods not strictly needed in *this* test.
            lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            lenient().when(mockResultSet.next()).thenReturn(false); // No results for loading events

            // Capture the runnable passed to Platform.runLater and execute it immediately
            ArgumentCaptor<Runnable> platformRunnableCaptor = ArgumentCaptor.forClass(Runnable.class);
            mockedPlatform.when(() -> Platform.runLater(platformRunnableCaptor.capture())).thenAnswer(invocation -> {
                Runnable runnable = platformRunnableCaptor.getValue();
                runnable.run(); // Execute the runnable immediately in the test thread
                return null;
            });

            // Call the method under test
            calendarController.initialize(mock(URL.class), mock(ResourceBundle.class));

            // Verify that loadAllEvents and populateCalendar (indirectly via Platform.runLater) were called
            // Verifying initialize calls loadAllEvents and populateCalendar implicitly by checking the mocks.
            // We can verify that DatabaseModel.connect() was called, indicating loadAllEvents started.
            mockedDatabaseModel.verify(DatabaseModel::connect, times(3)); // Called by loadSubtask, loadTask, loadMilestone

            // Verify actions within populateCalendar (executed via the captured runnable)
            verify(mockGridChildren).clear();
            verify(mockMonthYearLabel).setText(anyString());
            // Verifying additions to the grid depends on the month and if there are events,
            // which are handled in testPopulateCalendar.
        } catch (SQLException e) {
            fail("SQLException occurred during testInitialize: " + e.getMessage());
        }
    }


    @Test
    void testLoadSubtaskEvents() {
        try (MockedStatic<DatabaseModel> mockedDatabaseModel = mockStatic(DatabaseModel.class)) {
            mockedDatabaseModel.when(DatabaseModel::connect).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            // Simulate resultSet behavior
            when(mockResultSet.next()).thenReturn(true, true, false); // Two rows, then end
            when(mockResultSet.getString("subtask_name")).thenReturn("Subtask 1", "Subtask 2");
            when(mockResultSet.getDate("due_date")).thenReturn(
                    Date.valueOf(LocalDate.of(2024, 12, 15)),
                    Date.valueOf(LocalDate.of(2024, 12, 20))
            );

            // Use reflection or a helper method to call the private method
            callPrivateMethod(calendarController, "loadSubtaskEvents");

            // Verify dailyEvents contains the loaded subtasks
            var dailyEvents = (Map<LocalDate, List<String>>) getPrivateField(calendarController, "dailyEvents");
            assertNotNull(dailyEvents);
            assertTrue(dailyEvents.containsKey(LocalDate.of(2024, 12, 15)));
            assertTrue(dailyEvents.containsKey(LocalDate.of(2024, 12, 20)));
            assertEquals(1, dailyEvents.get(LocalDate.of(2024, 12, 15)).size());
            assertEquals("Cząstka: Subtask 1", dailyEvents.get(LocalDate.of(2024, 12, 15)).get(0));
            assertEquals(1, dailyEvents.get(LocalDate.of(2024, 12, 20)).size());
            assertEquals("Cząstka: Subtask 2", dailyEvents.get(LocalDate.of(2024, 12, 20)).get(0));

            // Verify resources were closed
            verify(mockResultSet).close();
            verify(mockPreparedStatement).close();
            verify(mockConnection).close();

        } catch (Exception e) {
            fail("Exception occurred during testLoadSubtaskEvents: " + e.getMessage());
        }
    }

    @Test
    void testLoadTaskEvents() {
        try (MockedStatic<DatabaseModel> mockedDatabaseModel = mockStatic(DatabaseModel.class)) {
            mockedDatabaseModel.when(DatabaseModel::connect).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            // Simulate resultSet behavior
            when(mockResultSet.next()).thenReturn(true, false); // One row, then end
            when(mockResultSet.getString("task_name")).thenReturn("Task Alpha");
            when(mockResultSet.getDate("due_date")).thenReturn(
                    Date.valueOf(LocalDate.of(2025, 1, 10))
            );

            // Use reflection or a helper method to call the private method
            callPrivateMethod(calendarController, "loadTaskEvents");

            // Verify dailyEvents contains the loaded tasks
            var dailyEvents = (Map<LocalDate, List<String>>) getPrivateField(calendarController, "dailyEvents");
            assertNotNull(dailyEvents);
            assertTrue(dailyEvents.containsKey(LocalDate.of(2025, 1, 10)));
            assertEquals(1, dailyEvents.get(LocalDate.of(2025, 1, 10)).size());
            assertEquals("Zadanie: Task Alpha", dailyEvents.get(LocalDate.of(2025, 1, 10)).get(0));

            // Verify resources were closed
            verify(mockResultSet).close();
            verify(mockPreparedStatement).close();
            verify(mockConnection).close();

        } catch (Exception e) {
            fail("Exception occurred during testLoadTaskEvents: " + e.getMessage());
        }
    }

    @Test
    void testLoadMilestoneEvents() {
        try (MockedStatic<DatabaseModel> mockedDatabaseModel = mockStatic(DatabaseModel.class)) {
            mockedDatabaseModel.when(DatabaseModel::connect).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            // Simulate resultSet behavior
            when(mockResultSet.next()).thenReturn(true, true, false); // Two rows, then end
            when(mockResultSet.getString("name")).thenReturn("Milestone X", "Milestone Y");
            when(mockResultSet.getTimestamp("created_at")).thenReturn(
                    Timestamp.valueOf(LocalDateTime.of(2024, 11, 5, 10, 0)),
                    Timestamp.valueOf(LocalDateTime.of(2024, 11, 5, 15, 30)) // Same day to test list handling
            );

            // Use reflection or a helper method to call the private method
            callPrivateMethod(calendarController, "loadMilestoneEvents");

            // Verify dailyEvents contains the loaded milestones
            var dailyEvents = (Map<LocalDate, List<String>>) getPrivateField(calendarController, "dailyEvents");
            assertNotNull(dailyEvents);
            assertTrue(dailyEvents.containsKey(LocalDate.of(2024, 11, 5)));
            assertEquals(2, dailyEvents.get(LocalDate.of(2024, 11, 5)).size());
            assertTrue(dailyEvents.get(LocalDate.of(2024, 11, 5)).contains("Kamień Milowy: Milestone X"));
            assertTrue(dailyEvents.get(LocalDate.of(2024, 11, 5)).contains("Kamień Milowy: Milestone Y"));

            // Verify resources were closed
            verify(mockResultSet).close();
            verify(mockPreparedStatement).close();
            verify(mockConnection).close();

        } catch (Exception e) {
            fail("Exception occurred during testLoadMilestoneEvents: " + e.getMessage());
        }
    }

    @Test
    void testLoadAllEvents() {
        // Use reflection to access and verify state changes
        try (MockedStatic<DatabaseModel> mockedDatabaseModel = mockStatic(DatabaseModel.class)) {
            mockedDatabaseModel.when(DatabaseModel::connect).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false); // No results for any query

            // Add some dummy events to simulate previous state
            Map<LocalDate, List<String>> dailyEvents = new java.util.HashMap<>();
            dailyEvents.put(LocalDate.now(), new ArrayList<>(List.of("Dummy Event")));
            setPrivateField(calendarController, "dailyEvents", dailyEvents);
            assertFalse(dailyEvents.isEmpty()); // Ensure it's not empty before calling the method

            // Use reflection or a helper method to call the private method
            callPrivateMethod(calendarController, "loadAllEvents");

            // Verify dailyEvents is cleared and loading methods were called
            dailyEvents = (Map<LocalDate, List<String>>) getPrivateField(calendarController, "dailyEvents");
            assertNotNull(dailyEvents);
            assertTrue(dailyEvents.isEmpty()); // Verify it was cleared

            // Verify connect was called three times (once for each load method)
            mockedDatabaseModel.verify(DatabaseModel::connect, times(3));

            // Verify executeQuery was called three times
            verify(mockPreparedStatement, times(3)).executeQuery();

        } catch (Exception e) {
            fail("Exception occurred during testLoadAllEvents: " + e.getMessage());
        }
    }

    @Test
    void testPopulateCalendar_noEvents() {
        // Mock Platform.runLater to execute the runnable immediately
        try (MockedStatic<Platform> mockedPlatform = mockStatic(Platform.class)) {
            ArgumentCaptor<Runnable> platformRunnableCaptor = ArgumentCaptor.forClass(Runnable.class);
            mockedPlatform.when(() -> Platform.runLater(platformRunnableCaptor.capture())).thenAnswer(invocation -> {
                Runnable runnable = platformRunnableCaptor.getValue();
                runnable.run(); // Execute the runnable immediately
                return null;
            });

            YearMonth testMonth = YearMonth.of(2024, 12); // December 2024 starts on Sunday

            // Ensure dailyEvents is empty for this test
            setPrivateField(calendarController, "dailyEvents", new java.util.HashMap<>());

            // Use reflection or a helper method to call the private method
            callPrivateMethod(calendarController, "populateCalendar", testMonth);

            // Verify grid was cleared and month/year label was set
            verify(mockGridChildren).clear();
            verify(mockMonthYearLabel).setText("grudnia 2024"); // December in Polish

            // Verify that a Label (day) was added for each day of the month
            int daysInMonth = testMonth.lengthOfMonth();
            // Number of cells populated will be days in month.
            // December 2024 starts on Sunday (day 7). Grid starts at col 0.
            // dayOfWeek = 7, col = 6. First day at (0,6).
            // Number of cells populated = daysInMonth = 31.
            verify(mockCalendarGrid, times(daysInMonth)).add(any(Label.class), anyInt(), anyInt());

            // Verify no VBoxes were added (since there are no events)
            verify(mockCalendarGrid, never()).add(any(VBox.class), anyInt(), anyInt());

        } catch (Exception e) {
            fail("Exception occurred during testPopulateCalendar_noEvents: " + e.getMessage());
        }
    }

    @Test
    void testPopulateCalendar_withEvents() {
        // Mock Platform.runLater to execute the runnable immediately
        try (MockedStatic<Platform> mockedPlatform = mockStatic(Platform.class)) {
            ArgumentCaptor<Runnable> platformRunnableCaptor = ArgumentCaptor.forClass(Runnable.class);
            mockedPlatform.when(() -> Platform.runLater(platformRunnableCaptor.capture())).thenAnswer(invocation -> {
                Runnable runnable = platformRunnableCaptor.getValue();
                runnable.run(); // Execute the runnable immediately
                return null;
            });

            YearMonth testMonth = YearMonth.of(2025, 1); // January 2025 starts on Wednesday
            LocalDate eventDate1 = LocalDate.of(2025, 1, 15); // Wednesday
            LocalDate eventDate2 = LocalDate.of(2025, 1, 20); // Monday

            // Populate dailyEvents with test data
            Map<LocalDate, List<String>> dailyEvents = new java.util.HashMap<>();
            dailyEvents.put(eventDate1, new ArrayList<>(List.of("Event A", "Event B")));
            dailyEvents.put(eventDate2, new ArrayList<>(List.of("Event C")));
            setPrivateField(calendarController, "dailyEvents", dailyEvents);

            // Use reflection or a helper method to call the private method
            callPrivateMethod(calendarController, "populateCalendar", testMonth);

            // Verify grid was cleared and month/year label was set
            verify(mockGridChildren).clear();
            verify(mockMonthYearLabel).setText("stycznia 2025"); // January in Polish

            int daysInMonth = testMonth.lengthOfMonth(); // 31 days
            int daysWithEvents = dailyEvents.size(); // 2 days

            // Total number of items added to the grid should be days in month.
            // Some days are Labels directly, some are VBoxes.
            verify(mockCalendarGrid, times(daysInMonth)).add(any(Node.class), anyInt(), anyInt());

            // Verify that VBoxes were added for the days with events
            verify(mockCalendarGrid, times(daysWithEvents)).add(any(VBox.class), anyInt(), anyInt());

            // Verify that Labels were added for days without events
            verify(mockCalendarGrid, times(daysInMonth - daysWithEvents)).add(any(Label.class), anyInt(), anyInt());

            // Although we can't easily inspect the content of the VBoxes added without a
            // more complex setup (like ArgumentCaptor for Node and then casting/inspecting),
            // verifying that VBoxes are added for the correct number of days with events
            // provides some confidence in the logic.

        } catch (Exception e) {
            fail("Exception occurred during testPopulateCalendar_withEvents: " + e.getMessage());
        }
    }

    @Test
    void testPreviousMonth() {
        // Mock populateCalendar to avoid testing its internal logic here
        CalendarController spyController = spy(calendarController);

        // Set initial YearMonth using reflection
        YearMonth initialMonth = YearMonth.of(2025, 3);
        setPrivateField(spyController, "currentYearMonth", initialMonth);

        doNothing().when(spyController).populateCalendar(any(YearMonth.class)); // Mock populateCalendar

        // Call the method under test
        spyController.previousMonth();

        // Verify currentYearMonth was decremented
        YearMonth expectedMonth = initialMonth.minusMonths(1);
        assertEquals(expectedMonth, getPrivateField(spyController, "currentYearMonth"));

        // Verify populateCalendar was called with the new month
        verify(spyController).populateCalendar(expectedMonth);
    }

    @Test
    void testNextMonth() {
        // Mock populateCalendar to avoid testing its internal logic here
        CalendarController spyController = spy(calendarController);

        // Set initial YearMonth using reflection
        YearMonth initialMonth = YearMonth.of(2025, 3);
        setPrivateField(spyController, "currentYearMonth", initialMonth);

        doNothing().when(spyController).populateCalendar(any(YearMonth.class)); // Mock populateCalendar

        // Call the method under test
        spyController.nextMonth();

        // Verify currentYearMonth was incremented
        YearMonth expectedMonth = initialMonth.plusMonths(1);
        assertEquals(expectedMonth, getPrivateField(spyController, "currentYearMonth"));

        // Verify populateCalendar was called with the new month
        verify(spyController).populateCalendar(expectedMonth);
    }

    // Helper methods for accessing private fields and methods using reflection

    private Object getPrivateField(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get private field: " + fieldName, e);
        }
    }

    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set private field: " + fieldName, e);
        }
    }

    private Object callPrivateMethod(Object obj, String methodName, Object... args) {
        try {
            Class<?>[] parameterTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    // Handle primitive types if necessary
                    if (args[i].getClass().equals(Integer.class)) parameterTypes[i] = int.class;
                    else if (args[i].getClass().equals(Long.class)) parameterTypes[i] = long.class;
                    else if (args[i].getClass().equals(Boolean.class)) parameterTypes[i] = boolean.class;
                    else if (args[i].getClass().equals(Double.class)) parameterTypes[i] = double.class;
                    else if (args[i].getClass().equals(Float.class)) parameterTypes[i] = float.class;
                    else if (args[i].getClass().equals(Character.class)) parameterTypes[i] = char.class;
                    else if (args[i].getClass().equals(Byte.class)) parameterTypes[i] = byte.class;
                    else if (args[i].getClass().equals(Short.class)) parameterTypes[i] = short.class;
                    else parameterTypes[i] = args[i].getClass();
                } else {
                    // Handle null arguments - this is tricky and might require more specific handling
                    // depending on the method signature. For simplicity, assuming no null primitives.
                    parameterTypes[i] = null; // This won't work directly, need to infer type or pass Class explicitly
                    throw new IllegalArgumentException("Cannot infer type for null argument at index " + i);
                }
            }

            // Find the method with matching name and parameter types
            java.lang.reflect.Method method = null;
            for (java.lang.reflect.Method m : obj.getClass().getDeclaredMethods()) {
                if (m.getName().equals(methodName) && m.getParameterCount() == args.length) {
                    boolean typesMatch = true;
                    Class<?>[] methodParameterTypes = m.getParameterTypes();
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] != null && !methodParameterTypes[i].isAssignableFrom(parameterTypes[i])) {
                            typesMatch = false;
                            break;
                        }
                        // Basic check for null if the parameter type is not primitive
                        if (args[i] == null && methodParameterTypes[i].isPrimitive()) {
                            typesMatch = false; // Cannot pass null to a primitive
                            break;
                        }
                    }
                    if (typesMatch) {
                        method = m;
                        break;
                    }
                }
            }

            if (method == null) {
                throw new NoSuchMethodException("Could not find method " + methodName + " with " + args.length + " parameters.");
            }

            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call private method: " + methodName, e);
        }
    }
}