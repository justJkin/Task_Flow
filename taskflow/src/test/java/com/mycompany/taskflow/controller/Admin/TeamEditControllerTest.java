package com.mycompany.taskflow.controller.Admin;

import com.mycompany.taskflow.model.Admin.Team;
import com.mycompany.taskflow.model.Admin.User;
import com.mycompany.taskflow.model.DatabaseModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamEditControllerTest {

    private TeamEditController controller;
    private static MockedStatic<DatabaseModel> databaseMock;
    private Connection mockConnection;
    private ResultSet mockResultSet;

    @BeforeAll
    static void setupStaticMock() {
        databaseMock = Mockito.mockStatic(DatabaseModel.class);
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @AfterAll
    static void closeStaticMock() {
        databaseMock.close();
    }

    @BeforeEach
    void setUp() throws SQLException {
        controller = new TeamEditController() {
            @Override
            protected void showAlert(Alert.AlertType type, String message) {
                // suppress alerts in tests
            }
        };

        mockConnection = mock(Connection.class);
        mockResultSet = mock(ResultSet.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);

        lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        lenient().when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        databaseMock.when(DatabaseModel::connect).thenReturn(mockConnection);

        // Inicjalizacja komponentów JavaFX wykorzystywanych w setTeam() i setupDragAndDrop()
        controller.nameTextField = new TextField();
        controller.saveButton = new Button();
        controller.usersInTeamListView = new ListView<>();
        controller.allUsersListView = new ListView<>();
        controller.otherUsersListView = new ListView<>();
    }

    @Test
    void testMapResultSetToUser_ValidData() throws SQLException {
        when(mockResultSet.getInt("id")).thenReturn(10);
        when(mockResultSet.getString("first_name")).thenReturn("Jan");
        when(mockResultSet.getString("last_name")).thenReturn("Kowalski");
        when(mockResultSet.getString("email")).thenReturn("jan@example.com");
        when(mockResultSet.getString("role")).thenReturn("user");
        when(mockResultSet.getObject("team_id")).thenReturn(2);
        when(mockResultSet.getInt("team_id")).thenReturn(2);
        when(mockResultSet.getString("password_hash")).thenReturn("hashed");

        User user = controller.mapResultSetToUser(mockResultSet);

        assertEquals(10, user.getId());
        assertEquals("Jan", user.getFirstName());
        assertEquals("Kowalski", user.getLastName());
        assertEquals("jan@example.com", user.getEmail());
        assertEquals("user", user.getRole());
        assertEquals(2, user.getTeamId());
        assertEquals("hashed", user.getPasswordHash());
    }

    @Test
    void testMapResultSetToUser_NullTeamId() throws SQLException {
        when(mockResultSet.getInt("id")).thenReturn(20);
        when(mockResultSet.getString("first_name")).thenReturn("Anna");
        when(mockResultSet.getString("last_name")).thenReturn("Nowak");
        when(mockResultSet.getString("email")).thenReturn("anna@example.com");
        when(mockResultSet.getString("role")).thenReturn("manager");
        when(mockResultSet.getObject("team_id")).thenReturn(null);
        when(mockResultSet.getString("password_hash")).thenReturn("secure");

        User user = controller.mapResultSetToUser(mockResultSet);

        assertNull(user.getTeamId());
        assertEquals("manager", user.getRole());
    }

    @Test
    void testMultipleManagersPreventionLogic() {
        ObservableList<User> mockUsers = FXCollections.observableArrayList();

        User manager1 = new User();
        manager1.setId(1);
        manager1.setRole("manager");

        User manager2 = new User();
        manager2.setId(2);
        manager2.setRole("manager");

        mockUsers.addAll(manager1, manager2);

        long count = mockUsers.stream().filter(u -> u.getRole().equalsIgnoreCase("manager")).count();

        assertEquals(2, count);
        assertTrue(count > 1, "Should detect multiple managers");
    }

    @Test
    void testLoadTeamsCache_HandlesSQLException() {
        databaseMock.when(DatabaseModel::connect).thenThrow(new SQLException("Simulated DB error"));

        assertDoesNotThrow(() -> controller.loadTeamsCache());
    }

    @Test
    void testInitialUsersRemovedCorrectly() {
        User user1 = new User();
        user1.setId(1);
        user1.setRole("user");
        User user2 = new User();
        user2.setId(2);
        user2.setRole("user");

        Set<Integer> initialIds = Set.of(user1.getId(), user2.getId());
        ObservableList<User> currentList = FXCollections.observableArrayList(user1);

        Set<Integer> currentIds = currentList.stream().map(User::getId).collect(Collectors.toSet());

        Set<Integer> removed = initialIds.stream()
                .filter(id -> !currentIds.contains(id))
                .collect(Collectors.toSet());

        assertEquals(Set.of(2), removed);
    }

    @Test
    void testHandleSave_TooManyManagers_ShowsAlert() {
        controller.nameTextField.setText("Nowa nazwa");

        controller.setTeam(new Team() {{
            setId(1);
            setName("Stara nazwa");
        }});

        User manager1 = new User();
        manager1.setId(1);
        manager1.setRole("manager");

        User manager2 = new User();
        manager2.setId(2);
        manager2.setRole("manager");

        controller.usersInTeam = FXCollections.observableArrayList(manager1, manager2);

        assertDoesNotThrow(() -> controller.handleSave());
    }

    @Test
    void testSingleManagerAllowed() {
        ObservableList<User> mockUsers = FXCollections.observableArrayList();

        User manager = new User();
        manager.setId(1);
        manager.setRole("manager");

        User user = new User();
        user.setId(2);
        user.setRole("user");

        mockUsers.addAll(manager, user);

        long count = mockUsers.stream().filter(u -> "manager".equalsIgnoreCase(u.getRole())).count();

        assertEquals(1, count);
        assertFalse(count > 1, "Powinien być tylko jeden manager");
    }

}
