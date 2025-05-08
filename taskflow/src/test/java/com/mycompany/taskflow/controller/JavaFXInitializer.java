package com.mycompany.taskflow.controller;

import javafx.application.Platform;

/**
 * Centralna klasa do inicjalizacji platformy JavaFX dla testów.
 * Używa statycznego bloku, aby próbować zainicjalizować toolkit przy pierwszym załadowaniu klasy.
 * Obsługuje przypadek, gdy toolkit jest już zainicjalizowany.
 */
public class JavaFXInitializer {

    // Statyczny blok inicjalizacyjny, uruchamiany przy pierwszym załadowaniu klasy przez JVM
    static {
        try {
            // Próba uruchomienia platformy JavaFX.
            // () -> {} to minimalny Runnable wymagany przez startup.
            Platform.startup(() -> {});
            // System.out.println("JavaFXInitializer: Platforma JavaFX zainicjalizowana."); // Opcjonalne logowanie
        } catch (IllegalStateException e) {
            // Sprawdź, czy wyjątek dotyczy już zainicjalizowanego toolkita
            if ("Toolkit already initialized".equals(e.getMessage())) {
                // System.out.println("JavaFXInitializer: Toolkit już zainicjalizowany, ignoruję."); // Opcjonalne logowanie
                // Ignoruj, jeśli toolkit jest już zainicjalizowany - to oczekiwane w niektórych scenariuszach testowych
            } else {
                // Zgłoś inny błąd IllegalStateException, ponieważ wskazuje na inny problem
                System.err.println("JavaFXInitializer: Nieoczekiwany błąd IllegalStateException podczas Platform.startup().");
                e.printStackTrace();
                throw e; // Ważne: zgłoś ponownie, jeśli to nie jest błąd "już zainicjalizowany"
            }
        } catch (Exception e) {
            // Złap inne potencjalne wyjątki podczas procesu startupu
            System.err.println("JavaFXInitializer: Nieoczekiwany wyjątek podczas inicjalizacji Platform.startup().");
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize JavaFX Platform", e);
        }
    }

    /**
     * Metoda pomocnicza, którą testy mogą wywołać w swoich metodach @BeforeAll.
     * Wywołanie tej metody gwarantuje, że statyczny blok inicjalizacyjny klasy JavaFXInitializer
     * został wykonany, co zapewnia próbę inicjalizacji platformy JavaFX.
     */
    public static void ensureInitialized() {
        // Pusta metoda - jej jedynym celem jest bycie punktem wywołania
        // do zagwarantowania załadowania tej klasy i wykonania bloku statycznego.
        // System.out.println("JavaFXInitializer: ensureInitialized() wywołana."); // Opcjonalne logowanie
    }
}