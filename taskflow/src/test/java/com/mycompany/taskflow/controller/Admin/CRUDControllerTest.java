package com.mycompany.taskflow.controller.Admin;

import com.mycompany.taskflow.model.Admin.Project;
import com.mycompany.taskflow.model.Admin.Team;
import com.mycompany.taskflow.model.Admin.User;
import com.mycompany.taskflow.model.DatabaseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CRUDControllerTest {

    private CRUDController controller;

    @BeforeEach
    void setUp() {
        controller = new CRUDController();
    }

    @Test
    void testGetDataFromDatabase_UserSection() throws SQLException {
        Statement statement = mock(Statement.class);
        controller.sectionName = "Użytkownicy";

        when(statement.executeQuery("SELECT id, first_name, last_name FROM \"user\""))
                .thenReturn(mock(ResultSet.class));

        ResultSet rs = controller.getDataFromDatabase(statement);
        assertNotNull(rs);
    }

    @Test
    void testGetDataFromDatabase_TeamSection() throws SQLException {
        Statement statement = mock(Statement.class);
        controller.sectionName = "Zespoły";

        when(statement.executeQuery("SELECT id, name FROM team"))
                .thenReturn(mock(ResultSet.class));

        ResultSet rs = controller.getDataFromDatabase(statement);
        assertNotNull(rs);
    }

    @Test
    void testGetDataFromDatabase_ProjectSection() throws SQLException {
        Statement statement = mock(Statement.class);
        controller.sectionName = "Projekty";

        when(statement.executeQuery("SELECT id, name, description FROM project"))
                .thenReturn(mock(ResultSet.class));

        ResultSet rs = controller.getDataFromDatabase(statement);
        assertNotNull(rs);
    }

    @Test
    void testCreatePreparedStatementForDelete_User() throws SQLException {
        Connection conn = mock(Connection.class);
        User user = new User();
        user.setId(123);

        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement("DELETE FROM \"user\" WHERE id = ?")).thenReturn(ps);

        PreparedStatement result = controller.createPreparedStatementForDelete(conn, user);
        assertNotNull(result);
        verify(ps).setInt(1, 123);
    }

    @Test
    void testCreatePreparedStatementForDelete_Team() throws SQLException {
        Connection conn = mock(Connection.class);
        Team team = new Team();
        team.setId(456);

        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement("DELETE FROM team WHERE id = ?")).thenReturn(ps);

        PreparedStatement result = controller.createPreparedStatementForDelete(conn, team);
        assertNotNull(result);
        verify(ps).setInt(1, 456);
    }

    @Test
    void testCreatePreparedStatementForDelete_Project() throws SQLException {
        Connection conn = mock(Connection.class);
        Project project = new Project();
        project.setId(789);

        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement("DELETE FROM project WHERE id = ?")).thenReturn(ps);

        PreparedStatement result = controller.createPreparedStatementForDelete(conn, project);
        assertNotNull(result);
        verify(ps).setInt(1, 789);
    }

    @Test
    void testOnDataChanged_WhenAdminPanelControllerExists() {
        AdminPanelController mockPanel = mock(AdminPanelController.class);
        CRUDController spyController = spy(controller);
        spyController.setAdminPanelController(mockPanel);

        doNothing().when(spyController).loadData();

        spyController.onDataChanged();

        verify(spyController).loadData();
        verify(mockPanel).refreshData();
    }

    @Test
    void testOnDataChanged_WhenAdminPanelControllerIsNull() {
        CRUDController spyController = spy(controller);
        spyController.setAdminPanelController(null);

        doNothing().when(spyController).loadData();

        assertDoesNotThrow(spyController::onDataChanged);
        verify(spyController).loadData();
    }

    @Test
    void testSetAdminPanelController() {
        AdminPanelController panel = mock(AdminPanelController.class);
        controller.setAdminPanelController(panel);
        assertEquals(panel, controller.adminPanelController);
    }

    // Niebanalne testy

    @Test
    void testGetDataFromDatabase_InvalidSection() throws SQLException {
        Statement statement = mock(Statement.class);
        controller.sectionName = "NieistniejącaSekcja";

        ResultSet rs = controller.getDataFromDatabase(statement);
        assertNull(rs, "Dla nieobsługiwanej sekcji powinien być zwrócony null");
    }

    @Test
    void testCreatePreparedStatementForDelete_NullObject() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = controller.createPreparedStatementForDelete(conn, null);
        assertNull(ps, "Dla null powinno zwracać null");
    }

    @Test
    void testCreatePreparedStatementForDelete_UnsupportedObject() throws SQLException {
        Connection conn = mock(Connection.class);
        Object unsupported = new Object();

        PreparedStatement ps = controller.createPreparedStatementForDelete(conn, unsupported);
        assertNull(ps, "Dla nieobsługiwanego typu powinno zwracać null");
    }

    @Test
    void testFetchData_ReturnsEmptyListWhenNoData() throws SQLException {
        controller.sectionName = "Użytkownicy";
        Statement stmt = mock(Statement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection conn = mock(Connection.class);

        when(rs.next()).thenReturn(false);
        when(stmt.executeQuery(anyString())).thenReturn(rs);
        when(conn.createStatement()).thenReturn(stmt);

        try (MockedStatic<DatabaseModel> mocked = mockStatic(DatabaseModel.class)) {
            mocked.when(DatabaseModel::connect).thenReturn(conn);
            List<Object> result = controller.fetchData("Użytkownicy");
            assertTrue(result.isEmpty(), "Lista powinna być pusta, gdy brak danych w bazie");
        }
    }

    @Test
    void testFetchData_ThrowsSQLExceptionHandledGracefully() throws SQLException {
        controller.sectionName = "Użytkownicy";
        try (MockedStatic<DatabaseModel> mocked = mockStatic(DatabaseModel.class)) {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.createStatement()).thenThrow(new SQLException("DB error"));
            mocked.when(DatabaseModel::connect).thenReturn(mockConnection);

            assertThrows(SQLException.class, () -> controller.fetchData("Użytkownicy"));
        }
    }



}
