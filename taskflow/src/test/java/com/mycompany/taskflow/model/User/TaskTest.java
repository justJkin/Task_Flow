package com.mycompany.taskflow.model.User;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.user.Subtask;
import com.mycompany.taskflow.model.user.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskTest {

    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private MockedStatic<DatabaseModel> mockedDatabaseModel;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDatabaseModel = Mockito.mockStatic(DatabaseModel.class);
        mockedDatabaseModel.when(DatabaseModel::connect).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        mockedDatabaseModel.close();
    }

    @Test
    void testDefaultConstructor() {
        Task task = new Task();

        assertEquals(0, task.getId());
        assertEquals(0, task.getMilestoneId());
        assertNull(task.getName());
        assertNull(task.getDescription());
        assertNull(task.getStatus());
        assertEquals(0, task.getPriority());
        assertEquals(0, task.getWeight());
        assertEquals(0, task.getProgress());
        assertNull(task.getDueDate());
        assertNull(task.getCreatedAt());
        assertNull(task.getUpdatedAt());
        assertNotNull(task.getSubtasks());
        assertTrue(task.getSubtasks().isEmpty());
    }


    @Test
    void testSettersAndGetters() {
        Task task = new Task();
        Date dueDate = new Date(System.currentTimeMillis());
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

        task.setId(1);
        task.setMilestoneId(2);
        task.setName("Task Name");
        task.setDescription("Task Description");
        task.setStatus("COMPLETED");
        task.setPriority((short)3);
        task.setWeight(50);
        task.setProgress((short)75);
        task.setDueDate(dueDate);
        task.setCreatedAt(createdAt);
        task.setUpdatedAt(updatedAt);

        List<Subtask> subtasks = List.of(new Subtask());
        task.setSubtasks(subtasks);

        assertEquals(1, task.getId());
        assertEquals(2, task.getMilestoneId());
        assertEquals("Task Name", task.getName());
        assertEquals("Task Description", task.getDescription());
        assertEquals("COMPLETED", task.getStatus());
        assertEquals(3, task.getPriority());
        assertEquals(50, task.getWeight());
        assertEquals(75, task.getProgress());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(createdAt, task.getCreatedAt());
        assertEquals(updatedAt, task.getUpdatedAt());
        assertEquals(subtasks, task.getSubtasks());
    }

    @Test
    void testGetTaskByIdSuccess() throws SQLException {
        Date dueDate = new Date(System.currentTimeMillis());
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("milestone_id")).thenReturn(2);
        when(mockResultSet.getString("name")).thenReturn("Test Task");
        when(mockResultSet.getString("description")).thenReturn("Description");
        when(mockResultSet.getString("status")).thenReturn("IN_PROGRESS");
        when(mockResultSet.getShort("priority")).thenReturn((short)3);
        when(mockResultSet.getInt("weight")).thenReturn(50);
        when(mockResultSet.getShort("progress")).thenReturn((short)75);
        when(mockResultSet.getDate("due_date")).thenReturn(dueDate);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(createdAt);
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(updatedAt);

        Task task = Task.getTaskById(1);

        assertNotNull(task);
        assertEquals(1, task.getId());
        assertEquals(2, task.getMilestoneId());
        assertEquals("Test Task", task.getName());
        assertEquals("Description", task.getDescription());
        assertEquals("IN_PROGRESS", task.getStatus());
        assertEquals(3, task.getPriority());
        assertEquals(50, task.getWeight());
        assertEquals(75, task.getProgress());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(createdAt, task.getCreatedAt());
        assertEquals(updatedAt, task.getUpdatedAt());
    }

    @Test
    void testGetTaskByIdNotFound() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        Task task = Task.getTaskById(1);

        assertNull(task);
    }

    @Test
    void testUpdateStatusInDatabase() throws SQLException {
        Task task = new Task();
        task.setId(1);
        task.setStatus("COMPLETED");

        task.updateStatusInDatabase();

        verify(mockPreparedStatement).setString(1, "COMPLETED");
        verify(mockPreparedStatement).setInt(2, 1);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testUpdateTaskInDatabase() throws SQLException {
        Date dueDate = new Date(System.currentTimeMillis());

        Task task = new Task();
        task.setId(1);
        task.setMilestoneId(2);
        task.setName("Updated Task");
        task.setDescription("Updated Description");
        task.setStatus("IN_PROGRESS");
        task.setPriority((short)2);
        task.setWeight(75);
        task.setProgress((short)50);
        task.setDueDate(dueDate);

        task.updateTaskInDatabase();

        verify(mockPreparedStatement).setInt(1, 2);
        verify(mockPreparedStatement).setString(2, "Updated Task");
        verify(mockPreparedStatement).setString(3, "Updated Description");
        verify(mockPreparedStatement).setString(4, "IN_PROGRESS");
        verify(mockPreparedStatement).setShort(5, (short)2);
        verify(mockPreparedStatement).setInt(6, 75);
        verify(mockPreparedStatement).setShort(7, (short)50);
        verify(mockPreparedStatement).setDate(8, dueDate);
        verify(mockPreparedStatement).setInt(9, 1);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testDatabaseErrorHandling() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test error"));
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test error"));

        assertNull(Task.getTaskById(1));

        Task task = new Task();
        task.setId(1);
        task.setStatus("COMPLETED");
        task.updateStatusInDatabase();

        task.updateTaskInDatabase();
    }
}