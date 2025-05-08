package com.mycompany.taskflow.model.Manager;

import com.mycompany.taskflow.model.DatabaseModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationManagerTest {

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
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(false);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockedDatabaseModel != null) {
            mockedDatabaseModel.close();
        }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void testGetLatestNotificationsForUser_Success() throws SQLException {
        // Pierwsze wywołanie - pierwszy rekord
        when(mockResultSet.next()).thenReturn(true, true, false);

        // Mockowanie wartości dla pierwszego rekordu
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getInt("user_id")).thenReturn(10);
        when(mockResultSet.getObject("task_id")).thenReturn(100, null);
        when(mockResultSet.getInt("task_id")).thenReturn(100);
        when(mockResultSet.getObject("subtask_id")).thenReturn(null, 200);
        when(mockResultSet.getInt("subtask_id")).thenReturn(0, 200);
        when(mockResultSet.wasNull()).thenReturn(true, false, true, false);
        when(mockResultSet.getString("type")).thenReturn("update", "reminder");
        when(mockResultSet.getString("content")).thenReturn("Task updated", "Due date reminder");
        when(mockResultSet.getBoolean("is_read")).thenReturn(false, true);
        when(mockResultSet.getTimestamp("created_at"))
                .thenReturn(Timestamp.valueOf("2025-04-01 10:00:00"))
                .thenReturn(Timestamp.valueOf("2025-04-02 11:00:00"));

        int userId = 10;
        int limit = 5;
        List<Notification> notifications = Notification.getLatestNotificationsForUser(userId, limit);

        verify(mockConnection).prepareStatement("SELECT id, user_id, task_id, subtask_id, type, content, is_read, created_at FROM notification WHERE user_id = ? ORDER BY created_at DESC LIMIT ?");
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).setInt(2, limit);
        verify(mockPreparedStatement).executeQuery();

        assertNotNull(notifications);
        assertEquals(2, notifications.size());

        // Weryfikacja pierwszego powiadomienia
        Notification first = notifications.get(0);
        assertEquals(1, first.getId());
        assertEquals(10, first.getUserId());
        assertEquals(100, first.getTaskId());
        assertNull(first.getSubtaskId());
        assertEquals("update", first.getType());
        assertEquals("Task updated", first.getContent());
        assertFalse(first.isRead());
        assertEquals(LocalDateTime.of(2025, 4, 1, 10, 0), first.getCreatedAt());

        // Weryfikacja drugiego powiadomienia
        Notification second = notifications.get(1);
        assertEquals(2, second.getId());
        assertEquals(10, second.getUserId());
        assertNull(second.getTaskId());
        assertEquals(200, second.getSubtaskId());  // Tu była główna różnica
        assertEquals("reminder", second.getType());
        assertEquals("Due date reminder", second.getContent());
        assertTrue(second.isRead());
        assertEquals(LocalDateTime.of(2025, 4, 2, 11, 0), second.getCreatedAt());
    }

    @Test
    void testGetLatestNotificationsForUser_Empty() throws SQLException {
        int userId = 10;
        int limit = 5;
        List<Notification> notifications = Notification.getLatestNotificationsForUser(userId, limit);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).setInt(2, limit);
        verify(mockPreparedStatement).executeQuery();

        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());
    }

    @Test
    void testGetLatestNotificationsForUser_SQLException() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQL Exception"));

        int userId = 10;
        int limit = 5;
        List<Notification> notifications = Notification.getLatestNotificationsForUser(userId, limit);

        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());
    }
}