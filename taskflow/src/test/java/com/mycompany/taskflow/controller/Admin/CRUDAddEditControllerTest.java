package com.mycompany.taskflow.controller.Admin;

import com.mycompany.taskflow.model.Admin.Project;
import com.mycompany.taskflow.model.Admin.Team;
import com.mycompany.taskflow.model.Admin.User;
import com.mycompany.taskflow.model.DatabaseModel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CRUDAddEditControllerTest {

    private CRUDAddEditController controller;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private static MockedStatic<DatabaseModel> databaseModelMock;

    @BeforeAll
    static void initStaticMock() {
        databaseModelMock = mockStatic(DatabaseModel.class);
    }

    @AfterAll
    static void closeStaticMock() {
        if (databaseModelMock != null) {
            databaseModelMock.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        controller = new CRUDAddEditController();
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        databaseModelMock.when(DatabaseModel::connect).thenReturn(mockConnection);
        lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    }

    @Test
    void testPerformSaveLogic_NewUser() throws Exception {
        controller.sectionName = "Użytkownicy";

        Map<String, Object> data = new HashMap<>();
        data.put("firstName", "Anna");
        data.put("lastName", "Nowak");
        data.put("email", "anna@example.com");
        data.put("role", "ADMIN");
        data.put("teamId", 2);
        data.put("password", "haslo123");
        data.put("confirmPassword", "haslo123");

        boolean result = controller.performSaveLogicForTest(data, null);

        assertTrue(result);
        verify(mockStatement, times(1)).executeUpdate();
    }

    @Test
    void testPerformSaveLogic_EditTeam() throws Exception {
        controller.sectionName = "Zespoły";

        Team team = new Team();
        team.setId(1);
        team.setName("Old Name");

        Map<String, Object> data = new HashMap<>();
        data.put("name", "New Name");

        boolean result = controller.performSaveLogicForTest(data, team);

        assertTrue(result);
        verify(mockStatement, times(1)).executeUpdate();
    }

    @Test
    void testPerformSaveLogic_NewProject() throws Exception {
        controller.sectionName = "Projekty";

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Projekt A");
        data.put("description", "Opis A");
        data.put("startDate", LocalDate.of(2024, 1, 1));
        data.put("endDate", LocalDate.of(2024, 12, 31));

        boolean result = controller.performSaveLogicForTest(data, null);

        assertTrue(result);
        verify(mockStatement, times(1)).executeUpdate();
    }

    @Test
    void testPasswordMismatch_ThrowsException() {
        controller.sectionName = "Użytkownicy";

        Map<String, Object> data = new HashMap<>();
        data.put("firstName", "Kuba");
        data.put("lastName", "Test");
        data.put("email", "kuba@test.pl");
        data.put("role", "ADMIN");
        data.put("teamId", 1);
        data.put("password", "pass1");
        data.put("confirmPassword", "pass2");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.performSaveLogicForTest(data, null));
        assertEquals("Hasła nie pasują do siebie.", ex.getMessage());
    }

    @Test
    void testSQLExceptionIsThrown() throws SQLException {
        controller.sectionName = "Zespoły";

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Nowy zespół");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB problem"));

        assertThrows(SQLException.class, () -> controller.performSaveLogicForTest(data, null));
    }

    @Test
    void testPerformSaveLogic_MissingRequiredFields_User() {
        controller.sectionName = "Użytkownicy";
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", "Jan");
        // Brakuje nazwiska, emaila, hasła itd.

        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.performSaveLogicForTest(data, null));
        assertNotNull(ex.getMessage());
    }

    @Test
    void testPerformSaveLogic_ProjectWithEndDateBeforeStartDate() {
        controller.sectionName = "Projekty";

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Projekt B");
        data.put("description", "Opis błędny");
        data.put("startDate", LocalDate.of(2025, 1, 1));
        data.put("endDate", LocalDate.of(2024, 1, 1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> controller.performSaveLogicForTest(data, null));
        assertEquals("Data zakończenia nie może być wcześniejsza niż data rozpoczęcia.", ex.getMessage());
    }

    @Test
    void testPerformSaveLogic_EditUserWithNullTeamId() throws Exception {
        controller.sectionName = "Użytkownicy";
        User user = new User();
        user.setId(5);

        Map<String, Object> data = new HashMap<>();
        data.put("firstName", "Tom");
        data.put("lastName", "Kowalski");
        data.put("email", "tom@kowalski.pl");
        data.put("role", "USER");
        data.put("teamId", null);

        boolean result = controller.performSaveLogicForTest(data, user);
        assertTrue(result);
        verify(mockStatement).executeUpdate();
    }

    @Test
    void testPerformSaveLogic_UnknownSection_ThrowsException() {
        controller.sectionName = "Nieznany";

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Coś");

        Exception ex = assertThrows(IllegalStateException.class, () -> controller.performSaveLogicForTest(data, null));
        assertEquals("Unknown section name: Nieznany", ex.getMessage());



    }
    @Test
    void testPerformSaveLogic_HashesPasswordCorrectly() throws Exception {
        controller.sectionName = "Użytkownicy";

        Map<String, Object> data = new HashMap<>();
        data.put("firstName", "Michał");
        data.put("lastName", "Hasło");
        data.put("email", "michal@hash.pl");
        data.put("role", "USER");
        data.put("teamId", 3);
        data.put("password", "sekret123");
        data.put("confirmPassword", "sekret123");

        final String[] capturedHash = new String[1];

        // przechwyć hash w SQL INSERT
        when(mockConnection.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            PreparedStatement stmt = mock(PreparedStatement.class);

            // udawaj setString wyłapując hash hasła (5. parametr wg insertu)
            doAnswer(setInvocation -> {
                int index = setInvocation.getArgument(0);
                String value = setInvocation.getArgument(1);
                if (value != null && value.startsWith("$2a$")) { // BCrypt hash zawsze zaczyna się od "$2a$"
                    capturedHash[0] = value;
                }

                return null;
            }).when(stmt).setString(anyInt(), anyString());

            when(stmt.executeUpdate()).thenReturn(1);
            return stmt;
        });

        boolean result = controller.performSaveLogicForTest(data, null);
        assertTrue(result);
        assertNotNull(capturedHash[0]);
        assertNotEquals("sekret123", capturedHash[0], "Hasło nie powinno być zapisane w postaci jawnej");
        assertTrue(org.mindrot.jbcrypt.BCrypt.checkpw("sekret123", capturedHash[0]), "Hash nie pasuje do hasła");
    }

}
