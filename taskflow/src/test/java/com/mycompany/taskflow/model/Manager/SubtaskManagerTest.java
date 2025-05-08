package com.mycompany.taskflow.model.Manager;

import com.mycompany.taskflow.model.DatabaseModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubtaskManagerTest {

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
        when(mockResultSet.getDate(anyString())).thenReturn(null);
        when(mockResultSet.wasNull()).thenReturn(true);
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
    void testGetRecentSubtasksForTeam_Success() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false); // 2 wiersze
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("name")).thenReturn("Recent Subtask 1", "Recent Subtask 2");
        when(mockResultSet.getDate("due_date")).thenReturn(Date.valueOf("2025-02-20"), null);
        when(mockResultSet.wasNull()).thenReturn(false, true);


        when(mockResultSet.getInt("task_id")).thenReturn(10, 11);
        when(mockResultSet.getString("task_name")).thenReturn("Task A", "Task B");

        int teamId = 200;
        List<Subtask> recentSubtasks = Subtask.getRecentSubtasksForTeam(teamId);
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).executeQuery();

        assertNotNull(recentSubtasks);
        assertEquals(2, recentSubtasks.size());

        assertEquals(1, recentSubtasks.get(0).getId());
        assertEquals("Recent Subtask 1", recentSubtasks.get(0).getName());
        assertEquals(LocalDate.of(2025, 2, 20), recentSubtasks.get(0).getDueDate());
        assertNotNull(recentSubtasks.get(0).getTask());
        assertEquals(10, recentSubtasks.get(0).getTask().getId());
        assertEquals("Task A", recentSubtasks.get(0).getTask().getName());
        assertEquals(2, recentSubtasks.get(1).getId());
        assertEquals("Recent Subtask 2", recentSubtasks.get(1).getName());
        assertNull(recentSubtasks.get(1).getDueDate());
        assertNotNull(recentSubtasks.get(1).getTask());
        assertEquals(11, recentSubtasks.get(1).getTask().getId());
        assertEquals("Task B", recentSubtasks.get(1).getTask().getName());
    }

    @Test
    void testGetRecentSubtasksForTeam_Empty() throws SQLException {
        int teamId = 200;
        List<Subtask> recentSubtasks = Subtask.getRecentSubtasksForTeam(teamId);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).executeQuery();

        assertNotNull(recentSubtasks);
        assertTrue(recentSubtasks.isEmpty());
    }

    @Test
    void testGetRecentSubtasksForTeam_SQLException() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQL Exception"));

        int teamId = 200;
        List<Subtask> recentSubtasks = Subtask.getRecentSubtasksForTeam(teamId);
        assertNotNull(recentSubtasks);
        assertTrue(recentSubtasks.isEmpty());
    }

    @Test
    void testGetUpcomingSubtasksForTeam_Success() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false); // 1 wiersz
        when(mockResultSet.getInt("id")).thenReturn(3);
        when(mockResultSet.getString("name")).thenReturn("Upcoming Subtask");
        when(mockResultSet.getString("description")).thenReturn("Upcoming Description");
        when(mockResultSet.getInt("weight")).thenReturn(5);
        when(mockResultSet.getBoolean("is_done")).thenReturn(false);
        when(mockResultSet.getShort("priority")).thenReturn((short) 1);
        when(mockResultSet.getDate("due_date")).thenReturn(Date.valueOf(LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.TUESDAY)));
        when(mockResultSet.wasNull()).thenReturn(false);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-03-01 09:00:00"));
        when(mockResultSet.getInt("task_id")).thenReturn(12);
        when(mockResultSet.getString("task_name")).thenReturn("Task C");
        when(mockResultSet.getInt("team_id")).thenReturn(200);


        int teamId = 200;
        List<Subtask> upcomingSubtasks = Subtask.getUpcomingSubtasksForTeam(teamId);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setInt(eq(1), eq(teamId));
        verify(mockPreparedStatement).setDate(eq(2), any(Date.class));
        verify(mockPreparedStatement).setDate(eq(3), any(Date.class));
        verify(mockPreparedStatement).executeQuery();
        assertNotNull(upcomingSubtasks);
        assertEquals(1, upcomingSubtasks.size());
        Subtask subtask = upcomingSubtasks.get(0);
        assertEquals(3, subtask.getId());
        assertEquals("Upcoming Subtask", subtask.getName());
        assertNotNull(subtask.getDueDate());
        assertTrue(subtask.getDueDate().isAfter(LocalDate.now()));
    }

    @Test
    void testGetUpcomingSubtasksForTeam_Empty() throws SQLException {
        int teamId = 200;
        List<Subtask> upcomingSubtasks = Subtask.getUpcomingSubtasksForTeam(teamId);
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setInt(eq(1), eq(teamId));
        verify(mockPreparedStatement).setDate(eq(2), any(Date.class));
        verify(mockPreparedStatement).setDate(eq(3), any(Date.class));
        verify(mockPreparedStatement).executeQuery();
        assertNotNull(upcomingSubtasks);
        assertTrue(upcomingSubtasks.isEmpty());
    }

    @Test
    void testGetUpcomingSubtasksForTeam_SQLException() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQL Exception"));
        int teamId = 200;
        List<Subtask> upcomingSubtasks = Subtask.getUpcomingSubtasksForTeam(teamId);
        assertNotNull(upcomingSubtasks);
        assertTrue(upcomingSubtasks.isEmpty());
    }
}
