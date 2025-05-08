package com.mycompany.taskflow.model.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SubtaskTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    private MockedStatic<DatabaseModel> mockedDatabaseModel;
    private AutoCloseable mocks;

    @BeforeEach
    public void setUp() throws SQLException {
        // 1. Wyczyść istniejące mocki statyczne
        Mockito.framework().clearInlineMocks();

        // 2. Inicjalizacja mocków
        mocks = MockitoAnnotations.openMocks(this);

        // 3. Utwórz nowe mockowanie statyczne
        mockedDatabaseModel = mockStatic(DatabaseModel.class);

        // 4. Konfiguracja mocków
        mockedDatabaseModel.when(DatabaseModel::connect).thenReturn(mockConnection);

        // Dla getAllSubtasks
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        // Dla innych metod
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // 1. Zamknij mocki statyczne (jeśli istnieją)
        if (mockedDatabaseModel != null) {
            mockedDatabaseModel.close();
        }

        // 2. Zamknij pozostałe mocki
        if (mocks != null) {
            mocks.close();
        }

        // 3. Dodatkowe czyszczenie
        Mockito.framework().clearInlineMocks();
    }
    @Test
    void testGetAllSubtasks_Success() throws SQLException {
        // Konfiguracja mockResultSet
        when(mockResultSet.next()).thenReturn(true, true, false); // Dwa wiersze
        when(mockResultSet.getInt("id")).thenReturn(10, 20);
        when(mockResultSet.getString("name")).thenReturn("Subtask A", "Subtask B");
        when(mockResultSet.getDate("due_date")).thenReturn(Date.valueOf("2025-10-01"), null);

        List<Subtask> subtasks = Subtask.getAllSubtasks();

        // Zmienione na weryfikację prepareStatement zamiast createStatement
        verify(mockConnection).prepareStatement("SELECT id, name, due_date FROM subtask");
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, times(3)).next(); // 2x true, 1x false

        assertNotNull(subtasks);
        assertEquals(2, subtasks.size());

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetAllSubtasks_Empty() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        List<Subtask> subtasks = Subtask.getAllSubtasks();

        assertNotNull(subtasks);
        assertTrue(subtasks.isEmpty());
    }

    @Test
    void testGetAllSubtasks_SQLException() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("Test SQLException"));

        List<Subtask> subtasks = Subtask.getAllSubtasks();

        assertNotNull(subtasks);
        assertTrue(subtasks.isEmpty());
    }

    @Test
    void testGetSubtaskById_Success() throws SQLException {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("task_id")).thenReturn(100);
        when(mockResultSet.getString("name")).thenReturn("Subtask X");
        when(mockResultSet.getString("description")).thenReturn("Description X");
        when(mockResultSet.getInt("weight")).thenReturn(5);
        when(mockResultSet.getBoolean("is_done")).thenReturn(true);
        when(mockResultSet.getShort("priority")).thenReturn((short) 2);
        when(mockResultSet.getDate("due_date")).thenReturn(Date.valueOf("2025-11-20"));
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-11-15 14:00:00"));

        int subtaskId = 5;
        Subtask subtask = Subtask.getSubtaskById(subtaskId);

        assertNotNull(subtask);
        assertEquals(subtaskId, subtask.getId());
    }

    @Test
    void testGetSubtaskById_NotFound() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        int subtaskId = 99;
        Subtask subtask = Subtask.getSubtaskById(subtaskId);

        assertNull(subtask);
    }

    @Test
    void testInsert_Success() throws SQLException {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(100);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-12-01 09:00:00"));

        Subtask subtask = new Subtask();
        subtask.setTaskId(1);
        subtask.setName("New Subtask");

        boolean success = subtask.insert();

        assertTrue(success);
        assertEquals(100, subtask.getId());
    }

    @Test
    void testUpdate_Success() throws SQLException {
        Subtask subtask = new Subtask();
        subtask.setId(5);

        boolean success = subtask.update();

        assertTrue(success);
    }

    @Test
    void testDelete_Success() throws SQLException {
        Subtask subtask = new Subtask();
        subtask.setId(10);

        boolean success = subtask.delete();

        assertTrue(success);
    }

    @Test
    void testGetterAndSetter() {
        Subtask subtask = new Subtask();
        subtask.setId(1);
        assertEquals(1, subtask.getId());
        // ... test innych getterów/setterów
    }


    @Test
    public void testGetTaskIdAndSetTaskId() {
        Subtask subtask = new Subtask();
        subtask.setTaskId(456);
        assertEquals(456, subtask.getTaskId());
    }

    @Test
    public void testGetNameAndSetName() {
        Subtask subtask = new Subtask();
        subtask.setName("Test Task");
        assertEquals("Test Task", subtask.getName());
    }

    @Test
    public void testGetDescriptionAndSetDescription() {
        Subtask subtask = new Subtask();
        subtask.setDescription("Test Description");
        assertEquals("Test Description", subtask.getDescription());
    }

    @Test
    public void testGetWeightAndSetWeight() {
        Subtask subtask = new Subtask();
        subtask.setWeight(10);
        assertEquals(10, subtask.getWeight());
    }

    @Test
    public void testIsDoneAndSetDone() {
        Subtask subtask = new Subtask();
        subtask.setDone(true);
        assertTrue(subtask.isDone());

        subtask.setDone(false);
        assertFalse(subtask.isDone());
    }

    @Test
    public void testGetPriorityAndSetPriority() {
        Subtask subtask = new Subtask();
        subtask.setPriority((short)5);
        assertEquals(5, subtask.getPriority());
    }

    @Test
    public void testGetCreatedAtAndSetCreatedAt() {
        Subtask subtask = new Subtask();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        subtask.setCreatedAt(now);
        assertEquals(now, subtask.getCreatedAt());
    }
    @Test
    public void testSetAndGetDueDate() {
        // Przygotowanie testu
        Subtask subtask = new Subtask();

        // Konwersja LocalDate na java.sql.Date
        LocalDate localDate = LocalDate.now();
        Date testDate = Date.valueOf(localDate);

        // Wykonanie akcji
        subtask.setDueDate(testDate);
        Date retrievedDate = subtask.getDueDate();

        // Weryfikacja
        assertNotNull(retrievedDate, "Data nie powinna być null");
        assertEquals(testDate, retrievedDate, "Data powinna być taka sama jak ustawiona");
    }
}