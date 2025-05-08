package com.mycompany.taskflow.model.User;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.Manager.Subtask;
import com.mycompany.taskflow.model.Manager.Task;
import com.mycompany.taskflow.model.Manager.User;
import com.mycompany.taskflow.model.user.Comment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommentTest {

    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private MockedStatic<DatabaseModel> mockedDatabaseModel;

    @BeforeEach
    public void setUp() throws SQLException {
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
    public void tearDown() {
        mockedDatabaseModel.close();
    }

    @Test
    public void testConstructorAndGetters() {
        Task task = new Task();
        Subtask subtask = new Subtask();
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        Comment comment = new Comment(1, task, subtask, user, "Test content", now);

        assertEquals(1, comment.getId());
        assertEquals(task, comment.getTask());
        assertEquals(subtask, comment.getSubtask());
        assertEquals(user, comment.getUser());
        assertEquals("Test content", comment.getContent());
        assertEquals(now, comment.getCreatedAt());
    }

    @Test
    public void testSetters() {
        Comment comment = new Comment();
        Task task = new Task();
        Subtask subtask = new Subtask();
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        comment.setId(1);
        comment.setTask(task);
        comment.setSubtask(subtask);
        comment.setUser(user);
        comment.setContent("Test content");
        comment.setCreatedAt(now);

        assertEquals(1, comment.getId());
        assertEquals(task, comment.getTask());
        assertEquals(subtask, comment.getSubtask());
        assertEquals(user, comment.getUser());
        assertEquals("Test content", comment.getContent());
        assertEquals(now, comment.getCreatedAt());
    }

    @Test
    public void testSaveToDatabase_Success() throws SQLException {
        Comment comment = new Comment();
        comment.setId(1);
        comment.setContent("Test content");
        comment.setCreatedAt(LocalDateTime.now());

        boolean result = comment.saveToDatabase();

        assertTrue(result);
    }

    @Test
    public void testLoadFromDatabase_NotFound() {
        Comment result = Comment.loadFromDatabase(1);
        assertNull(result);
    }

    @Test
    public void testDeleteFromDatabase_Success() {
        boolean result = Comment.deleteFromDatabase(1);
        assertTrue(result);
    }

    @Test
    public void testToString() {
        Comment comment = new Comment(1, null, null, null, "Test", LocalDateTime.of(2023, 1, 1, 12, 0));
        String expected = "Comment{id=1, task=null, subtask=null, user=null, content='Test', createdAt=2023-01-01T12:00}";
        assertEquals(expected, comment.toString());
    }
}