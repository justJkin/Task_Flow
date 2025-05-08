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

class MilestoneManagerTest {

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
    void testGetMilestonesFromDatabaseByManagerId_Success() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getInt("project_id")).thenReturn(10);
        when(mockResultSet.getString("project_name")).thenReturn("Projekt Alpha");
        when(mockResultSet.getString("name")).thenReturn("Kamień A");
        when(mockResultSet.getString("description")).thenReturn("Opis A");
        when(mockResultSet.getInt("weight")).thenReturn(50);
        when(mockResultSet.getShort("progress")).thenReturn((short) 25);
        when(mockResultSet.getInt("team_id")).thenReturn(100);
        when(mockResultSet.getString("team_name")).thenReturn("Zespół X");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-01-01 10:00:00"));

        int managerId = 5;
        List<Milestone> milestones = Milestone.getMilestonesFromDatabaseByManagerId(managerId);
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setInt(1, managerId);
        verify(mockPreparedStatement).executeQuery();

        assertNotNull(milestones);
        assertEquals(1, milestones.size());

        Milestone milestone = milestones.get(0);
        assertEquals(1, milestone.getId());
        assertEquals("Kamień A", milestone.getName());
        assertEquals(50, milestone.getWeight());
        assertNotNull(milestone.getProject());
        assertEquals(10, milestone.getProject().getId());
        assertEquals("Projekt Alpha", milestone.getProject().getName());
        assertNotNull(milestone.getTeam());
        assertEquals(100, milestone.getTeam().getId());
        assertEquals("Zespół X", milestone.getTeam().getName());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), milestone.getCreatedAt());
    }

    @Test
    void testGetMilestonesFromDatabaseByManagerId_Empty() throws SQLException {
        int managerId = 5;
        List<Milestone> milestones = Milestone.getMilestonesFromDatabaseByManagerId(managerId);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setInt(1, managerId);
        verify(mockPreparedStatement).executeQuery();

        assertNotNull(milestones);
        assertTrue(milestones.isEmpty());
    }

    @Test
    void testGetMilestonesFromDatabaseByManagerId_SQLException() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQL Exception"));

        int managerId = 5;
        assertThrows(SQLException.class, () -> Milestone.getMilestonesFromDatabaseByManagerId(managerId));


    }


}
