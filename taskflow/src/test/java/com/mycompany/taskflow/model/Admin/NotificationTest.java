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

public class NotificationTest {

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

        // Skonfiguruj mock Connection
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Skonfiguruj mock PreparedStatement do zwracania ResultSet (dla zapytań SELECT)
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Skonfiguruj mock PreparedStatement do zwracania liczby zaktualizowanych wierszy (dla zapytań UPDATE/DELETE)
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Domyślnie 1 wiersz zmieniony/usunięty

        // Domyślna konfiguracja mockResultSet: brak wyników
        when(mockResultSet.next()).thenReturn(false); // Domyślnie nie ma kolejnych wierszy
    }

    // Ważne: Po każdym teście zakończ mockowanie statyczne
    @AfterEach
    public void tearDown() {
        mockedDatabaseModel.close();
    }

    // --- Testy dla metody getAllNotifications ---

    @Test
    void testGetAllNotifications_Success() throws SQLException {
        // Skonfiguruj mockResultSet, aby zwrócił przykładowe dane powiadomienia
        when(mockResultSet.next()).thenReturn(true, true, false); // Symuluj 2 wiersze
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getInt("user_id")).thenReturn(10, 20);
        when(mockResultSet.getObject("task_id")).thenReturn(100, null); // Testowanie wartości NULL
        when(mockResultSet.getObject("subtask_id")).thenReturn(null, 200); // Testowanie wartości NULL
        when(mockResultSet.getString("type")).thenReturn("reminder", "update");
        when(mockResultSet.getString("content")).thenReturn("Treść powiadomienia 1", "Treść powiadomienia 2");
        when(mockResultSet.getBoolean("is_read")).thenReturn(false, true);
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-01-01 10:00:00"), Timestamp.valueOf("2025-01-02 11:00:00"));

        List<Notification> notifications = Notification.getAllNotifications();

        // Weryfikuj, czy zapytanie SELECT zostało wykonane
        verify(mockConnection).prepareStatement("SELECT id, user_id, task_id, subtask_id, type, content, is_read, created_at FROM notification ORDER BY created_at DESC");
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócona lista powiadomień jest poprawna
        assertNotNull(notifications);
        assertEquals(2, notifications.size());

        Notification notification1 = notifications.get(0);
        assertEquals(1, notification1.getId());
        assertEquals(10, notification1.getUserId());
        assertEquals(100, notification1.getTaskId());
        assertNull(notification1.getSubtaskId());
        assertEquals("reminder", notification1.getType());
        assertEquals("Treść powiadomienia 1", notification1.getContent());
        assertFalse(notification1.isRead());
        assertEquals(Timestamp.valueOf("2025-01-01 10:00:00"), notification1.getCreatedAt());

        Notification notification2 = notifications.get(1);
        assertEquals(2, notification2.getId());
        assertEquals(20, notification2.getUserId());
        assertNull(notification2.getTaskId());
        assertEquals(200, notification2.getSubtaskId());
        assertEquals("update", notification2.getType());
        assertEquals("Treść powiadomienia 2", notification2.getContent());
        assertTrue(notification2.isRead());
        assertEquals(Timestamp.valueOf("2025-01-02 11:00:00"), notification2.getCreatedAt());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetAllNotifications_Empty() throws SQLException {
        // Domyślna konfiguracja mockResultSet (next() zwraca false) symuluje brak wyników

        List<Notification> notifications = Notification.getAllNotifications();

        // Weryfikuj, czy zapytanie SELECT zostało wykonane
        verify(mockConnection).prepareStatement("SELECT id, user_id, task_id, subtask_id, type, content, is_read, created_at FROM notification ORDER BY created_at DESC");
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócona lista jest pusta
        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetAllNotifications_SQLException() throws SQLException {
        // Skonfiguruj mock PreparedStatement, aby rzucił SQLException przy wywołaniu executeQuery
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQLException"));

        // Weryfikuj, czy metoda rzuca SQLException
        assertThrows(SQLException.class, () -> Notification.getAllNotifications());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte (nawet w przypadku błędu)
        verify(mockPreparedStatement).close(); // Prepared Statement powinien być zamknięty
        verify(mockConnection).close(); // Połączenie powinno być zamknięte
        // resultSet nie zostanie utworzony, więc nie będzie zamknięty w tym scenariuszu błędu executeQuery
    }

    // --- Testy dla metody markAsRead ---

    @Test
    void testMarkAsRead_Success() throws SQLException {
        int notificationIdToMark = 5;

        // Wywołaj metodę markAsRead
        Notification.markAsRead(notificationIdToMark);

        // Weryfikuj, czy zapytanie UPDATE zostało wykonane z poprawnym ID
        verify(mockConnection).prepareStatement("UPDATE notification SET is_read = TRUE WHERE id = ?");
        verify(mockPreparedStatement).setInt(1, notificationIdToMark);
        verify(mockPreparedStatement).executeUpdate(); // Sprawdź, czy executeUpdate zostało wywołane

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testMarkAsRead_SQLException() throws SQLException {
        // Skonfiguruj mock PreparedStatement, aby rzucił SQLException przy wywołaniu executeUpdate
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test SQLException"));

        int notificationIdToMark = 5;

        // Weryfikuj, czy metoda rzuca SQLException
        assertThrows(SQLException.class, () -> Notification.markAsRead(notificationIdToMark));

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte (nawet w przypadku błędu)
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    // --- Testy dla metody deleteNotification ---

    @Test
    void testDeleteNotification_Success() throws SQLException {
        int notificationIdToDelete = 10;

        // Wywołaj metodę deleteNotification
        Notification.deleteNotification(notificationIdToDelete);

        // Weryfikuj, czy zapytanie DELETE zostało wykonane z poprawnym ID
        verify(mockConnection).prepareStatement("DELETE FROM notification WHERE id = ?");
        verify(mockPreparedStatement).setInt(1, notificationIdToDelete);
        verify(mockPreparedStatement).executeUpdate(); // Sprawdź, czy executeUpdate zostało wywołane

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testDeleteNotification_SQLException() throws SQLException {
        // Skonfiguruj mock PreparedStatement, aby rzucił SQLException przy wywołaniu executeUpdate
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test SQLException"));

        int notificationIdToDelete = 10;

        // Weryfikuj, czy metoda rzuca SQLException
        assertThrows(SQLException.class, () -> Notification.deleteNotification(notificationIdToDelete));

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte (nawet w przypadku błędu)
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    // --- Testy dla metody getRecentNotifications ---

    @Test
    void testGetRecentNotifications_Success() throws SQLException {
        // Skonfiguruj mockResultSet, aby zwrócił przykładowe dane powiadomienia (tylko content)
        when(mockResultSet.next()).thenReturn(true, true, false); // Symuluj 2 wiersze
        when(mockResultSet.getString("content")).thenReturn("Treść 1", "Treść 2");

        int limit = 5;
        List<String> notificationsContent = Notification.getRecentNotifications(limit);

        // Weryfikuj, czy zapytanie SELECT zostało wykonane z poprawnym limitem
        verify(mockConnection).prepareStatement("SELECT content FROM notification ORDER BY created_at DESC LIMIT ?");
        verify(mockPreparedStatement).setInt(1, limit);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócona lista treści powiadomień jest poprawna
        assertNotNull(notificationsContent);
        assertEquals(2, notificationsContent.size());
        assertEquals("Treść 1", notificationsContent.get(0));
        assertEquals("Treść 2", notificationsContent.get(1));

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetRecentNotifications_Empty() throws SQLException {
        // Domyślna konfiguracja mockResultSet (next() zwraca false) symuluje brak wyników

        int limit = 5;
        List<String> notificationsContent = Notification.getRecentNotifications(limit);

        // Weryfikuj, czy zapytanie SELECT zostało wykonane
        verify(mockConnection).prepareStatement("SELECT content FROM notification ORDER BY created_at DESC LIMIT ?");
        verify(mockPreparedStatement).setInt(1, limit);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócona lista jest pusta
        assertNotNull(notificationsContent);
        assertTrue(notificationsContent.isEmpty());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetRecentNotifications_SQLException() throws SQLException {
        // Skonfiguruj mock PreparedStatement, aby rzucił SQLException przy wywołaniu executeQuery
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQLException"));

        int limit = 5;
        List<String> notificationsContent = Notification.getRecentNotifications(limit);

        // Weryfikuj, czy metoda złapała wyjątek i zwróciła pustą listę
        assertNotNull(notificationsContent);
        assertTrue(notificationsContent.isEmpty());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte (nawet w przypadku błędu)
        verify(mockPreparedStatement).close(); // Prepared Statement powinien być zamknięty
        verify(mockConnection).close(); // Połączenie powinno być zamknięte
        // resultSet nie zostanie utworzony, więc nie będzie zamknięty
    }


    // --- Testy dla getterów i setterów ---

    @Test
    void testGetterAndSetter() {
        Notification notification = new Notification();

        // Ustawianie wartości za pomocą setterów
        notification.setId(3);
        notification.setUserId(30);
        notification.setTaskId(300);
        notification.setSubtaskId(null); // Testowanie wartości NULL
        notification.setType("alert");
        notification.setContent("Ważny alert!");
        notification.setRead(true);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        notification.setCreatedAt(now);

        // Sprawdzanie wartości za pomocą getterów
        assertEquals(3, notification.getId());
        assertEquals(30, notification.getUserId());
        assertEquals(300, notification.getTaskId());
        assertNull(notification.getSubtaskId());
        assertEquals("alert", notification.getType());
        assertEquals("Ważny alert!", notification.getContent());
        assertTrue(notification.isRead());
        assertEquals(now, notification.getCreatedAt());

        // Testowanie ustawienia wartości NULL dla TaskId/SubtaskId
        notification.setTaskId(null);
        notification.setSubtaskId(400);
        assertNull(notification.getTaskId());
        assertEquals(400, notification.getSubtaskId());
    }
}