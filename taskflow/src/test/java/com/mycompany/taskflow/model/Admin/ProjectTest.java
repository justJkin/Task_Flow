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

public class ProjectTest {

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

    // --- Testy dla metody getAllProjects ---

    @Test
    void testGetAllProjects_Success() throws SQLException {
        // Skonfiguruj mockResultSet, aby zwrócił przykładowe dane projektów
        when(mockResultSet.next()).thenReturn(true, true, false); // Symuluj 2 wiersze
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("name")).thenReturn("Projekt A", "Projekt B");
        when(mockResultSet.getDate("end_date")).thenReturn(Date.valueOf("2025-12-31"), Date.valueOf("2026-01-15"));

        List<Project> projects = Project.getAllProjects();

        // Weryfikuj, czy zapytanie SELECT zostało wykonane
        verify(mockConnection).prepareStatement("SELECT id, name, end_date FROM project");
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócona lista projektów jest poprawna
        assertNotNull(projects);
        assertEquals(2, projects.size());

        Project project1 = projects.get(0);
        assertEquals(1, project1.getId());
        assertEquals("Projekt A", project1.getName());
        assertEquals(Date.valueOf("2025-12-31"), project1.getEndDate());

        Project project2 = projects.get(1);
        assertEquals(2, project2.getId());
        assertEquals("Projekt B", project2.getName());
        assertEquals(Date.valueOf("2026-01-15"), project2.getEndDate());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetAllProjects_Empty() throws SQLException {
        // Domyślna konfiguracja mockResultSet (next() zwraca false) symuluje brak wyników

        List<Project> projects = Project.getAllProjects();

        // Weryfikuj, czy zapytanie SELECT zostało wykonane
        verify(mockConnection).prepareStatement("SELECT id, name, end_date FROM project");
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócona lista jest pusta
        assertNotNull(projects);
        assertTrue(projects.isEmpty());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetAllProjects_SQLException() throws SQLException {
        // Skonfiguruj mock PreparedStatement, aby rzucił SQLException przy wywołaniu executeQuery
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQLException"));

        List<Project> projects = Project.getAllProjects();

        // Weryfikuj, czy metoda złapała wyjątek i zwróciła pustą listę
        assertNotNull(projects);
        assertTrue(projects.isEmpty());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte (nawet w przypadku błędu)
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
        // resultSet nie zostanie utworzony, więc nie będzie zamknięty w tym scenariuszu błędu executeQuery
    }

    // --- Testy dla metody getProjectById ---

    @Test
    void testGetProjectById_Success() throws SQLException {
        // Skonfiguruj mockResultSet, aby zwrócił przykładowe dane projektu
        when(mockResultSet.next()).thenReturn(true); // Znaleziono jeden projekt
        when(mockResultSet.getString("name")).thenReturn("Projekt X");
        when(mockResultSet.getString("description")).thenReturn("Szczegóły projektu X");
        when(mockResultSet.getInt("total_weight")).thenReturn(100);
        when(mockResultSet.getString("status")).thenReturn("active");
        when(mockResultSet.getDate("start_date")).thenReturn(Date.valueOf("2025-01-01"));
        when(mockResultSet.getDate("end_date")).thenReturn(Date.valueOf("2025-12-31"));
        when(mockResultSet.getInt("admin_id")).thenReturn(10);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2024-11-01 08:00:00"));
        when(mockResultSet.getShort("progress")).thenReturn((short) 50);

        int projectIdToFind = 10;
        Project project = Project.getProjectById(projectIdToFind);

        // Weryfikuj, czy zapytanie zostało wykonane z poprawnym ID
        verify(mockConnection).prepareStatement("SELECT name, description, total_weight, status, start_date, end_date, admin_id, created_at, progress FROM project WHERE id = ?");
        verify(mockPreparedStatement).setInt(1, projectIdToFind);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócony obiekt Project jest poprawny
        assertNotNull(project);
        assertEquals(projectIdToFind, project.getId()); // ID jest ustawiane ręcznie w modelu
        assertEquals("Projekt X", project.getName());
        assertEquals("Szczegóły projektu X", project.getDescription());
        assertEquals(100, project.getTotalWeight());
        assertEquals("active", project.getStatus());
        assertEquals(Date.valueOf("2025-01-01"), project.getStartDate());
        assertEquals(Date.valueOf("2025-12-31"), project.getEndDate());
        assertEquals(10, project.getAdminId());
        assertEquals(Timestamp.valueOf("2024-11-01 08:00:00"), project.getCreatedAt());
        assertEquals((short) 50, project.getProgress());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetProjectById_NotFound() throws SQLException {
        // Domyślna konfiguracja mockResultSet (next() zwraca false) symuluje brak wyników

        int projectIdToFind = 99;
        Project project = Project.getProjectById(projectIdToFind);

        // Weryfikuj, czy zapytanie zostało wykonane
        verify(mockConnection).prepareStatement("SELECT name, description, total_weight, status, start_date, end_date, admin_id, created_at, progress FROM project WHERE id = ?");
        verify(mockPreparedStatement).setInt(1, projectIdToFind);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócony obiekt Project to null
        assertNull(project);

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetProjectById_SQLException() throws SQLException {
        // Skonfiguruj mock PreparedStatement, aby rzucił SQLException przy wywołaniu executeQuery
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQLException"));

        int projectIdToFind = 10;
        Project project = Project.getProjectById(projectIdToFind);

        // Weryfikuj, czy metoda złapała wyjątek i zwróciła null
        assertNull(project);

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte (nawet w przypadku błędu)
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
        // resultSet nie zostanie utworzony, więc nie będzie zamknięty
    }

    // --- Testy dla getterów i setterów ---

    @Test
    void testGetterAndSetter() {
        Project project = new Project();

        // Ustawianie wartości za pomocą setterów
        project.setId(3);
        project.setName("Projekt Y");
        project.setDescription("Opis projektu Y");
        project.setTotalWeight(100);
        project.setStatus("draft");
        Date startDate = Date.valueOf("2025-05-01");
        Date endDate = Date.valueOf("2025-11-30");
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setAdminId(20);
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        project.setCreatedAt(createdAt);
        project.setProgress((short) 10);

        // Sprawdzanie wartości za pomocą getterów
        assertEquals(3, project.getId());
        assertEquals("Projekt Y", project.getName());
        assertEquals("Opis projektu Y", project.getDescription());
        assertEquals(100, project.getTotalWeight());
        assertEquals("draft", project.getStatus());
        assertEquals(startDate, project.getStartDate());
        assertEquals(endDate, project.getEndDate());
        assertEquals(20, project.getAdminId());
        assertEquals(createdAt, project.getCreatedAt());
        assertEquals((short) 10, project.getProgress());
    }
}