package com.mycompany.taskflow.controller.Manager;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.TaskStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TaskManagerController {

    //<editor-fold defaultstate="collapsed" desc="FXML Pola">
    @FXML private TabPane tasksTabPane;
    @FXML private Tab taskListTab;
    @FXML private VBox taskListViewContainer;
    @FXML private TreeView<TaskItem> tasksByMilestoneTreeView;
    @FXML private ListView<String> taskAssignmentsListView;
    @FXML private ListView<String> completedTasksListView;
    @FXML private Tab taskPoolTab;
    @FXML private ListView<TaskItem> unassignedTasksListView;
    @FXML private ComboBox<String> assignUserComboBox;
    @FXML private Button assignUserButton;
    //</editor-fold>

    private ObservableList<TaskItem> allTasks = FXCollections.observableArrayList();
    private ObservableList<String> teamMembers = FXCollections.observableArrayList();
    private ObservableList<TaskItem> unassignedTasks = FXCollections.observableArrayList();
    private Map<String, List<TaskItem>> tasksGroupedByMilestone = new HashMap<>();

    public void initialize() {
        System.out.println("TaskManagerController: Inicjalizacja...");
        try {
            setupViews(); // Konfiguracja widoków najpierw
            loadData(); // Ładowanie danych
            populateViews(); // Wypełnianie widoków danymi

            assignUserButton.setOnAction(event -> assignSelectedTaskToUser());
            System.out.println("TaskManagerController: Inicjalizacja zakończona pomyślnie.");
        } catch (Exception e) { // Łapanie ogólnego Exception na wszelki wypadek
            System.err.println("TaskManagerController: Krytyczny błąd podczas inicjalizacji: " + e.getMessage());
            e.printStackTrace();
            showAlert("Krytyczny błąd", "Nie udało się zainicjalizować widoku menedżera zadań: \n" + e.getMessage());
        }
    }

    private void setupViews() {
        System.out.println("TaskManagerController: Konfiguracja widoków...");
        // Ustawienie ListView dla nieprzypisanych zadań
        unassignedTasksListView.setItems(unassignedTasks);
        unassignedTasksListView.setCellFactory(param -> new ListCell<TaskItem>() {
            @Override
            protected void updateItem(TaskItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        // Konfiguracja drzewa (CellFactory w populateViews)
        tasksByMilestoneTreeView.setShowRoot(false);

        // Konfiguracja ComboBox (elementy dodane w populateViews)
        assignUserComboBox.setPromptText("Wybierz użytkownika...");
        System.out.println("TaskManagerController: Konfiguracja widoków zakończona.");
    }

    private void loadData() throws SQLException {
        System.out.println("TaskManagerController: Rozpoczęcie ładowania danych...");
        loadTasksFromDatabase();
        loadTeamMembersFromDatabase();
        System.out.println("TaskManagerController: Zakończono ładowanie danych.");
    }

    private void populateViews() {
        System.out.println("TaskManagerController: Rozpoczęcie wypełniania widoków...");
        groupTasksByMilestone();
        populateTasksByMilestoneView();
        populateTaskAssignmentsView();
        populateCompletedTasksView();
        filterAndPopulateUnassignedTasksView();
        populateAssignUserComboBoxItems();
        System.out.println("TaskManagerController: Zakończono wypełnianie widoków.");
    }


    private void loadTasksFromDatabase() throws SQLException {
        System.out.println("TaskManagerController: Rozpoczynanie ładowania zadań z bazy...");
        allTasks.clear(); // Wyczyść listę przed załadowaniem
        String query = """
        SELECT t.id, t.name, t.description, t.status,
               m.name AS milestone_name,
               CONCAT(u.first_name, ' ', u.last_name) AS assigned_user,
               (SELECT COUNT(*) FROM subtask s WHERE s.task_id = t.id AND s.is_done = true) AS done_subtasks,
               (SELECT COUNT(*) FROM subtask s WHERE s.task_id = t.id) AS total_subtasks
        FROM task t
        LEFT JOIN milestone m ON t.milestone_id = m.id
        LEFT JOIN task_assignment ta ON t.id = ta.task_id
        LEFT JOIN "user" u ON ta.user_id = u.id
        ORDER BY t.id
        """;

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseModel.connect();
            System.out.println("TaskManagerController: Połączenie z bazą danych uzyskane.");
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            System.out.println("TaskManagerController: Zapytanie o zadania wykonane.");

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                int taskId = -1; // Domyślna wartość w razie błędu
                String name = null;
                String assignedUser = null;
                try {
                    taskId = rs.getInt("id");
                    name = rs.getString("name");
                    String description = rs.getString("description");
                    String milestone = rs.getString("milestone_name"); // Może być null
                    assignedUser = rs.getString("assigned_user"); // Może być null

                    // KLUCZOWE: Sprawdzenie czy CONCAT zwrócił NULL z powodu braku dopasowania w LEFT JOIN
                    if (rs.wasNull()) {
                        assignedUser = null;
                    }

                    String statusStr = rs.getString("status");
                    TaskStatus status = TaskStatus.fromString(statusStr); // Może rzucić IllegalArgumentException
                    boolean completed = TaskStatus.DONE.equals(status);

                    List<String> subtasksInfo = new ArrayList<>();
                    int totalSubtasks = rs.getInt("total_subtasks");
                    if (totalSubtasks > 0) {
                        subtasksInfo.add(String.format("%d/%d ukończonych",
                                rs.getInt("done_subtasks"), totalSubtasks));
                    }

                    // Dodaj zadanie do listy allTasks
                    allTasks.add(new TaskItem(taskId, name, description, milestone, assignedUser, completed, subtasksInfo));

                    // Logowanie dla każdego zadania (już PO udanym dodaniu)
                    System.out.println("  [OK] Załadowano zadanie: ID=" + taskId + ", Nazwa=" + name + ", Przypisany=" + assignedUser + ", Status=" + statusStr);

                } catch (Exception e) {
                    // Złap błąd dla konkretnego wiersza, zaloguj i kontynuuj z następnym
                    System.err.println("  [BŁĄD] Nie udało się przetworzyć wiersza zadania (ID=" + taskId + ", Nazwa=" + name + "): " + e.getMessage());
                    // Możesz chcieć zalogować pełny stack trace dla szczegółów: e.printStackTrace();
                    // Nie dodawaj tego zadania do listy allTasks, jeśli wystąpił błąd
                }
            }
            System.out.println("TaskManagerController: Przetworzono wierszy: " + rowCount);
            System.out.println("TaskManagerController: Zakończono ładowanie zadań. Rozmiar allTasks: " + allTasks.size());

        } catch (SQLException e) {
            System.err.println("TaskManagerController: Błąd SQL podczas ładowania zadań: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rzuć dalej, aby obsłużyć w initialize
        } finally {
            // Zamknij zasoby w bloku finally
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            System.out.println("TaskManagerController: Zasoby bazy danych (ładowanie zadań) zamknięte.");
        }
    }

    private void loadTeamMembersFromDatabase() throws SQLException {
        System.out.println("TaskManagerController: Ładowanie członków zespołu...");
        teamMembers.clear();
        String query = "SELECT id, first_name, last_name FROM \"user\" WHERE role != 'admin' ORDER BY last_name, first_name";

        try (Connection conn = DatabaseModel.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                teamMembers.add(rs.getString("first_name") + " " + rs.getString("last_name"));
            }
            System.out.println("TaskManagerController: Załadowano członków zespołu: " + teamMembers.size());
        } catch (SQLException e) {
            System.err.println("TaskManagerController: Błąd SQL podczas ładowania członków zespołu: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void groupTasksByMilestone() {
        tasksGroupedByMilestone.clear();
        for (TaskItem task : allTasks) {
            String milestoneKey = task.getMilestone() != null ? task.getMilestone() : "Bez kamienia milowego";
            tasksGroupedByMilestone.computeIfAbsent(milestoneKey, k -> new ArrayList<>()).add(task);
        }
    }

    private void populateTasksByMilestoneView() {
        System.out.println("TaskManagerController: Wypełnianie widoku drzewa zadań...");
        TreeItem<TaskItem> rootItem = new TreeItem<>(new TaskItem(-1, "Wszystkie Zadania", "", "", null, false, null));
        rootItem.setExpanded(true);

        tasksGroupedByMilestone.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String milestoneName = entry.getKey();
                    TreeItem<TaskItem> milestoneItem = new TreeItem<>(new TaskItem(-2, milestoneName, "", milestoneName, null, false, null));
                    milestoneItem.setExpanded(true);

                    List<TaskItem> tasksInMilestone = entry.getValue();
                    tasksInMilestone.sort(Comparator.comparing(TaskItem::getName));

                    for (TaskItem task : tasksInMilestone) {
                        if (task != null) {
                            TreeItem<TaskItem> taskTreeItem = new TreeItem<>(task);
                            milestoneItem.getChildren().add(taskTreeItem);
                        }
                    }

                    if (!milestoneItem.getChildren().isEmpty()) {
                        rootItem.getChildren().add(milestoneItem);
                    }
                });

        tasksByMilestoneTreeView.setRoot(rootItem);
        // Ustawienie CellFactory do wyświetlania tekstu w drzewie
        tasksByMilestoneTreeView.setCellFactory(tv -> new TreeCell<TaskItem>() {
            @Override
            protected void updateItem(TaskItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(""); // Resetuj styl
                } else {
                    if (item.getId() == -2) { // Kamień milowy
                        setText(item.getName());
                        setStyle("-fx-font-weight: bold;");
                    } else if (item.getId() != -1) { // Zadanie
                        String assigned = item.getAssignedUser() != null ? item.getAssignedUser() : "Nieprzypisane";
                        String subtasks = item.getSubtasksInfo().isEmpty() ? "" : " [" + String.join(", ", item.getSubtasksInfo()) + "]";
                        setText(item.getName() + " (" + assigned + ")" + subtasks);
                        setStyle("");
                    } else { // Korzeń (jeśli widoczny)
                        setText(item.getName());
                        setStyle("");
                    }
                }
            }
        });
        System.out.println("TaskManagerController: Zakończono wypełnianie widoku drzewa zadań.");
    }

    private void populateTaskAssignmentsView() {
        System.out.println("TaskManagerController: Wypełnianie widoku przypisań...");
        taskAssignmentsListView.getItems().clear();
        allTasks.stream()
                .sorted(Comparator.comparing(TaskItem::getName))
                .forEach(task -> {
                    String assignmentInfo = task.getName() + " - Przypisany do: " +
                            (task.getAssignedUser() != null ? task.getAssignedUser() : "Nieprzypisany");
                    taskAssignmentsListView.getItems().add(assignmentInfo);
                });
        System.out.println("TaskManagerController: Zakończono wypełnianie widoku przypisań.");
    }

    private void populateCompletedTasksView() {
        System.out.println("TaskManagerController: Wypełnianie widoku ukończonych zadań...");
        completedTasksListView.getItems().clear();
        allTasks.stream()
                .filter(TaskItem::isCompleted)
                .sorted(Comparator.comparing(TaskItem::getName))
                .forEach(task -> {
                    String completedTaskInfo = task.getName() + " (Cząstki: " +
                            (task.getSubtasksInfo().isEmpty() ? "Brak" : String.join(", ", task.getSubtasksInfo())) + ")";
                    completedTasksListView.getItems().add(completedTaskInfo);
                });
        System.out.println("TaskManagerController: Zakończono wypełnianie widoku ukończonych zadań.");
    }

    private void filterAndPopulateUnassignedTasksView() {
        System.out.println("TaskManagerController: Filtrowanie nieprzypisanych zadań...");
        System.out.println("  Rozmiar allTasks przed filtrem: " + allTasks.size());

        List<TaskItem> filteredList = allTasks.stream()
                // POPRAWIONY FILTR: Sprawdza czy assignedUser jest null LUB pusty/zawiera tylko białe znaki
                .filter(task -> task.getAssignedUser() == null || task.getAssignedUser().trim().isEmpty())
                .sorted(Comparator.comparing(TaskItem::getName))
                .collect(Collectors.toList());

        System.out.println("  Rozmiar listy po filtracji: " + filteredList.size());

        unassignedTasks.setAll(filteredList);

        System.out.println("  Rozmiar listy unassignedTasks (po setAll): " + unassignedTasks.size());
        // Opcjonalnie: Można dodać odświeżenie ListView, chociaż setAll powinno wystarczyć
        // unassignedTasksListView.refresh();
        System.out.println("TaskManagerController: Zakończono filtrowanie nieprzypisanych zadań.");
    }

    private void populateAssignUserComboBoxItems() {
        System.out.println("TaskManagerController: Wypełnianie ComboBox użytkowników...");
        assignUserComboBox.setItems(teamMembers);
        System.out.println("TaskManagerController: Zakończono wypełnianie ComboBox użytkowników.");
    }

    @FXML
    void assignSelectedTaskToUser() {
        System.out.println("TaskManagerController: Próba przypisania zadania...");
        TaskItem selectedTask = unassignedTasksListView.getSelectionModel().getSelectedItem();
        String selectedUserFullName = assignUserComboBox.getValue();

        // Walidacja wyboru
        if (selectedTask == null) {
            showAlert("Ostrzeżenie", "Proszę wybrać zadanie do przypisania.");
            System.out.println("  Przypisanie anulowane: nie wybrano zadania.");
            return;
        }
        if (selectedUserFullName == null || selectedUserFullName.isEmpty()) {
            showAlert("Ostrzeżenie", "Proszę wybrać użytkownika.");
            System.out.println("  Przypisanie anulowane: nie wybrano użytkownika.");
            return;
        }

        System.out.println("  Wybrano zadanie: ID=" + selectedTask.getId() + ", Nazwa=" + selectedTask.getName());
        System.out.println("  Wybrano użytkownika: " + selectedUserFullName);

        try {
            int taskId = selectedTask.getId();
            int userId = getUserIdByName(selectedUserFullName);

            if (taskId != -1 && userId != -1) {
                System.out.println("  Znaleziono ID użytkownika: " + userId);
                assignTaskToUserInDatabase(taskId, userId);
                System.out.println("  Zadanie przypisane w bazie danych.");

                // Aktualizacja danych w pamięci (liście allTasks) - kluczowe!
                Optional<TaskItem> taskToUpdateOpt = allTasks.stream()
                        .filter(t -> t.getId() == taskId)
                        .findFirst();

                if (taskToUpdateOpt.isPresent()) {
                    TaskItem taskToUpdate = taskToUpdateOpt.get();
                    taskToUpdate.setAssignedUser(selectedUserFullName);
                    System.out.println("  Zaktualizowano TaskItem w allTasks.");

                    // Odśwież widoki
                    System.out.println("  Odświeżanie widoków...");
                    filterAndPopulateUnassignedTasksView();
                    populateTaskAssignmentsView();
                    populateTasksByMilestoneView();
                    System.out.println("  Widoki odświeżone.");
                } else {
                    System.err.println("  [BŁĄD KRYTYCZNY] Nie znaleziono zadania (ID=" + taskId + ") w liście allTasks po przypisaniu w bazie!");
                    showAlert("Błąd wewnętrzny", "Nie udało się zaktualizować stanu zadania w aplikacji.");
                }

            } else {
                if (taskId == -1) {
                    System.err.println("  [BŁĄD] Wybrane zadanie ma nieprawidłowe ID (-1).");
                    showAlert("Błąd", "Wybrane zadanie ma nieprawidłowe ID.");
                }
                if (userId == -1) {
                    System.err.println("  [BŁĄD] Nie znaleziono ID dla użytkownika: " + selectedUserFullName);
                    showAlert("Błąd", "Nie znaleziono wybranego użytkownika w bazie danych.");
                }
            }
        } catch (SQLException e) {
            System.err.println("  [BŁĄD BAZY DANYCH] Podczas przypisywania zadania: " + e.getMessage());
            e.printStackTrace();
            showAlert("Błąd bazy danych", "Nie udało się przypisać zadania: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("  [BŁĄD OGÓLNY] Podczas przypisywania zadania: " + e.getMessage());
            e.printStackTrace();
            showAlert("Błąd aplikacji", "Wystąpił nieoczekiwany błąd: " + e.getMessage());
        }
    }

    private int getUserIdByName(String fullName) throws SQLException {
        // (Implementacja bez zmian, ale dodajmy logowanie na początku)
        System.out.println("  Pobieranie ID dla użytkownika: " + fullName);
        if (fullName == null || fullName.trim().isEmpty()) return -1;
        String[] names = fullName.split(" ", 2);
        if (names.length != 2) return -1;
        String firstName = names[0].trim();
        String lastName = names[1].trim();
        String query = "SELECT id FROM \"user\" WHERE first_name = ? AND last_name = ?";
        try (Connection conn = DatabaseModel.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, firstName); stmt.setString(2, lastName);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
        // Błąd SQL zostanie złapany w assignSelectedTaskToUser
    }

    private void assignTaskToUserInDatabase(int taskId, int userId) throws SQLException {
        // (Implementacja z transakcją DELETE + INSERT bez zmian, dodajmy logowanie)
        System.out.println("  Rozpoczęcie transakcji przypisania w bazie (DELETE+INSERT) dla taskID=" + taskId + ", userID=" + userId);
        String deleteQuery = "DELETE FROM task_assignment WHERE task_id = ?";
        String insertQuery = "INSERT INTO task_assignment (user_id, task_id, assigned_at) VALUES (?, ?, ?)";
        Connection conn = null;
        boolean committed = false;
        try {
            conn = DatabaseModel.connect();
            conn.setAutoCommit(false);
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, taskId);
                int deletedRows = deleteStmt.executeUpdate();
                System.out.println("    Usunięto starych przypisań dla taskID=" + taskId + ": " + deletedRows);
            }
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, userId); insertStmt.setInt(2, taskId); insertStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                insertStmt.executeUpdate();
                System.out.println("    Dodano nowe przypisanie taskID=" + taskId + " do userID=" + userId);
            }
            conn.commit();
            committed = true;
            System.out.println("  Transakcja przypisania ZATWIERDZONA.");
        } catch (SQLException e) {
            System.err.println("  Błąd SQL w transakcji przypisania: " + e.getMessage());
            if (conn != null && !committed) {
                try { conn.rollback(); System.err.println("  Transakcja przypisania WYCOFANA."); }
                catch (SQLException ex) { System.err.println("  Błąd podczas rollbacku: " + ex.getMessage()); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); }
                catch (SQLException ex) { System.err.println("  Błąd przy zamykaniu połączenia po transakcji: " + ex.getMessage());}
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //<editor-fold defaultstate="collapsed" desc="Klasa TaskItem">
    public static class TaskItem {
        private final int id;
        private final String name;
        private final String description;
        private final String milestone;
        private String assignedUser; // Może być null
        private final boolean completed;
        private final List<String> subtasksInfo;

        public TaskItem(int id, String name, String description, String milestone,
                        String assignedUser, boolean completed, List<String> subtasksInfo) {
            this.id = id;
            this.name = Objects.requireNonNullElse(name, "");
            this.description = Objects.requireNonNullElse(description, "");
            this.milestone = milestone;
            this.assignedUser = assignedUser; // Przyjmujemy null bezpośrednio
            this.completed = completed;
            this.subtasksInfo = Objects.requireNonNullElse(subtasksInfo, Collections.emptyList());
        }

        // Gettery
        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getMilestone() { return milestone; }
        public String getAssignedUser() { return assignedUser; } // Zwraca null jeśli nieprzypisane
        public boolean isCompleted() { return completed; }
        public List<String> getSubtasksInfo() { return subtasksInfo; }

        // Setter
        public void setAssignedUser(String assignedUser) { this.assignedUser = assignedUser; }

        @Override
        public String toString() {
            String assigned = assignedUser != null ? assignedUser : "Nieprzypisane";
            String mile = milestone != null ? milestone : "Bez kamienia";
            return String.format("ID: %d, Nazwa: %s (Kamień: %s, Przypisany: %s)", id, name, mile, assigned);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return id == ((TaskItem) o).id;
        }

        @Override
        public int hashCode() { return Objects.hash(id); }
    }
    //</editor-fold>
}