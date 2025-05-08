package com.mycompany.taskflow.model.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskAssignmentTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    // Mockowanie statycznej metody DatabaseModel.connect()
    private MockedStatic<DatabaseModel> mockedDatabaseModel;

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this); // Inicjalizuje mocki

        // Rozpocznij mockowanie statycznej klasy DatabaseModel
        mockedDatabaseModel = mockStatic(DatabaseModel.class);

        // Skonfiguruj mock DatabaseModel.connect()
        mockedDatabaseModel.when(DatabaseModel::connect).thenReturn(mockConnection);

        // Skonfiguruj mock Connection do tworzenia PreparedStatement
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Skonfiguruj mock PreparedStatement do wykonywania zapytań i zwracania ResultSet
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Domyślna konfiguracja mockResultSet: brak wyników
        when(mockResultSet.next()).thenReturn(false); // Domyślnie brak kolejnych wierszy
    }

    // Ważne: Po każdym teście zakończ mockowanie statyczne
    @AfterEach
    public void tearDown() {
        mockedDatabaseModel.close();
    }

    // --- Testy dla metody getAssignment ---

    @Test
    void testGetAssignment_Success() throws SQLException {
        // Skonfiguruj mockResultSet, aby zwrócił przykładową datę przypisania
        when(mockResultSet.next()).thenReturn(true); // Znaleziono jedno przypisanie
        when(mockResultSet.getTimestamp("assigned_at")).thenReturn(Timestamp.valueOf("2025-01-01 12:00:00"));

        int userIdToFind = 1;
        int taskIdToFind = 10;
        TaskAssignment assignment = TaskAssignment.getAssignment(userIdToFind, taskIdToFind);

        // Weryfikuj, czy zapytanie zostało wykonane z poprawnymi ID
        verify(mockConnection).prepareStatement("SELECT assigned_at FROM task_assignment WHERE user_id = ? AND task_id = ?");
        verify(mockPreparedStatement).setInt(1, userIdToFind);
        verify(mockPreparedStatement).setInt(2, taskIdToFind);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócony obiekt TaskAssignment jest poprawny
        assertNotNull(assignment);
        assertEquals(userIdToFind, assignment.getUserId());
        assertEquals(taskIdToFind, assignment.getTaskId());
        assertEquals(Timestamp.valueOf("2025-01-01 12:00:00"), assignment.getAssignedAt());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetAssignment_NotFound() throws SQLException {
        // Domyślna konfiguracja mockResultSet (next() zwraca false) symuluje brak wyników

        int userIdToFind = 99;
        int taskIdToFind = 999;
        TaskAssignment assignment = TaskAssignment.getAssignment(userIdToFind, taskIdToFind);

        // Weryfikuj, czy zapytanie zostało wykonane
        verify(mockConnection).prepareStatement("SELECT assigned_at FROM task_assignment WHERE user_id = ? AND task_id = ?");
        verify(mockPreparedStatement).setInt(1, userIdToFind);
        verify(mockPreparedStatement).setInt(2, taskIdToFind);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócony obiekt TaskAssignment to null
        assertNull(assignment);

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetAssignment_SQLException() throws SQLException {
        // Skonfiguruj mock PreparedStatement, aby rzucił SQLException przy wywołaniu executeQuery
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQLException"));

        int userIdToFind = 1;
        int taskIdToFind = 10;
        TaskAssignment assignment = TaskAssignment.getAssignment(userIdToFind, taskIdToFind);

        // Weryfikuj, czy metoda złapała wyjątek i zwróciła null
        assertNull(assignment);

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte (nawet w przypadku błędu)
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
        // resultSet nie zostanie utworzony, więc nie będzie zamknięty
    }

    // --- Testy dla getterów i setterów ---

    @Test
    void testGetterAndSetter() {
        TaskAssignment assignment = new TaskAssignment();

        // Ustawianie wartości za pomocą setterów
        int userId = 2;
        int taskId = 20;
        Timestamp assignedAt = Timestamp.valueOf("2025-02-01 10:00:00");

        assignment.setUserId(userId);
        assignment.setTaskId(taskId);
        assignment.setAssignedAt(assignedAt);

        // Sprawdzanie wartości za pomocą getterów
        assertEquals(userId, assignment.getUserId());
        assertEquals(taskId, assignment.getTaskId());
        assertEquals(assignedAt, assignment.getAssignedAt());
    }
}