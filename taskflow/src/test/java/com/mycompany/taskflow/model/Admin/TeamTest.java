package com.mycompany.taskflow.model.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TeamTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    // Mockowanie statycznej metody DatabaseModel.connect()
    private MockedStatic<DatabaseModel> mockedDatabaseModel;

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this); // Inicjalizuje mocki

        // Rozpocznij mockowanie statycznej klasy DatabaseModel
        mockedDatabaseModel = mockStatic(DatabaseModel.class);

        // Skonfiguruj mock DatabaseModel.connect()
        mockedDatabaseModel.when(DatabaseModel::connect).thenReturn(mockConnection);

        // Skonfiguruj mock Connection do tworzenia PreparedStatement
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Skonfiguruj mock PreparedStatement do wykonywania zapytań i zwracania ResultSet
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Domyślna konfiguracja mockResultSet: brak wyników
        when(mockResultSet.next()).thenReturn(false); // Domyślnie brak kolejnych wierszy
    }

    // Ważne: Po każdym teście zakończ mockowanie statyczne
    @AfterEach
    public void tearDown() {
        mockedDatabaseModel.close();
    }

    // --- Testy dla metody getAllTeams ---

    @Test
    void testGetAllTeams_Success() throws SQLException {
        // Skonfiguruj mockResultSet, aby zwrócił przykładowe dane zespołów
        when(mockResultSet.next()).thenReturn(true, true, false); // Symuluj 2 wiersze
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("name")).thenReturn("Zespół X", "Zespół Y");

        List<Team> teams = Team.getAllTeams();

        // Weryfikuj, czy zapytanie SELECT zostało wykonane
        verify(mockConnection).prepareStatement("SELECT id, name FROM team");
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócona lista zespołów jest poprawna
        assertNotNull(teams);
        assertEquals(2, teams.size());

        Team team1 = teams.get(0);
        assertEquals(1, team1.getId());
        assertEquals("Zespół X", team1.getName());

        Team team2 = teams.get(1);
        assertEquals(2, team2.getId());
        assertEquals("Zespół Y", team2.getName());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetAllTeams_Empty() throws SQLException {
        // Domyślna konfiguracja mockResultSet (next() zwraca false) symuluje brak wyników

        List<Team> teams = Team.getAllTeams();

        // Weryfikuj, czy zapytanie SELECT zostało wykonane
        verify(mockConnection).prepareStatement("SELECT id, name FROM team");
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócona lista jest pusta
        assertNotNull(teams);
        assertTrue(teams.isEmpty());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetAllTeams_SQLException() throws SQLException {
        // Skonfiguruj mock PreparedStatement, aby rzucił SQLException przy wywołaniu executeQuery
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQLException"));

        List<Team> teams = Team.getAllTeams();

        // Weryfikuj, czy metoda złapała wyjątek i zwróciła pustą listę
        assertNotNull(teams);
        assertTrue(teams.isEmpty());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte (nawet w przypadku błędu)
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
        // resultSet nie zostanie utworzony, więc nie będzie zamknięty w tym scenariuszu błędu executeQuery
    }

}