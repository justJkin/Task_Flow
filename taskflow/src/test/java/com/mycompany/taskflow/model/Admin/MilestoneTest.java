package com.mycompany.taskflow.model.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MilestoneTest {

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
        when(mockResultSet.next()).thenReturn(false); // Domyślnie nie ma kolejnych wierszy
    }

    // Ważne: Po każdym teście zakończ mockowanie statyczne
    @AfterEach
    public void tearDown() {
        mockedDatabaseModel.close();
    }

    // --- Testy dla metody getMilestoneById ---

    @Test
    void testGetMilestoneById_Success() throws SQLException {
        // Skonfiguruj mockResultSet, aby zwrócił przykładowe dane kamienia milowego
        when(mockResultSet.next()).thenReturn(true); // Znaleziono jeden kamień milowy
        when(mockResultSet.getInt("project_id")).thenReturn(100);
        when(mockResultSet.getString("name")).thenReturn("Kamień Milowy Alpha");
        when(mockResultSet.getString("description")).thenReturn("Opis kamienia milowego");
        when(mockResultSet.getInt("weight")).thenReturn(50);
        when(mockResultSet.getShort("progress")).thenReturn((short) 25);
        when(mockResultSet.getInt("team_id")).thenReturn(201);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-01-15 09:30:00"));

        int milestoneIdToFind = 1;
        Milestone milestone = Milestone.getMilestoneById(milestoneIdToFind);

        // Weryfikuj, czy zapytanie zostało wykonane z poprawnym ID
        verify(mockConnection).prepareStatement("SELECT project_id, name, description, weight, progress, team_id, created_at FROM milestone WHERE id = ?");
        verify(mockPreparedStatement).setInt(1, milestoneIdToFind);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócony obiekt Milestone jest poprawny
        assertNotNull(milestone);
        assertEquals(milestoneIdToFind, milestone.getId()); // ID jest ustawiane ręcznie w modelu
        assertEquals(100, milestone.getProjectId());
        assertEquals("Kamień Milowy Alpha", milestone.getName());
        assertEquals("Opis kamienia milowego", milestone.getDescription());
        assertEquals(50, milestone.getWeight());
        assertEquals((short) 25, milestone.getProgress());
        assertEquals(201, milestone.getTeamId());
        assertEquals(Timestamp.valueOf("2025-01-15 09:30:00"), milestone.getCreatedAt());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetMilestoneById_NotFound() throws SQLException {
        // Domyślna konfiguracja mockResultSet (next() zwraca false) symuluje brak wyników

        int milestoneIdToFind = 99;
        Milestone milestone = Milestone.getMilestoneById(milestoneIdToFind);

        // Weryfikuj, czy zapytanie zostało wykonane
        verify(mockConnection).prepareStatement("SELECT project_id, name, description, weight, progress, team_id, created_at FROM milestone WHERE id = ?");
        verify(mockPreparedStatement).setInt(1, milestoneIdToFind);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócony obiekt Milestone to null
        assertNull(milestone);

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetMilestoneById_SQLException() throws SQLException {
        // Skonfiguruj mock PreparedStatement, aby rzucił SQLException przy wywołaniu executeQuery
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQLException"));

        int milestoneIdToFind = 1;
        Milestone milestone = Milestone.getMilestoneById(milestoneIdToFind);

        // Weryfikuj, czy rzucono wyjątek (i został obsłużony w modelu - model zwraca null)
        assertNull(milestone); // Model łapie wyjątek i zwraca null

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte (nawet w przypadku błędu)
        verify(mockPreparedStatement).close(); // Prepared Statement powinien być zamknięty
        verify(mockConnection).close(); // Połączenie powinno być zamknięte
        // resultSet nie zostanie utworzony, więc nie będzie zamknięty
    }

    // --- Testy dla getterów i setterów ---

    @Test
    void testGetterAndSetter() {
        Milestone milestone = new Milestone();

        // Ustawianie wartości za pomocą setterów
        milestone.setId(2);
        milestone.setProjectId(101);
        milestone.setName("Kamień Milowy Beta");
        milestone.setDescription("Szczegóły drugiego kamienia milowego");
        milestone.setWeight(30);
        milestone.setProgress((short) 75);
        milestone.setTeamId(202);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        milestone.setCreatedAt(now);

        // Sprawdzanie wartości za pomocą getterów
        assertEquals(2, milestone.getId());
        assertEquals(101, milestone.getProjectId());
        assertEquals("Kamień Milowy Beta", milestone.getName());
        assertEquals("Szczegóły drugiego kamienia milowego", milestone.getDescription());
        assertEquals(30, milestone.getWeight());
        assertEquals((short) 75, milestone.getProgress());
        assertEquals(202, milestone.getTeamId());
        assertEquals(now, milestone.getCreatedAt());
    }
}