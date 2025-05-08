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

public class UserTest {

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

    // --- Testy dla metody getUserById ---

    @Test
    void testGetUserById_Success() throws SQLException {
        // Skonfiguruj mockResultSet, aby zwrócił przykładowe dane użytkownika
        when(mockResultSet.next()).thenReturn(true); // Znaleziono jednego użytkownika
        when(mockResultSet.getString("first_name")).thenReturn("Jan");
        when(mockResultSet.getString("last_name")).thenReturn("Kowalski");
        when(mockResultSet.getString("email")).thenReturn("jan.kowalski@example.com");
        when(mockResultSet.getString("password_hash")).thenReturn("hashedpassword");
        when(mockResultSet.getString("role")).thenReturn("user");
        // Mockujemy getObject dla team_id (Integer, może być NULL)
        when(mockResultSet.getObject("team_id")).thenReturn(10); // Symuluj wartość nie-NULL
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-01-01 08:00:00"));
        // Mockujemy getTimestamp dla last_login (może być NULL)
        when(mockResultSet.getTimestamp("last_login")).thenReturn(Timestamp.valueOf("2025-04-20 15:30:00")); // Symuluj wartość nie-NULL

        int userIdToFind = 1;
        User user = User.getUserById(userIdToFind);

        // Weryfikuj, czy zapytanie zostało wykonane z poprawnym ID
        verify(mockConnection).prepareStatement("SELECT first_name, last_name, email, password_hash, role, team_id, created_at, last_login FROM \"user\" WHERE id = ?");
        verify(mockPreparedStatement).setInt(1, userIdToFind);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócony obiekt User jest poprawny
        assertNotNull(user);
        assertEquals(userIdToFind, user.getId()); // ID jest ustawiane ręcznie w modelu
        assertEquals("Jan", user.getFirstName());
        assertEquals("Kowalski", user.getLastName());
        assertEquals("jan.kowalski@example.com", user.getEmail());
        assertEquals("hashedpassword", user.getPasswordHash());
        assertEquals("user", user.getRole());
        assertEquals(10, user.getTeamId()); // Teraz oczekujemy Integer 10 lub null
        assertEquals(Timestamp.valueOf("2025-01-01 08:00:00"), user.getCreatedAt());
        assertEquals(Timestamp.valueOf("2025-04-20 15:30:00"), user.getLastLogin());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetUserById_SuccessWithNulls() throws SQLException {
        // Skonfiguruj mockResultSet z wartościami NULL dla team_id i last_login
        when(mockResultSet.next()).thenReturn(true); // Znaleziono jednego użytkownika
        when(mockResultSet.getString("first_name")).thenReturn("Anna");
        when(mockResultSet.getString("last_name")).thenReturn("Nowak");
        when(mockResultSet.getString("email")).thenReturn("anna.nowak@example.com");
        when(mockResultSet.getString("password_hash")).thenReturn("anotherhash");
        when(mockResultSet.getString("role")).thenReturn("admin");
        when(mockResultSet.getObject("team_id")).thenReturn(null); // Symuluj wartość NULL dla team_id
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-03-10 09:00:00"));
        when(mockResultSet.getTimestamp("last_login")).thenReturn(null); // Symuluj wartość NULL dla last_login

        int userIdToFind = 2;
        User user = User.getUserById(userIdToFind);

        // Weryfikuj, czy zapytanie zostało wykonane z poprawnym ID
        verify(mockConnection).prepareStatement("SELECT first_name, last_name, email, password_hash, role, team_id, created_at, last_login FROM \"user\" WHERE id = ?");
        verify(mockPreparedStatement).setInt(1, userIdToFind);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócony obiekt User jest poprawny z wartościami NULL
        assertNotNull(user);
        assertEquals(userIdToFind, user.getId());
        assertEquals("Anna", user.getFirstName());
        assertEquals("Nowak", user.getLastName());
        assertEquals("anna.nowak@example.com", user.getEmail());
        assertEquals("anotherhash", user.getPasswordHash());
        assertEquals("admin", user.getRole());
        assertNull(user.getTeamId()); // Oczekujemy null
        assertEquals(Timestamp.valueOf("2025-03-10 09:00:00"), user.getCreatedAt());
        assertNull(user.getLastLogin()); // Oczekujemy null

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }


    @Test
    void testGetUserById_NotFound() throws SQLException {
        // Domyślna konfiguracja mockResultSet (next() zwraca false) symuluje brak wyników

        int userIdToFind = 99;
        User user = User.getUserById(userIdToFind);

        // Weryfikuj, czy zapytanie zostało wykonane
        verify(mockConnection).prepareStatement("SELECT first_name, last_name, email, password_hash, role, team_id, created_at, last_login FROM \"user\" WHERE id = ?");
        verify(mockPreparedStatement).setInt(1, userIdToFind);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócony obiekt User to null
        assertNull(user);

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetUserById_SQLException() throws SQLException {
        // Skonfiguruj mock PreparedStatement, aby rzucił SQLException przy wywołaniu executeQuery
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQLException"));

        int userIdToFind = 1;
        User user = User.getUserById(userIdToFind);

        // Weryfikuj, czy metoda złapała wyjątek i zwróciła null
        assertNull(user);

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte (nawet w przypadku błędu)
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
        // resultSet nie zostanie utworzony, więc nie będzie zamknięty
    }

    // --- Testy dla getterów i setterów ---

    @Test
    void testGetterAndSetter() {
        User user = new User();

        // Ustawianie wartości za pomocą setterów
        user.setId(3);
        user.setFirstName("Piotr");
        user.setLastName("Zielony");
        user.setEmail("piotr.zielony@example.com");
        user.setPasswordHash("newhashedpassword");
        user.setRole("manager");
        user.setTeamId(null); // Testowanie wartości NULL
        Timestamp createdAt = Timestamp.valueOf("2025-04-01 07:00:00");
        Timestamp lastLogin = Timestamp.valueOf("2025-04-25 10:00:00");
        user.setCreatedAt(createdAt);
        user.setLastLogin(lastLogin); // Testowanie wartości nie-NULL

        // Sprawdzanie wartości za pomocą getterów
        assertEquals(3, user.getId());
        assertEquals("Piotr", user.getFirstName());
        assertEquals("Zielony", user.getLastName());
        assertEquals("piotr.zielony@example.com", user.getEmail());
        assertEquals("newhashedpassword", user.getPasswordHash());
        assertEquals("manager", user.getRole());
        assertNull(user.getTeamId());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(lastLogin, user.getLastLogin());

        // Testowanie ustawienia wartości nie-NULL dla teamId
        user.setTeamId(5);
        assertEquals(5, user.getTeamId());

        // Testowanie ustawienia wartości NULL dla lastLogin
        user.setLastLogin(null);
        assertNull(user.getLastLogin());
    }
}