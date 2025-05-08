package com.mycompany.taskflow.model.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskTest {

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

        // Domyślna konfiguracja dla executeUpdate (jeśli model miałby takie metody)
        // when(mockPreparedStatement.executeUpdate()).thenReturn(1);
    }

    // Ważne: Po każdym teście zakończ mockowanie statyczne
    @AfterEach
    public void tearDown() {
        mockedDatabaseModel.close();
    }

    // --- Testy dla metody getAllTasks ---

    @Test
    void testGetAllTasks_Success() throws SQLException {
        // Skonfiguruj mockResultSet, aby zwrócił przykładowe dane zadań
        when(mockResultSet.next()).thenReturn(true, true, false); // Symuluj 2 wiersze
        when(mockResultSet.getInt("id")).thenReturn(10, 20);
        when(mockResultSet.getString("name")).thenReturn("Zadanie X", "Zadanie Y");
        when(mockResultSet.getDate("due_date")).thenReturn(Date.valueOf("2025-10-10"), null); // Testowanie wartości NULL dla daty

        List<Task> tasks = Task.getAllTasks();

        // Weryfikuj, czy zapytanie SELECT zostało wykonane
        verify(mockConnection).prepareStatement("SELECT id, name, due_date FROM task");
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócona lista zadań jest poprawna
        assertNotNull(tasks);
        assertEquals(2, tasks.size());

        Task task1 = tasks.get(0);
        assertEquals(10, task1.getId());
        assertEquals("Zadanie X", task1.getName());
        assertEquals(Date.valueOf("2025-10-10"), task1.getDueDate());

        Task task2 = tasks.get(1);
        assertEquals(20, task2.getId());
        assertEquals("Zadanie Y", task2.getName());
        assertNull(task2.getDueDate());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetAllTasks_Empty() throws SQLException {
        // Domyślna konfiguracja mockResultSet (next() zwraca false) symuluje brak wyników

        List<Task> tasks = Task.getAllTasks();

        // Weryfikuj, czy zapytanie SELECT zostało wykonane
        verify(mockConnection).prepareStatement("SELECT id, name, due_date FROM task");
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócona lista jest pusta
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetAllTasks_SQLException() throws SQLException {
        // Skonfiguruj mock PreparedStatement, aby rzucił SQLException przy wywołaniu executeQuery
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQLException"));

        List<Task> tasks = Task.getAllTasks();

        // Weryfikuj, czy metoda złapała wyjątek i zwróciła pustą listę
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte (nawet w przypadku błędu)
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
        // resultSet nie zostanie utworzony, więc nie będzie zamknięty w tym scenariuszu błędu executeQuery
    }


    // --- Testy dla metody getTaskById ---

    @Test
    void testGetTaskById_Success() throws SQLException {
        // Skonfiguruj mockResultSet, aby zwrócił przykładowe dane zadania
        when(mockResultSet.next()).thenReturn(true); // Znaleziono jedno zadanie
        when(mockResultSet.getInt("milestone_id")).thenReturn(200);
        when(mockResultSet.getString("name")).thenReturn("Zadanie Z");
        when(mockResultSet.getString("description")).thenReturn("Opis zadania Z");
        when(mockResultSet.getString("status")).thenReturn("In Progress");
        when(mockResultSet.getShort("priority")).thenReturn((short) 1);
        when(mockResultSet.getInt("weight")).thenReturn(25);
        when(mockResultSet.getShort("progress")).thenReturn((short) 50);
        when(mockResultSet.getDate("due_date")).thenReturn(Date.valueOf("2025-11-01"));
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-10-25 10:00:00"));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf("2025-10-28 11:00:00"));

        int taskIdToFind = 30;
        Task task = Task.getTaskById(taskIdToFind);

        // Weryfikuj, czy zapytanie zostało wykonane z poprawnym ID
        verify(mockConnection).prepareStatement("SELECT milestone_id, name, description, status, priority, weight, progress, due_date, created_at, updated_at FROM task WHERE id = ?");
        verify(mockPreparedStatement).setInt(1, taskIdToFind);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócony obiekt Task jest poprawny
        assertNotNull(task);
        assertEquals(taskIdToFind, task.getId()); // ID jest ustawiane ręcznie w modelu
        assertEquals(200, task.getMilestoneId());
        assertEquals("Zadanie Z", task.getName());
        assertEquals("Opis zadania Z", task.getDescription());
        assertEquals("In Progress", task.getStatus());
        assertEquals((short) 1, task.getPriority());
        assertEquals(25, task.getWeight());
        assertEquals((short) 50, task.getProgress());
        assertEquals(Date.valueOf("2025-11-01"), task.getDueDate());
        assertEquals(Timestamp.valueOf("2025-10-25 10:00:00"), task.getCreatedAt());
        assertEquals(Timestamp.valueOf("2025-10-28 11:00:00"), task.getUpdatedAt());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetTaskById_NotFound() throws SQLException {
        // Domyślna konfiguracja mockResultSet (next() zwraca false) symuluje brak wyników

        int taskIdToFind = 999;
        Task task = Task.getTaskById(taskIdToFind);

        // Weryfikuj, czy zapytanie zostało wykonane
        verify(mockConnection).prepareStatement("SELECT milestone_id, name, description, status, priority, weight, progress, due_date, created_at, updated_at FROM task WHERE id = ?");
        verify(mockPreparedStatement).setInt(1, taskIdToFind);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócony obiekt Task to null
        assertNull(task);

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetTaskById_SQLException() throws SQLException {
        // Skonfiguruj mock PreparedStatement, aby rzucił SQLException przy wywołaniu executeQuery
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQLException"));

        int taskIdToFind = 30;
        Task task = Task.getTaskById(taskIdToFind);

        // Weryfikuj, czy metoda złapała wyjątek i zwróciła null
        assertNull(task);

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte (nawet w przypadku błędu)
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
        // resultSet nie zostanie utworzony, więc nie będzie zamknięty
    }

    // --- Testy dla getterów i setterów ---

    @Test
    void testGetterAndSetter() {
        Task task = new Task();

        // Ustawianie wartości za pomocą setterów
        task.setId(40);
        task.setMilestoneId(300);
        task.setName("Testowe Zadanie");
        task.setDescription("Opis testowego zadania");
        task.setStatus("Done");
        task.setPriority((short) 5);
        task.setWeight(50);
        task.setProgress((short) 100);
        Date dueDate = Date.valueOf("2025-12-31");
        task.setDueDate(dueDate);
        Timestamp createdAt = Timestamp.valueOf("2025-12-20 08:00:00");
        Timestamp updatedAt = Timestamp.valueOf("2025-12-30 17:00:00");
        task.setCreatedAt(createdAt);
        task.setUpdatedAt(updatedAt);

        // Sprawdzanie wartości za pomocą getterów
        assertEquals(40, task.getId());
        assertEquals(300, task.getMilestoneId());
        assertEquals("Testowe Zadanie", task.getName());
        assertEquals("Opis testowego zadania", task.getDescription());
        assertEquals("Done", task.getStatus());
        assertEquals((short) 5, task.getPriority());
        assertEquals(50, task.getWeight());
        assertEquals((short) 100, task.getProgress());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(createdAt, task.getCreatedAt());
        assertEquals(updatedAt, task.getUpdatedAt());
    }
}