package com.mycompany.taskflow.model;

public enum TaskStatus {
    TO_DO("To Do"),
    IN_PROGRESS("In Progress"),
    DONE("Done");

    private final String dbValue;

    TaskStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public static TaskStatus fromString(String status) {
        if (status == null) {
            return TO_DO; // Domyślna wartość dla null
        }

        // Usuń zbędne białe znaki i znormalizuj format
        String normalized = status.trim().replace(" ", "").toUpperCase();

        // Sprawdź wszystkie możliwe warianty
        for (TaskStatus taskStatus : values()) {
            if (taskStatus.name().equals(normalized) ||
                    taskStatus.dbValue.equalsIgnoreCase(status)) {
                return taskStatus;
            }
        }

        // Obsługa ewentualnych alternatywnych nazw
        switch (normalized) {
            case "TODO":
            case "TOD":
                return TO_DO;
            case "INPROGRESS":
            case "INPROG":
                return IN_PROGRESS;
        }

        throw new IllegalArgumentException("Nieznana wartość statusu: " + status);
    }

    /**
     * Zwraca wartość zgodną z enumem w bazie danych
     */
    public String getDbValue() {
        return dbValue;
    }

    /**
     * Konwersja z wartości bazy danych na enum
     */
    public static TaskStatus fromDbValue(String dbValue) {
        if (dbValue == null) {
            return TO_DO; // Domyślna wartość
        }

        for (TaskStatus status : values()) {
            if (status.dbValue.equals(dbValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Nieznana wartość statusu: " + dbValue);
    }

    /**
     * Konwersja na czytelną nazwę dla UI
     */
    public String toDisplayName() {
        return dbValue; // Możesz dostosować formatowanie jeśli potrzebne
    }

    @Override
    public String toString() {
        return dbValue; // Domyślna reprezentacja tekstowa
    }
}