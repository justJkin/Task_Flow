package com.mycompany.taskflow.model.User;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.user.Subtask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubtaskTest {

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
        Subtask subtask = new Subtask();

        assertEquals(0, subtask.getId());
        assertEquals(0, subtask.getTaskId());
        assertNull(subtask.getName());
        assertNull(subtask.getDescription());
        assertEquals(0, subtask.getWeight());
        assertFalse(subtask.isDone());
        assertEquals(0, subtask.getPriority());
        assertNull(subtask.getDueDate());
        assertNull(subtask.getCreatedAt());
        assertNull(subtask.getTaskName());
    }

    @Test
    void testParameterizedConstructor() {
        Date dueDate = new Date(System.currentTimeMillis());
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());

        Subtask subtask = new Subtask(1, 2, "Test Subtask", "Description",
                50, true, (short)3, new java.sql.Date(dueDate.getTime()),
                createdAt, "Parent Task");

        assertEquals(1, subtask.getId());
        assertEquals(2, subtask.getTaskId());
        assertEquals("Test Subtask", subtask.getName());
        assertEquals("Description", subtask.getDescription());
        assertEquals(50, subtask.getWeight());
        assertTrue(subtask.isDone());
        assertEquals(3, subtask.getPriority());
        assertNotNull(subtask.getDueDate());
        assertNotNull(subtask.getCreatedAt());
        assertEquals("Parent Task", subtask.getTaskName());
    }

    @Test
    void testCopyConstructor() {
        Date dueDate = new Date(System.currentTimeMillis());
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());

        Subtask original = new Subtask(1, 2, "Test", "Desc", 50,
                false, (short)1, new java.sql.Date(dueDate.getTime()),
                createdAt, "Task");
        Subtask copy = new Subtask(original);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getTaskId(), copy.getTaskId());
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getDescription(), copy.getDescription());
        assertEquals(original.getWeight(), copy.getWeight());
        assertEquals(original.isDone(), copy.isDone());
        assertEquals(original.getPriority(), copy.getPriority());
        assertEquals(original.getDueDate(), copy.getDueDate());
        assertEquals(original.getCreatedAt(), copy.getCreatedAt());
        assertEquals(original.getTaskName(), copy.getTaskName());
    }

    @Test
    void testSettersAndGetters() {
        Date dueDate = new Date(System.currentTimeMillis());
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());

        Subtask subtask = new Subtask();

        subtask.setId(10);
        subtask.setTaskId(20);
        subtask.setName("New Name");
        subtask.setDescription("New Desc");
        subtask.setWeight(75);
        subtask.setDone(true);
        subtask.setPriority((short)2);
        subtask.setDueDate(new java.sql.Date(dueDate.getTime()));
        subtask.setCreatedAt(createdAt);
        subtask.setTaskName("New Task");

        assertEquals(10, subtask.getId());
        assertEquals(20, subtask.getTaskId());
        assertEquals("New Name", subtask.getName());
        assertEquals("New Desc", subtask.getDescription());
        assertEquals(75, subtask.getWeight());
        assertTrue(subtask.isDone());
        assertEquals(2, subtask.getPriority());
        assertNotNull(subtask.getDueDate());
        assertNotNull(subtask.getCreatedAt());
        assertEquals("New Task", subtask.getTaskName());
    }

    @Test
    void testInsertSuccess() throws SQLException {
        Date dueDate = new Date(System.currentTimeMillis());
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());

        Subtask subtask = new Subtask();
        subtask.setTaskId(1);
        subtask.setName("Test");
        subtask.setDescription("Desc");
        subtask.setWeight(50);
        subtask.setDone(false);
        subtask.setPriority((short)1);
        subtask.setDueDate(new java.sql.Date(dueDate.getTime()));

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(100);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(createdAt);

        boolean result = subtask.insert();

        assertTrue(result);
        assertEquals(100, subtask.getId());
        assertEquals(createdAt, subtask.getCreatedAt());
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void testInsertFailure() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test error"));

        Subtask subtask = new Subtask();
        boolean result = subtask.insert();

        assertFalse(result);
        assertEquals(0, subtask.getId());
        assertNull(subtask.getCreatedAt());
    }

    @Test
    void testUpdateSuccess() throws SQLException {
        Date dueDate = new Date(System.currentTimeMillis());

        Subtask subtask = new Subtask();
        subtask.setId(1);
        subtask.setTaskId(2);
        subtask.setName("Updated");
        subtask.setDueDate(new java.sql.Date(dueDate.getTime()));

        boolean result = subtask.update();

        assertTrue(result);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testUpdateFailure() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test error"));

        Subtask subtask = new Subtask();
        subtask.setId(1);
        boolean result = subtask.update();

        assertFalse(result);
    }

    @Test
    void testGetSubtaskByIdSuccess() throws SQLException {
        Date dueDate = new Date(System.currentTimeMillis());
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getInt("task_id")).thenReturn(2);
        when(mockResultSet.getString("name")).thenReturn("Test");
        when(mockResultSet.getString("description")).thenReturn("Desc");
        when(mockResultSet.getInt("weight")).thenReturn(50);
        when(mockResultSet.getBoolean("is_done")).thenReturn(false);
        when(mockResultSet.getShort("priority")).thenReturn((short)1);
        when(mockResultSet.getDate("due_date")).thenReturn(new java.sql.Date(dueDate.getTime()));
        when(mockResultSet.getTimestamp("created_at")).thenReturn(createdAt);
        when(mockResultSet.getString("task_name")).thenReturn("Parent Task");

        Subtask result = Subtask.getSubtaskById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test", result.getName());
        assertEquals("Parent Task", result.getTaskName());
    }

    @Test
    void testGetSubtaskByIdNotFound() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        Subtask result = Subtask.getSubtaskById(1);

        assertNull(result);
    }

    @Test
    void testEqualsAndHashCode() {
        Subtask subtask1 = new Subtask(1, 0, null, null, 0, false, (short)0, null, null, null);
        Subtask subtask2 = new Subtask(1, 0, null, null, 0, false, (short)0, null, null, null);
        Subtask subtask3 = new Subtask(2, 0, null, null, 0, false, (short)0, null, null, null);

        assertEquals(subtask1, subtask2);
        assertNotEquals(subtask1, subtask3);
        assertEquals(subtask1.hashCode(), subtask2.hashCode());
    }

    @Test
    void testToString() {
        Subtask subtask = new Subtask(1, 0, "Test", null, 0, false, (short)0, null, null, "Parent");
        String expected = "Subtask{id=1, name='Test', taskName='Parent', isDone=false, dueDate=null}";

        assertEquals(expected, subtask.toString());
    }
}