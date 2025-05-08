package com.mycompany.taskflow.model.Admin;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeadlineTest {

    // --- Testy dla modelu Deadline ---

    @Test
    void testConstructorAndGetters() {
        // Przykładowe dane
        String name = "Testowy Projekt";
        LocalDate deadlineDate = LocalDate.of(2025, 12, 31);
        String type = "projekt";
        String user = "testuser";
        List<String> team = Arrays.asList("Zespół A", "Zespół B");

        // Utwórz instancję Deadline
        Deadline deadline = new Deadline(name, deadlineDate, type, user, team);

        // Sprawdź, czy gettery zwracają poprawne wartości
        assertEquals(name, deadline.getName(), "Getter getName powinien zwrócić poprawną nazwę.");
        assertEquals(deadlineDate, deadline.getDeadline(), "Getter getDeadline powinien zwrócić poprawną datę.");
        assertEquals(type, deadline.getType(), "Getter getType powinien zwrócić poprawny typ.");
        assertEquals(user, deadline.getUser(), "Getter getUser powinien zwrócić poprawnego użytkownika.");
        assertEquals(team, deadline.getTeam(), "Getter getTeam powinien zwrócić poprawną listę zespołów.");
    }

    @Test
    void testConstructorWithNullValues() {
        // Przykładowe dane z wartościami null
        String name = "Zadanie bez przypisania";
        LocalDate deadlineDate = LocalDate.of(2025, 11, 15);
        String type = "zadanie";
        String user = null;
        List<String> team = null;

        // Utwórz instancję Deadline z wartościami null
        Deadline deadline = new Deadline(name, deadlineDate, type, user, team);

        // Sprawdź, czy gettery zwracają poprawne wartości (w tym null)
        assertEquals(name, deadline.getName(), "Getter getName powinien zwrócić poprawną nazwę.");
        assertEquals(deadlineDate, deadline.getDeadline(), "Getter getDeadline powinien zwrócić poprawną datę.");
        assertEquals(type, deadline.getType(), "Getter getType powinien zwrócić poprawny typ.");
        assertNull(deadline.getUser(), "Getter getUser powinien zwrócić null dla użytkownika.");
        assertNull(deadline.getTeam(), "Getter getTeam powinien zwrócić null dla listy zespołów.");
    }
}