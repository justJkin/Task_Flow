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

class TeamManagerTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    private MockedStatic<DatabaseModel> mockedDatabaseModel;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);

        mockedDatabaseModel = mockStatic(DatabaseModel.class);
        mockedDatabaseModel.when(DatabaseModel::connect).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
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
    void testGetTeamsFromDatabase_Success() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false); // 2 wiersze
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("name")).thenReturn("Zespół Alpha", "Zespół Beta");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-05-01 08:00:00"), Timestamp.valueOf("2025-05-02 09:00:00"));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf("2025-05-01 09:00:00"), Timestamp.valueOf("2025-05-02 10:00:00"));

        List<Team> teams = Team.getTeamsFromDatabase();
        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT * FROM team");
        assertNotNull(teams);
        assertEquals(2, teams.size());
        assertEquals(1, teams.get(0).getId());
        assertEquals("Zespół Alpha", teams.get(0).getName());
        assertEquals(LocalDateTime.of(2025, 5, 1, 8, 0), teams.get(0).getCreatedAt());
        assertEquals(LocalDateTime.of(2025, 5, 1, 9, 0), teams.get(0).getUpdatedAt());
        assertEquals(2, teams.get(1).getId());
        assertEquals("Zespół Beta", teams.get(1).getName());
        assertEquals(LocalDateTime.of(2025, 5, 2, 9, 0), teams.get(1).getCreatedAt());
        assertEquals(LocalDateTime.of(2025, 5, 2, 10, 0), teams.get(1).getUpdatedAt());
        verify(mockResultSet).close();
        verify(mockStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetTeamsFromDatabase_Empty() throws SQLException {
        List<Team> teams = Team.getTeamsFromDatabase();

        verify(mockConnection).createStatement();
        verify(mockStatement).executeQuery("SELECT * FROM team");

        assertNotNull(teams);
        assertTrue(teams.isEmpty());

        verify(mockResultSet).close();
        verify(mockStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetTeamsFromDatabase_SQLException() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("Test SQL Exception"));
        assertThrows(SQLException.class, () -> Team.getTeamsFromDatabase());
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockResultSet, never()).close();
    }
}