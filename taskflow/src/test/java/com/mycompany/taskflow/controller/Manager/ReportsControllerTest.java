package com.mycompany.taskflow.controller.Manager;

import com.mycompany.taskflow.model.DatabaseModel;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction; // Nadal potrzebne, jeśli mockujesz inne konstruktory, ale nie dla Statement/Connection
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ReportsController, focusing on the ranking generation logic.
 * These tests mock the JavaFX components and the database interactions.
 */
@ExtendWith(MockitoExtension.class)
class ReportsControllerTest {

    // Flag to ensure JavaFX Platform is initialized only once per test run
    private static volatile boolean javaFxInitialized = false; // Use volatile for thread safety

    // Mock for the FXML ListView component
    @Mock
    private ListView<String> mockRankingListView;

    // Mock for the ObservableList that backs the ListView
    @Mock
    private ObservableList<String> mockRankingList;

    // Inject mocks into the controller instance
    @InjectMocks
    private ReportsController reportsController;

    /**
     * Initializes the JavaFX platform before any tests are run.
     * Necessary for testing classes that use JavaFX components.
     * Ensures the platform is initialized only once across all test classes.
     */
    @BeforeAll
    static void initJavaFX() {
        if (!javaFxInitialized) {
            synchronized (ReportsControllerTest.class) { // Synchronize to prevent race conditions
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

    /**
     * Sets up the test environment before each test method.
     * Uses reflection to inject the mock ListView into the controller.
     */
    @BeforeEach
    void setUp() {
        // Use reflection to inject the mock ListView into the private rankingListView field
        try {
            setPrivateField(reportsController, "rankingListView", mockRankingListView);
        } catch (Exception e) {
            fail("Failed to inject mock rankingListView using reflection: " + e.getMessage());
        }

        // Configure mockRankingListView to return the mock ranking list
        lenient().when(mockRankingListView.getItems()).thenReturn(mockRankingList);
    }

    /**
     * Tests that the initialize method calls the generateRanking method.
     */
    @Test
    void initialize_callsGenerateRanking() {
        // Spy on the controller instance to verify calls to its methods
        ReportsController spyController = spy(reportsController);

        // Call the initialize method under test
        spyController.initialize();

        // Verify that generateRanking was called exactly once
        verify(spyController, times(1)).generateRanking();
    }

    /**
     * Tests that generateRanking clears the list, calls pobierzRankingUzytkownikow,
     * and adds the results to the ListView.
     */
    @Test
    void generateRanking_clearsListFetchesAndAddsRanking() {
        // Prepare dummy ranking data that the actual private method will return
        // FIX: Added extra space after first names to match the controller's concatenation logic
        List<String> expectedRankingData = new ArrayList<>();
        expectedRankingData.add("User A  - Ukończone zadania: 5"); // Note the extra space
        expectedRankingData.add("User B  - Ukończone zadania: 3"); // Note the extra space


        // Mock static DatabaseModel.connect()
        try (MockedStatic<DatabaseModel> mockedDatabaseModel = Mockito.mockStatic(DatabaseModel.class)) {

            // Mock the Connection, Statement, and ResultSet using Mockito.mock()
            Connection mockConnection = mock(Connection.class);
            Statement mockStatement = mock(Statement.class);
            ResultSet mockResultSet = mock(ResultSet.class);

            try {
                // Configure DatabaseModel.connect() to return the mock Connection
                mockedDatabaseModel.when(DatabaseModel::connect).thenReturn(mockConnection);

                // Configure the mock Connection to return the mock Statement
                when(mockConnection.createStatement()).thenReturn(mockStatement);

                // Configure the mock Statement to return the mock ResultSet when executeQuery is called
                when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

                // Configure the mock ResultSet to simulate rows and data
                when(mockResultSet.next()).thenReturn(true, true, false); // Simulate two rows
                when(mockResultSet.getString("first_name")).thenReturn("User A", "User B");
                when(mockResultSet.getString("last_name")).thenReturn("", ""); // Assuming last name might be empty
                when(mockResultSet.getInt("completed_tasks")).thenReturn(5, 3);

                // Call the method under test
                reportsController.generateRanking();

                // Verify that the ListView's items list was cleared
                verify(mockRankingList).clear();

                // Verify that the correct data was added to the ListView's items list
                verify(mockRankingList).addAll(expectedRankingData);

                // Verify that database resources were closed (implicitly by try-with-resources in the controller)
                // We can verify close() calls on the mocks
                verify(mockResultSet).close();
                verify(mockStatement).close();
                verify(mockConnection).close();

            } catch (SQLException e) {
                fail("SQLException occurred during generateRanking test: " + e.getMessage());
            }
        }
    }


    /**
     * Tests that the private pobierzRankingUzytkownikow method executes the correct SQL query
     * and processes the ResultSet correctly.
     */
    @Test
    void pobierzRankingUzytkownikow_executesCorrectQueryAndProcessesResults() {
        // Mock static DatabaseModel.connect()
        try (MockedStatic<DatabaseModel> mockedDatabaseModel = Mockito.mockStatic(DatabaseModel.class)) {

            // Mock the Connection, Statement, and ResultSet using Mockito.mock()
            Connection mockConnection = mock(Connection.class);
            Statement mockStatement = mock(Statement.class);
            ResultSet mockResultSet = mock(ResultSet.class);

            try {
                // Configure DatabaseModel.connect() to return the mock Connection
                mockedDatabaseModel.when(DatabaseModel::connect).thenReturn(mockConnection);

                // Configure the mock Connection to return the mock Statement
                when(mockConnection.createStatement()).thenReturn(mockStatement);

                // Configure the mock Statement to return the mock ResultSet when executeQuery is called
                when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

                // Configure the mock ResultSet to simulate rows and data
                when(mockResultSet.next()).thenReturn(true, true, false); // Simulate two rows
                when(mockResultSet.getString("first_name")).thenReturn("Jan", "Anna");
                when(mockResultSet.getString("last_name")).thenReturn("Kowalski", "Nowak");
                when(mockResultSet.getInt("completed_tasks")).thenReturn(10, 5);

                // Call the private method under test using reflection
                List<String> ranking = (List<String>) callPrivateMethod(reportsController, "pobierzRankingUzytkownikow");

                // Verify that DatabaseModel.connect() was called
                mockedDatabaseModel.verify(DatabaseModel::connect, times(1));

                // Verify that createStatement() was called on the Connection
                verify(mockConnection).createStatement();

                // Verify that executeQuery was called on the Statement with the correct SQL query
                String expectedQuery = "SELECT u.first_name, u.last_name, COUNT(t.id) AS completed_tasks " +
                        "FROM \"user\" u LEFT JOIN task t ON t.id = u.id " +
                        "WHERE t.status = 'Done' GROUP BY u.id ORDER BY COUNT(t.id) DESC";
                verify(mockStatement).executeQuery(expectedQuery);

                // Verify that the ResultSet methods were called to retrieve data
                verify(mockResultSet, times(3)).next(); // 2 true + 1 false
                verify(mockResultSet, times(2)).getString("first_name");
                verify(mockResultSet, times(2)).getString("last_name");
                verify(mockResultSet, times(2)).getInt("completed_tasks");

                // Verify the content and format of the returned ranking list
                assertNotNull(ranking);
                assertEquals(2, ranking.size());
                assertEquals("Jan Kowalski - Ukończone zadania: 10", ranking.get(0));
                assertEquals("Anna Nowak - Ukończone zadania: 5", ranking.get(1));

                // Verify that database resources were closed (implicitly by try-with-resources)
                // We can verify close() calls on the mocks
                verify(mockResultSet).close();
                verify(mockStatement).close();
                verify(mockConnection).close();


            } catch (SQLException e) {
                fail("SQLException occurred during pobierzRankingUzytkownikow test: " + e.getMessage());
            } catch (RuntimeException e) {
                fail("RuntimeException occurred during pobierzRankingUzytkownikow test: " + e.getMessage());
            }
        }
    }

    // Note: generateStatistics and generateReport methods are placeholders and
    // do not contain testable logic in the provided controller code.
    // Tests for these methods would be added once their implementation is complete.

    // ------ Helper methods for reflection (copied from DashboardControllerTest) ------

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
}
