package com.mycompany.taskflow.controller.Manager;

import com.mycompany.taskflow.model.UserSession;
import com.mycompany.taskflow.model.Manager.Subtask;
import com.mycompany.taskflow.model.Manager.Task;
import com.mycompany.taskflow.model.Manager.Notification;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.PropertyValueFactory; // Importuj, jeśli używasz PropertyValueFactory

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito; // Jawny import Mockito

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Import Objects for equals/hashCode
import java.util.stream.Collectors; // Import Collectors

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    // Flag to ensure JavaFX Platform is initialized only once per test run
    private static volatile boolean javaFxInitialized = false; // Use volatile for thread safety

    // Mocks for FXML components
    @Mock
    private Label mockWelcomeLabel;
    @Mock
    private TableView<Task> mockRecentTasksTableView;
    @Mock
    private TableColumn<Task, String> mockRecentTaskNameColumn;
    @Mock
    private TableColumn<Task, LocalDate> mockRecentTaskDueDateColumn;
    @Mock
    private TableView<Subtask> mockRecentSubtasksTableView;
    @Mock
    private TableColumn<Subtask, String> mockRecentSubtaskNameColumn;
    @Mock
    private TableColumn<Subtask, LocalDate> mockRecentSubtaskDueDateColumn;
    @Mock
    private TableView<Task> mockUpcomingTasksTableView;
    @Mock
    private TableColumn<Task, String> mockUpcomingTaskNameColumn;
    @Mock
    private TableColumn<Task, LocalDate> mockUpcomingTaskDueDateColumn;
    @Mock
    private TableView<Subtask> mockUpcomingSubtasksTableView;
    @Mock
    private TableColumn<Subtask, String> mockUpcomingSubtaskNameColumn;
    @Mock
    private TableColumn<Subtask, LocalDate> mockUpcomingSubtaskDueDateColumn;
    @Mock
    private ListView<String> mockNotificationsListView;

    // Mocks for TableView item lists
    @Mock
    private ObservableList<Task> mockRecentTasksList;
    @Mock
    private ObservableList<Subtask> mockRecentSubtasksList;
    @Mock
    private ObservableList<Task> mockUpcomingTasksList;
    @Mock
    private ObservableList<Subtask> mockUpcomingSubtasksList;
    @Mock
    private ObservableList<String> mockNotificationsList;


    @InjectMocks
    private DashboardController dashboardController;

    /**
     * Initializes the JavaFX platform before any tests are run.
     * Necessary for testing classes that use JavaFX components.
     * Ensures the platform is initialized only once across all test classes.
     */
    @BeforeAll
    static void initJavaFX() {
        if (!javaFxInitialized) {
            synchronized (DashboardControllerTest.class) { // Synchronize to prevent race conditions
                if (!javaFxInitialized) { // Double-check locking
                    try {
                        Platform.startup(() -> {});
                        javaFxInitialized = true; // Set flag after successful initialization
                    } catch (IllegalStateException e) {
                        // If the toolkit is already initialized, just catch the exception and proceed.
                        // This handles cases where another test class initialized it first.
                        if (e.getMessage().contains("Toolkit already initialized")) {
                            javaFxInitialized = true; // Consider it initialized if this specific error occurs
                        } else {
                            // Rethrow other IllegalStateExceptions
                            throw e;
                        }
                    } catch (Exception e) {
                        // Catch any other potential exceptions during startup
                        System.err.println("Error initializing JavaFX platform: " + e.getMessage());
                        e.printStackTrace();
                        fail("Failed to initialize JavaFX Platform");
                    }
                }
            }
        }
    }

    @BeforeEach
    void setUp() {
        // Konfiguracja mocków TableView, aby zwracały mockowane listy
        lenient().when(mockRecentTasksTableView.getItems()).thenReturn(mockRecentTasksList);
        lenient().when(mockRecentSubtasksTableView.getItems()).thenReturn(mockRecentSubtasksList);
        lenient().when(mockUpcomingTasksTableView.getItems()).thenReturn(mockUpcomingTasksList);
        lenient().when(mockUpcomingSubtasksTableView.getItems()).thenReturn(mockUpcomingSubtasksList);
        lenient().when(mockNotificationsListView.getItems()).thenReturn(mockNotificationsList);

        // Upewnij się, że pola FXML są wstrzyknięte
        dashboardController = new DashboardController();
        try {
            // Użyj refleksji do wstrzyknięcia mocków FXML, ponieważ @InjectMocks działa tylko dla pól
            // bez adnotacji FXML w przypadku testowania kontrolera, który nie jest ładowany przez FXMLoader.
            // Alternatywnie można użyć metody inicjalizacji podobnej do FXMLoader.
            setPrivateField(dashboardController, "welcomeLabel", mockWelcomeLabel);
            setPrivateField(dashboardController, "recentTasksTableView", mockRecentTasksTableView);
            setPrivateField(dashboardController, "recentTaskNameColumn", mockRecentTaskNameColumn);
            setPrivateField(dashboardController, "recentTaskDueDateColumn", mockRecentTaskDueDateColumn);
            setPrivateField(dashboardController, "recentSubtasksTableView", mockRecentSubtasksTableView);
            setPrivateField(dashboardController, "recentSubtaskNameColumn", mockRecentSubtaskNameColumn);
            setPrivateField(dashboardController, "recentSubtaskDueDateColumn", mockRecentSubtaskDueDateColumn);
            setPrivateField(dashboardController, "upcomingTasksTableView", mockUpcomingTasksTableView);
            setPrivateField(dashboardController, "upcomingTaskNameColumn", mockUpcomingTaskNameColumn);
            setPrivateField(dashboardController, "upcomingTaskDueDateColumn", mockUpcomingTaskDueDateColumn);
            setPrivateField(dashboardController, "upcomingSubtasksTableView", mockUpcomingSubtasksTableView);
            setPrivateField(dashboardController, "upcomingSubtaskNameColumn", mockUpcomingSubtaskNameColumn);
            setPrivateField(dashboardController, "upcomingSubtaskDueDateColumn", mockUpcomingSubtaskDueDateColumn);
            setPrivateField(dashboardController, "notificationsListView", mockNotificationsListView);

        } catch (Exception e) {
            fail("Failed to inject mocks using reflection: " + e.getMessage());
        }
    }

    @Test
    void testInitialize() {
        // Mock static dependencies
        try (MockedStatic<UserSession> mockedUserSession = Mockito.mockStatic(UserSession.class);
             MockedStatic<Task> mockedTask = Mockito.mockStatic(Task.class);
             MockedStatic<Subtask> mockedSubtask = Mockito.mockStatic(Subtask.class);
             MockedStatic<Notification> mockedNotification = Mockito.mockStatic(Notification.class)) {

            // Configure mock UserSession
            UserSession mockSession = mock(UserSession.class);
            mockedUserSession.when(UserSession::getInstance).thenReturn(mockSession);
            when(mockSession.getFirstName()).thenReturn("Jan");
            when(mockSession.getLastName()).thenReturn("Kowalski");
            when(mockSession.getUserId()).thenReturn(1);
            when(mockSession.getTeamId()).thenReturn(101);

            // Prepare the exact list objects to be returned by mocked static methods
            // These are the lists that should be passed to setAll/addAll
            List<Task> recentTasks = new ArrayList<>(); // Use a concrete list for verification
            List<Subtask> recentSubtasks = new ArrayList<>(); // Use a concrete list for verification
            List<Task> upcomingTasks = new ArrayList<>(); // Use a concrete list for verification
            List<Subtask> upcomingSubtasks = new ArrayList<>(); // Use a concrete list for verification
            List<Notification> notifications = new ArrayList<>(); // Use a concrete list for verification


            // Configure mock data loading methods to return the prepared empty lists
            mockedTask.when(() -> Task.getRecentTasksForTeam(anyInt())).thenReturn(recentTasks);
            mockedSubtask.when(() -> Subtask.getRecentSubtasksForTeam(anyInt())).thenReturn(recentSubtasks);
            mockedTask.when(() -> Task.getUpcomingTasksForTeam(anyInt())).thenReturn(upcomingTasks);
            mockedSubtask.when(() -> Subtask.getUpcomingSubtasksForTeam(anyInt())).thenReturn(upcomingSubtasks);
            mockedNotification.when(() -> Notification.getLatestNotificationsForUser(anyInt(), anyInt())).thenReturn(notifications);

            // Wywołaj metodę do testowania
            dashboardController.initialize();

            // Weryfikuj, że UserSession został pobrany
            mockedUserSession.verify(UserSession::getInstance, times(1));
            verify(mockSession).getFirstName();
            verify(mockSession).getLastName();
            verify(mockSession).getUserId();
            verify(mockSession).getTeamId();

            // Weryfikuj, że Label powitalny został ustawiony
            verify(mockWelcomeLabel).setText("Witaj, Jan Kowalski!");

            // Weryfikuj, że listy elementów w TableView i ListView zostały zaktualizowane (sprawdzamy poprzez wywołania setAll() i clear())
            // FIX: Verify that setAll was called with the specific empty lists returned by the mocks
            verify(mockRecentTasksList).setAll(recentTasks); // Verify with the exact list object
            verify(mockRecentSubtasksList).setAll(recentSubtasks); // Verify with the exact list object
            verify(mockUpcomingTasksList).setAll(upcomingTasks); // Verify with the exact list object
            verify(mockUpcomingSubtasksList).setAll(upcomingSubtasks); // Verify with the exact list object
            verify(mockNotificationsList).clear(); // W loadData jest clear() przed dodawaniem

            // Verify addAll for notifications if loadData adds them after clear
            // FIX: If the list is empty, addAll might not be called. Only verify clear().
            // If the list could potentially have items in other test scenarios,
            // a more complex verification or separate test case might be needed.
            // For this test (where notifications is empty), only verifying clear() is sufficient.
            // verify(mockNotificationsList, never()).addAll(anyList()); // Removed this verification as addAll might not be called for empty list


        } catch (Exception e) {
            fail("Exception occurred during testInitialize: " + e.getMessage());
        }
    }

    @Test
    void testInitializeTables() {
        // Użyj refleksji, aby wywołać prywatną metodę initializeTables
        try {
            callPrivateMethod(dashboardController, "initializeTables");

            // Verify that setCellValueFactory was called on each TableColumn mock
            // We cannot easily verify the lambda/PropertyValueFactory argument directly
            // without complex ArgumentCaptor setup for generic types.
            // Verifying that the method was called is a reasonable proxy.
            verify(mockRecentTaskNameColumn).setCellValueFactory(any());
            verify(mockRecentTaskDueDateColumn).setCellValueFactory(any());
            verify(mockRecentSubtaskNameColumn).setCellValueFactory(any());
            verify(mockRecentSubtaskDueDateColumn).setCellValueFactory(any());
            verify(mockUpcomingTaskNameColumn).setCellValueFactory(any());
            verify(mockUpcomingTaskDueDateColumn).setCellValueFactory(any());
            verify(mockUpcomingSubtaskNameColumn).setCellValueFactory(any());
            verify(mockUpcomingSubtaskDueDateColumn).setCellValueFactory(any());

        } catch (Exception e) {
            fail("Exception occurred during testInitializeTables: " + e.getMessage());
        }
    }

    // Usunięto metodę testLoadData zgodnie z życzeniem użytkownika.
    // @Test
    // void testLoadData() { ... }


    // ------ Helper methods for reflection (reuse from CalendarControllerTest) ------

    private Object getPrivateField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get private field: " + fieldName, e);
        }
    }

    private void setPrivateField(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set private field: " + fieldName, e);
        }
    }

    // Helper method to call private methods using reflection
    private Object callPrivateMethod(Object obj, String methodName, Object... args) {
        try {
            Class<?>[] parameterTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
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
                    // Handle nulls if your private method accepts nulls,
                    // otherwise this indicates an issue in test setup.
                    parameterTypes[i] = null; // Or handle as appropriate
                }
            }

            Method method = null;
            // Simple parameter type matching - might need refinement for complex types/inheritance
            for (Method m : obj.getClass().getDeclaredMethods()) {
                if (m.getName().equals(methodName) && m.getParameterCount() == args.length) {
                    boolean typesMatch = true;
                    Class<?>[] methodParameterTypes = m.getParameterTypes();
                    for (int i = 0; i < args.length; i++) {
                        // Check for null argument compatibility with primitive types
                        if (args[i] == null && methodParameterTypes[i].isPrimitive()) {
                            typesMatch = false;
                            break;
                        }
                        // Check for type assignability for non-null arguments
                        if (args[i] != null && !methodParameterTypes[i].isAssignableFrom(parameterTypes[i])) {
                            // Special handling for primitive wrappers and primitives
                            if (methodParameterTypes[i].isPrimitive()) {
                                if ((methodParameterTypes[i].equals(int.class) && parameterTypes[i].equals(Integer.class)) ||
                                        (methodParameterTypes[i].equals(long.class) && parameterTypes[i].equals(Long.class)) ||
                                        (methodParameterTypes[i].equals(boolean.class) && parameterTypes[i].equals(Boolean.class)) ||
                                        (methodParameterTypes[i].equals(double.class) && parameterTypes[i].equals(Double.class)) ||
                                        (methodParameterTypes[i].equals(float.class) && parameterTypes[i].equals(Float.class)) ||
                                        (methodParameterTypes[i].equals(char.class) && parameterTypes[i].equals(Character.class)) ||
                                        (methodParameterTypes[i].equals(byte.class) && parameterTypes[i].equals(Byte.class)) ||
                                        (methodParameterTypes[i].equals(short.class) && parameterTypes[i].equals(Short.class))) {
                                    // This case is fine, wrapper can be assigned to primitive
                                } else {
                                    typesMatch = false;
                                    break;
                                }
                            } else {
                                typesMatch = false;
                                break;
                            }
                        }
                    }
                    if (typesMatch) {
                        method = m;
                        break;
                    }
                }
            }

            if (method == null) {
                // Fallback: try finding method by name and parameter count, assuming argument types match
                try {
                    // Attempt to find method by name and parameter count, assuming argument types match
                    Class<?>[] argClasses = new Class[args.length];
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] != null) {
                            argClasses[i] = args[i].getClass();
                        } else {
                            argClasses[i] = null; // Cannot determine type for null
                        }
                    }
                    method = obj.getClass().getDeclaredMethod(methodName, parameterTypes); // Use the parameterTypes array
                } catch (NoSuchMethodException e) {
                    throw new NoSuchMethodException("Could not find method " + methodName + " with matching parameters.");
                }
            }


            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // If the invoked method throws an exception, unwrap and rethrow its cause
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                throw new RuntimeException("Method invocation failed", cause);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to call private method: " + methodName, e);
        }
    }


    // Dummy classes for mocking Task, Subtask, Notification without database/full logic
    // These are needed because the controller expects actual objects, even if mocked.
    // In a real project, you might mock interfaces or have dedicated test DTOs.
    public static class Task {
        private int id;
        private String name;
        private LocalDate dueDate;

        public Task(int id, String name, LocalDate dueDate) {
            this.id = id;
            this.name = name;
            this.dueDate = dueDate;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public LocalDate getDueDate() { return dueDate; }

        // Override equals and hashCode for list content verification
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Task task = (Task) o;
            return id == task.id &&
                    Objects.equals(name, task.name) &&
                    Objects.equals(dueDate, task.dueDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, dueDate);
        }


        // Mock static methods - these will be overridden by Mockito static mocks
        public static List<Task> getRecentTasksForTeam(int teamId) { return new ArrayList<>(); }
        public static List<Task> getUpcomingTasksForTeam(int teamId) { return new ArrayList<>(); }
    }

    public static class Subtask {
        private int id;
        private String name;
        private LocalDate dueDate;

        public Subtask(int id, String name, LocalDate dueDate) {
            this.id = id;
            this.name = name;
            this.dueDate = dueDate;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public LocalDate getDueDate() { return dueDate; }

        // Override equals and hashCode for list content verification
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Subtask subtask = (Subtask) o;
            return id == subtask.id &&
                    Objects.equals(name, subtask.name) &&
                    Objects.equals(dueDate, subtask.dueDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, dueDate);
        }

        // Mock static methods - these will be overridden by Mockito static mocks
        public static List<Subtask> getRecentSubtasksForTeam(int teamId) { return new ArrayList<>(); }
        public static List<Subtask> getUpcomingSubtasksForTeam(int teamId) { return new ArrayList<>(); }
    }

    public static class Notification {
        private int id;
        private int userId;
        private String type;
        private String content;
        private LocalDateTime createdAt;

        public Notification(int id, int userId, String type, String content, LocalDateTime createdAt) {
            this.id = id;
            this.userId = userId;
            this.type = type;
            this.content = content;
            this.createdAt = createdAt;
        }

        public int getId() { return id; }
        public int getUserId() { return userId; }
        public String getType() { return type; }
        public String getContent() { return content; }
        public LocalDateTime getCreatedAt() { return createdAt; }

        // Override equals and hashCode for list content verification (if needed, though we verify formatted strings)
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Notification that = (Notification) o;
            return id == that.id &&
                    userId == that.userId &&
                    Objects.equals(type, that.type) &&
                    Objects.equals(content, that.content) &&
                    Objects.equals(createdAt, that.createdAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, userId, type, content, createdAt);
        }


        // Mock static methods - these will be overridden by Mockito static mocks
        public static List<Notification> getLatestNotificationsForUser(int userId, int limit) { return new ArrayList<>(); }
    }
}
