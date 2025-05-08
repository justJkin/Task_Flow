package com.mycompany.taskflow.model.Manager;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.TaskStatus;
import com.mycompany.taskflow.model.Manager.Milestone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
class TaskManagerTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    private MockedStatic<DatabaseModel> mockedDatabaseModel;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        mockedDatabaseModel = mockStatic(DatabaseModel.class);
        mockedDatabaseModel.when(DatabaseModel::connect).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        when(mockResultSet.next()).thenReturn(false);
        when(mockResultSet.getInt(anyString())).thenReturn(0);
        when(mockResultSet.getString(anyString())).thenReturn("");
        when(mockResultSet.getDate(anyString())).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getTimestamp(anyString())).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.wasNull()).thenReturn(false);

        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(false);
        when(mockGeneratedKeys.getInt(anyInt())).thenReturn(0);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedDatabaseModel.close();
        mocks.close();
        reset(mockConnection, mockPreparedStatement, mockResultSet);
    }

    @Test
    void testSaveTaskToDatabase_Success() throws SQLException {
        Milestone mockMilestone = new Milestone();
        mockMilestone.setId(10);

        Task taskToSave = new Task();
        taskToSave.setMilestone(mockMilestone);
        taskToSave.setName("Nowe Zadanie");
        taskToSave.setDescription("Opis zadania");
        taskToSave.setStatus(TaskStatus.TO_DO);
        taskToSave.setPriority((short) 3);
        taskToSave.setWeight(15);
        taskToSave.setProgress((short) 0);
        taskToSave.setDueDate(LocalDate.now().plusDays(7));
        taskToSave.setCreatedAt(LocalDateTime.now());
        taskToSave.setUpdatedAt(LocalDateTime.now());

        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(99);

        Task.saveTaskToDatabase(taskToSave);

        verify(mockConnection).prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS));
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).getGeneratedKeys();
        verify(mockGeneratedKeys).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
        assertEquals(99, taskToSave.getId());
    }

    @Test
    void testSaveTaskToDatabase_NoRowsAffected() throws SQLException {
        Milestone mockMilestone = new Milestone();
        mockMilestone.setId(10);

        Task taskToSave = new Task();
        taskToSave.setMilestone(mockMilestone);
        taskToSave.setName("Nowe Zadanie");

        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        SQLException exception = assertThrows(SQLException.class, () -> Task.saveTaskToDatabase(taskToSave));
        assertTrue(exception.getMessage().contains("no rows affected"));

        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetUsedWeightForMilestone_Success() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("used_weight")).thenReturn(75);

        int usedWeight = Task.getUsedWeightForMilestone(10);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setInt(1, 10);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
        assertEquals(75, usedWeight);
    }

    @Test
    void testGetUsedWeightForMilestone_NoTasks() throws SQLException {
        int usedWeight = Task.getUsedWeightForMilestone(10);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setInt(1, 10);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
        assertEquals(0, usedWeight);
    }

    @Test
    void testGetUsedWeightForMilestone_SQLException() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQL Exception"));

        assertThrows(SQLException.class, () -> Task.getUsedWeightForMilestone(10));

        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetRecentTasksForTeam_Success() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("name")).thenReturn("Zadanie 1");
        when(mockResultSet.getDate("due_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getString("status")).thenReturn(TaskStatus.DONE.getDbValue());
        when(mockResultSet.getInt("milestone_id")).thenReturn(10);
        when(mockResultSet.getString("milestone_name")).thenReturn("Milestone 1");

        List<Task> tasks = Task.getRecentTasksForTeam(100);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setInt(1, 100);
        verify(mockPreparedStatement).setString(2, TaskStatus.DONE.getDbValue());
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();

        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals("Zadanie 1", tasks.get(0).getName());
    }

    @Test
    void testGetRecentTasksForTeam_Empty() throws SQLException {
        List<Task> tasks = Task.getRecentTasksForTeam(100);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();

        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void testGetRecentTasksForTeam_SQLException() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQL Exception"));

        List<Task> tasks = Task.getRecentTasksForTeam(100);

        verify(mockPreparedStatement).close();
        verify(mockConnection).close();

        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void testGetUpcomingTasksForTeam_Success() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("name")).thenReturn("Nadchodzące Zadanie");
        when(mockResultSet.getDate("due_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(1)));
        when(mockResultSet.getString("status")).thenReturn(TaskStatus.TO_DO.getDbValue());
        when(mockResultSet.getInt("milestone_id")).thenReturn(10);
        when(mockResultSet.getString("milestone_name")).thenReturn("Milestone 1");

        List<Task> tasks = Task.getUpcomingTasksForTeam(100);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();

        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals("Nadchodzące Zadanie", tasks.get(0).getName());
    }

    @Test
    void testGetUpcomingTasksForTeam_Empty() throws SQLException {
        List<Task> tasks = Task.getUpcomingTasksForTeam(100);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();

        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void testGetUpcomingTasksForTeam_SQLException() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQL Exception"));

        List<Task> tasks = Task.getUpcomingTasksForTeam(100);

        verify(mockPreparedStatement).close();
        verify(mockConnection).close();

        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }
}