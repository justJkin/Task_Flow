package com.mycompany.taskflow.controller.Admin;

import com.mycompany.taskflow.model.Admin.Project;
import com.mycompany.taskflow.model.DatabaseModel;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminPanelControllerTest {

    private AdminPanelController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminPanelController();
    }

    @Test
    void testLoadProjectData_WithValidData() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("name")).thenReturn("Project A");
        when(mockResultSet.getString("description")).thenReturn("Opis A");
        when(mockResultSet.getDate("start_date")).thenReturn(null);
        when(mockResultSet.getDate("end_date")).thenReturn(null);

        try (MockedStatic<DatabaseModel> dbMock = mockStatic(DatabaseModel.class)) {
            dbMock.when(DatabaseModel::connect).thenReturn(mockConnection);

            assertDoesNotThrow(() -> controller.loadProjectData());
        }
    }

    @Test
    void testLoadProjectData_WithEmptyResultSet() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        try (MockedStatic<DatabaseModel> dbMock = mockStatic(DatabaseModel.class)) {
            dbMock.when(DatabaseModel::connect).thenReturn(mockConnection);

            assertDoesNotThrow(() -> controller.loadProjectData());
        }
    }

    @Test
    void testLoadProjectData_ThrowsSQLException() throws SQLException {
        // Mockujemy Alert, aby nie uruchamiał JavaFX w czasie testu
        try (MockedConstruction<Alert> ignored = mockConstruction(Alert.class)) {
            // Mockujemy DatabaseModel, aby rzucił SQLException
            try (MockedStatic<DatabaseModel> dbMock = mockStatic(DatabaseModel.class)) {
                dbMock.when(DatabaseModel::connect).thenThrow(new SQLException("Błąd bazy danych"));

                // Sprawdzamy, że metoda NIE rzuca wyjątku (bo sama go obsługuje)
                assertDoesNotThrow(() -> controller.loadProjectData());
            }
        }
    }



    @Test
    void testRefreshData_CallsAllLoadMethods() throws SQLException {
        AdminPanelController spyController = spy(new AdminPanelController());

        doNothing().when(spyController).loadUserData();
        doNothing().when(spyController).loadTeamData();
        doNothing().when(spyController).loadProjectData();

        spyController.refreshData();

        verify(spyController).loadUserData();
        verify(spyController).loadTeamData();
        verify(spyController).loadProjectData();
    }

    @Test
    void testLoadProjectData_AddsCorrectProject() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(5);
        when(mockResultSet.getString("name")).thenReturn("Projekt Testowy");
        when(mockResultSet.getString("description")).thenReturn("Opis Testowy");
        when(mockResultSet.getDate("start_date")).thenReturn(null);
        when(mockResultSet.getDate("end_date")).thenReturn(null);

        try (MockedStatic<DatabaseModel> dbMock = mockStatic(DatabaseModel.class)) {
            dbMock.when(DatabaseModel::connect).thenReturn(mockConnection);

            controller.loadProjectData();

            try {
                Field projectsField = AdminPanelController.class.getDeclaredField("projects");
                projectsField.setAccessible(true); // pozwala na dostęp do prywatnych pól
                ObservableList<Project> projects = (ObservableList<Project>) projectsField.get(controller);

                assertEquals(1, projects.size());
                assertEquals("Projekt Testowy", projects.get(0).getName());

            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail("Nie udało się uzyskać dostępu do pola 'projects': " + e.getMessage());
            }
        }
    }
}
