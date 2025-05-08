package com.mycompany.taskflow.model.Manager;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.Role;
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

class UserManagerTest {

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
        when(mockResultSet.next()).thenReturn(false);
        lenient().when(mockResultSet.getInt(anyString())).thenReturn(0);
        lenient().when(mockResultSet.getString(anyString())).thenReturn(null);
        lenient().when(mockResultSet.getObject(anyString())).thenReturn(null);
        lenient().when(mockResultSet.getTimestamp(anyString())).thenReturn(null);
        lenient().when(mockResultSet.wasNull()).thenReturn(true);

        lenient().when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        lenient().when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        lenient().when(mockGeneratedKeys.next()).thenReturn(false);
        lenient().when(mockGeneratedKeys.getInt(anyInt())).thenReturn(0);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockedDatabaseModel != null) {
            mockedDatabaseModel.close();
        }
        if (mocks != null) {
            mocks.close();
        }
        reset(mockConnection, mockPreparedStatement, mockResultSet);
    }

    @Test
    void testGetUsersFromDatabaseByTeamId_Success() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false); // Simulate 2 rows
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("first_name")).thenReturn("Jan");
        when(mockResultSet.getString("last_name")).thenReturn("Kowalski");
        when(mockResultSet.getString("email")).thenReturn("j.kowalski@example.com");
        when(mockResultSet.getString("password_hash")).thenReturn("hash1");
        when(mockResultSet.getString("role")).thenReturn("user");
        when(mockResultSet.getObject("team_id")).thenReturn(Integer.valueOf(100));
        when(mockResultSet.getInt("team_id")).thenReturn(100); // Wartość int dla getInt()
        when(mockResultSet.wasNull()).thenReturn(false); // team_id nie jest null
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-05-01 08:00:00"));
        when(mockResultSet.getTimestamp("last_login")).thenReturn(Timestamp.valueOf("2025-05-03 10:00:00"));
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("first_name")).thenReturn("Jan", "Anna");
        when(mockResultSet.getString("last_name")).thenReturn("Kowalski", "Nowak");
        when(mockResultSet.getString("email")).thenReturn("j.kowalski@example.com", "a.nowak@example.com");
        when(mockResultSet.getString("password_hash")).thenReturn("hash1", "hash2");
        when(mockResultSet.getString("role")).thenReturn("user", "user");
        when(mockResultSet.getObject("team_id")).thenReturn(Integer.valueOf(100), Integer.valueOf(100));
        when(mockResultSet.getInt("team_id")).thenReturn(100, 100); // Wartość int dla getInt()
        when(mockResultSet.wasNull()).thenReturn(false, false); // Obaj mają team_id nie-null
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-05-01 08:00:00"), Timestamp.valueOf("2025-05-02 09:00:00"));
        when(mockResultSet.getTimestamp("last_login")).thenReturn(Timestamp.valueOf("2025-05-03 10:00:00"), null);
        int teamId = 100;
        List<User> users = User.getUsersFromDatabaseByTeamId(teamId);
        verify(mockConnection).prepareStatement("SELECT * FROM \"user\" WHERE team_id = ?");
        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).executeQuery();
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals(1, users.get(0).getId());
        assertEquals("Jan", users.get(0).getFirstName());
        assertEquals("Kowalski", users.get(0).getLastName());
        assertEquals("j.kowalski@example.com", users.get(0).getEmail());
        assertEquals("hash1", users.get(0).getPasswordHash());
        assertEquals(Role.USER, users.get(0).getRole());
        assertNotNull(users.get(0).getTeam());
        assertEquals(100, users.get(0).getTeam().getId());
        assertEquals(LocalDateTime.of(2025, 5, 1, 8, 0), users.get(0).getCreatedAt());
        assertEquals(LocalDateTime.of(2025, 5, 3, 10, 0), users.get(0).getLastLogin());
        assertEquals(2, users.get(1).getId());
        assertEquals("Anna", users.get(1).getFirstName());
        assertEquals("Nowak", users.get(1).getLastName());
        assertEquals("a.nowak@example.com", users.get(1).getEmail());
        assertEquals("hash2", users.get(1).getPasswordHash());
        assertEquals(Role.USER, users.get(1).getRole());
        assertNotNull(users.get(1).getTeam());
        assertEquals(100, users.get(1).getTeam().getId());
        assertEquals(LocalDateTime.of(2025, 5, 2, 9, 0), users.get(1).getCreatedAt());
        assertNull(users.get(1).getLastLogin());

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetUsersFromDatabaseByTeamId_Empty() throws SQLException {
        int teamId = 100;
        List<User> users = User.getUsersFromDatabaseByTeamId(teamId);
        verify(mockConnection).prepareStatement("SELECT * FROM \"user\" WHERE team_id = ?");
        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, times(1)).next();
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetUsersFromDatabaseByTeamId_SQLException() throws SQLException {
        SQLException testException = new SQLException("Test SQL Exception");
        when(mockPreparedStatement.executeQuery()).thenThrow(testException);

        int teamId = 100;
        assertThrows(SQLException.class, () -> User.getUsersFromDatabaseByTeamId(teamId));
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, never()).next();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetManager_Success() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false); // Simulate 1 row (should be only one manager)

        when(mockResultSet.getInt("id")).thenReturn(5);
        when(mockResultSet.getString("first_name")).thenReturn("Piotr");
        when(mockResultSet.getString("last_name")).thenReturn("Manager");
        when(mockResultSet.getString("email")).thenReturn("p.manager@example.com");
        when(mockResultSet.getString("password_hash")).thenReturn("managerhash");
        when(mockResultSet.getString("role")).thenReturn("manager");
        when(mockResultSet.getObject("team_id")).thenReturn(Integer.valueOf(200)); // Manager ma team ID 200
        when(mockResultSet.getInt("team_id")).thenReturn(200);
        when(mockResultSet.wasNull()).thenReturn(false); // team_id nie jest null
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-01-01 08:00:00"));
        when(mockResultSet.getTimestamp("last_login")).thenReturn(Timestamp.valueOf("2025-05-04 10:00:00"));
        User manager = User.getManager();
        verify(mockConnection).prepareStatement("SELECT * FROM \"user\" WHERE role = 'manager' LIMIT 1");
        verify(mockPreparedStatement).executeQuery();
        assertNotNull(manager);
        assertEquals(5, manager.getId());
        assertEquals("Piotr", manager.getFirstName());
        assertEquals("Manager", manager.getLastName());
        assertEquals("p.manager@example.com", manager.getEmail());
        assertEquals(Role.MANAGER, manager.getRole());
        assertNotNull(manager.getTeam());
        assertEquals(200, manager.getTeam().getId());
        assertEquals(LocalDateTime.of(2025, 1, 1, 8, 0), manager.getCreatedAt());
        assertEquals(LocalDateTime.of(2025, 5, 4, 10, 0), manager.getLastLogin());

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetManager_NotFound() throws SQLException {
        User manager = User.getManager();
        verify(mockConnection).prepareStatement("SELECT * FROM \"user\" WHERE role = 'manager' LIMIT 1");
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, times(1)).next();

        assertNull(manager);

        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetManager_SQLException() throws SQLException {
        SQLException testException = new SQLException("Test SQL Exception");
        when(mockPreparedStatement.executeQuery()).thenThrow(testException);
        assertThrows(SQLException.class, () -> User.getManager());
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, never()).next();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

}