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

public class CommentTest {

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

    // --- Testy dla metody getCommentById ---

    @Test
    void testGetCommentById_Success() throws SQLException {
        // Skonfiguruj mockResultSet, aby zwrócił przykładowe dane komentarza
        when(mockResultSet.next()).thenReturn(true); // Znaleziono jeden komentarz
        when(mockResultSet.getObject("task_id")).thenReturn(10); // Mockujemy getObject, zwracamy Integer 10
        when(mockResultSet.getObject("subtask_id")).thenReturn(20); // Mockujemy getObject, zwracamy Integer 20
        when(mockResultSet.getInt("user_id")).thenReturn(1); // user_id jest NOT NULL, więc int jest OK
        when(mockResultSet.getString("content")).thenReturn("Testowy komentarz");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2025-01-01 10:00:00"));
        int commentIdToFind = 5;
        Comment comment = Comment.getCommentById(commentIdToFind);

        // Weryfikuj, czy zapytanie zostało wykonane z poprawnym ID
        verify(mockConnection).prepareStatement("SELECT task_id, subtask_id, user_id, content, created_at FROM comment WHERE id = ?");
        verify(mockPreparedStatement).setInt(1, commentIdToFind);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócony obiekt Comment jest poprawny
        assertNotNull(comment);
        assertEquals(commentIdToFind, comment.getId()); // ID jest ustawiane ręcznie w modelu
        assertEquals(10, comment.getTaskId());
        assertEquals(20, comment.getSubtaskId());
        assertEquals(1, comment.getUserId());
        assertEquals("Testowy komentarz", comment.getContent());
        assertEquals(Timestamp.valueOf("2025-01-01 10:00:00"), comment.getCreatedAt());

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetCommentById_NotFound() throws SQLException {
        // Domyślna konfiguracja mockResultSet (next() zwraca false) symuluje brak wyników

        int commentIdToFind = 99;
        Comment comment = Comment.getCommentById(commentIdToFind);

        // Weryfikuj, czy zapytanie zostało wykonane
        verify(mockConnection).prepareStatement("SELECT task_id, subtask_id, user_id, content, created_at FROM comment WHERE id = ?");
        verify(mockPreparedStatement).setInt(1, commentIdToFind);
        verify(mockPreparedStatement).executeQuery();

        // Weryfikuj, czy zwrócony obiekt Comment to null
        assertNull(comment);

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    void testGetCommentById_SQLException() throws SQLException {
        // Skonfiguruj mock PreparedStatement, aby rzucił SQLException przy wywołaniu executeQuery
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQLException"));

        int commentIdToFind = 5;
        Comment comment = Comment.getCommentById(commentIdToFind);

        // Weryfikuj, czy rzucono wyjątek (i został obsłużony w modelu - model zwraca null)
        assertNull(comment); // Model łapie wyjątek i zwraca null

        // Weryfikuj, czy zasoby bazy danych zostały zamknięte (nawet w przypadku błędu)
        verify(mockPreparedStatement).close(); // Prepared Statement powinien być zamknięty
        verify(mockConnection).close(); // Połączenie powinno być zamknięte
        // resultSet nie zostanie utworzony, więc nie będzie zamknięty
    }
}