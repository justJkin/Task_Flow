package com.mycompany.taskflow.controller.Manager;

import com.mycompany.taskflow.model.*;
import com.mycompany.taskflow.model.Manager.*; // Zakładamy, że UserSession jest w tym lub nadrzędnym pakiecie
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class ManagerPanelController {

    // == Zakładka ZESPÓŁ ==
    @FXML private TableView<User> teamMembersTable;
    @FXML private TableColumn<User, String> memberNameColumn;
    @FXML private TableColumn<User, String> memberEmailColumn;
    @FXML private TableColumn<User, String> memberRoleColumn; // Nowa kolumna dla roli

    // == Zakładka KAMIENIE MILOWE i ZADANIA ==
    @FXML private TableView<Milestone> milestoneTable;
    @FXML private TableColumn<Milestone, String> milestoneNameColumn;
    @FXML private TableColumn<Milestone, Integer> milestoneWeightColumn;
    @FXML private Label milestoneInfoLabel;
    @FXML private Label remainingWeightLabel;

    // == Formularz ZADAŃ ==
    @FXML private TextField taskNameField;
    @FXML private TextArea taskDescriptionField;
    @FXML private DatePicker taskDueDatePicker;
    @FXML private ComboBox<Integer> taskPriorityComboBox;
    @FXML private TextField taskWeightField;
    @FXML private Button saveTaskButton;
    @FXML private Button cancelButton;

    private ObservableList<User> teamMembers = FXCollections.observableArrayList();
    private ObservableList<Milestone> milestones = FXCollections.observableArrayList();

    private Milestone selectedMilestone;
    private int remainingWeight;
    // Usunięto pole loggedInManager - będziemy pobierać dane bezpośrednio z UserSession
    // private User loggedInManager;

    @FXML
    public void initialize() {
        System.out.println("ManagerPanelController: Inicjalizacja...");
        UserSession session = null;
        try {
            session = UserSession.getInstance();
            if (session == null) {
                throw new IllegalStateException("UserSession nie została zainicjalizowana. Użytkownik nie jest zalogowany?");
            }
            System.out.println("ManagerPanelController: Pomyślnie pobrano UserSession dla użytkownika ID: " + session.getUserId());

            // Sprawdzenie czy zalogowany użytkownik ma przypisany zespół (ważne dla managera)
            if (session.getTeamId() == null) {
                System.err.println("ManagerPanelController: Zalogowany użytkownik (ID: " + session.getUserId() + ") nie ma przypisanego zespołu (teamId is null).");
                showAlert("Błąd Konfiguracji", "Twoje konto nie ma przypisanego zespołu. Skontaktuj się z administratorem.");
                // Można zablokować funkcjonalność lub rzucić wyjątek
                disableFunctionality(); // Przykładowa metoda blokująca UI
                return; // Zakończ inicjalizację jeśli brak zespołu
            }

        } catch (IllegalStateException e) {
            System.err.println("ManagerPanelController: Błąd krytyczny - " + e.getMessage());
            showAlert("Błąd Sesji", "Nie udało się pobrać danych sesji użytkownika. Spróbuj zalogować się ponownie.");
            disableFunctionality();
            return;
        }
        // =============================================================

        setupTableColumns(); // Konfiguracja kolumn tabel
        setupEventListeners(); // Konfiguracja listenerów
        setupTaskForm(); // Konfiguracja formularza zadań

        // Ładowanie danych na podstawie POBRANEJ sesji
        loadTeamMembers(session.getTeamId()); // Przekaż teamId z sesji
        loadMilestones(session.getUserId()); // Przekaż userId managera z sesji

        System.out.println("ManagerPanelController: Inicjalizacja zakończona.");
    }

    // Metoda pomocnicza do konfiguracji kolumn
    private void setupTableColumns() {
        // == Zakładka ZESPÓŁ ==
        memberNameColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            // Dodano sprawdzenie null na wszelki wypadek
            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            String lastName = user.getLastName() != null ? user.getLastName() : "";
            return new SimpleStringProperty(firstName + " " + lastName);
        });
        memberEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        memberRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role")); // Konfiguracja dla kolumny roli

        // == Zakładka KAMIENIE MILOWE ==
        milestoneNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        milestoneWeightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));
    }

    // Metoda pomocnicza do konfiguracji listenerów
    private void setupEventListeners() {
        milestoneTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    // Usunięto przypisanie do pola selectedMilestone, bo jest już w parametrze
                    showMilestoneDetails(newSelection);
                }
        );
    }

    // Metoda pomocnicza do konfiguracji formularza zadań
    private void setupTaskForm() {
        taskPriorityComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        taskPriorityComboBox.setPromptText("Wybierz...");
    }

    // == Metody ładowania danych (przyjmują teraz ID z sesji) ==

    private void loadTeamMembers(Integer managerTeamId) {
        System.out.println("ManagerPanelController: Ładowanie członków zespołu dla teamId: " + managerTeamId);
        if (managerTeamId == null) {
            System.err.println("ManagerPanelController: Nie można załadować członków zespołu - teamId jest null.");
            showAlert("Błąd", "Nie można zidentyfikować zespołu managera.");
            return;
        }

        try {
            teamMembers.clear();
            // Używamy teraz metody statycznej User z przekazanym teamId
            teamMembers.addAll(User.getUsersFromDatabaseByTeamId(managerTeamId));
            teamMembersTable.setItems(teamMembers);
            System.out.println("ManagerPanelController: Załadowano członków zespołu: " + teamMembers.size());
        } catch (SQLException e) {
            System.err.println("ManagerPanelController: Błąd SQL podczas ładowania członków zespołu: " + e.getMessage());
            e.printStackTrace();
            showAlert("Błąd Bazy Danych", "Nie można załadować członków zespołu.");
        }
    }

    private void loadMilestones(int managerId) { // Przyjmuje ID managera
        System.out.println("ManagerPanelController: Ładowanie kamieni milowych dla managera ID: " + managerId);
        try {
            milestones.clear();
            // Używamy metody statycznej Milestone z przekazanym ID managera
            milestones.addAll(Milestone.getMilestonesFromDatabaseByManagerId(managerId));
            milestoneTable.setItems(milestones);
            System.out.println("ManagerPanelController: Załadowano kamieni milowych: " + milestones.size());
        } catch (SQLException e) {
            System.err.println("ManagerPanelController: Błąd SQL podczas ładowania kamieni milowych: " + e.getMessage());
            e.printStackTrace();
            showAlert("Błąd Bazy Danych", "Nie można załadować kamieni milowych.");
        }
    }

    // == Formularz ZADAŃ ==

    @FXML
    private void handleSaveTask() {
        System.out.println("ManagerPanelController: Próba zapisu zadania...");
        // Pobierz zaznaczony kamień milowy bezpośrednio z tabeli
        Milestone currentSelectedMilestone = milestoneTable.getSelectionModel().getSelectedItem();

        if (currentSelectedMilestone == null) {
            showAlert("Błąd Walidacji", "Wybierz kamień milowy przed dodaniem zadania.");
            System.out.println("  Zapis anulowany: nie wybrano kamienia milowego.");
            return;
        }
        System.out.println("  Wybrany kamień milowy: ID=" + currentSelectedMilestone.getId() + ", Nazwa=" + currentSelectedMilestone.getName());


        Optional<String> errorMessage = validateTaskForm(currentSelectedMilestone); // Przekaż aktualnie wybrany kamień
        if (errorMessage.isPresent()) {
            showAlert("Błąd Walidacji", errorMessage.get());
            System.out.println("  Zapis anulowany: błąd walidacji - " + errorMessage.get());
            return;
        }
        System.out.println("  Walidacja formularza zakończona pomyślnie.");

        // Utwórz nowy obiekt Task
        Task newTask = new Task(
                0, // ID zostanie nadane przez bazę danych
                currentSelectedMilestone, // Użyj aktualnie wybranego
                taskNameField.getText().trim(), // Usuń białe znaki
                taskDescriptionField.getText().trim(),
                TaskStatus.TO_DO, // Domyślny status
                taskPriorityComboBox.getValue().shortValue(),
                Integer.parseInt(taskWeightField.getText()),
                (short) 0, // Domyślny postęp
                taskDueDatePicker.getValue(),
                LocalDateTime.now(), // createdAt
                LocalDateTime.now()  // updatedAt
        );
        System.out.println("  Utworzono obiekt newTask: " + newTask.getName());

        try {
            Task.saveTaskToDatabase(newTask); // Zapisz do bazy
            System.out.println("  Zadanie zapisane w bazie danych, nadane ID: " + newTask.getId());
            // Odśwież pozostałą wagę dla TEGO SAMEGO kamienia milowego
            calculateRemainingWeight(currentSelectedMilestone);
            System.out.println("  Odświeżono pozostałą wagę.");
            clearTaskForm();
            System.out.println("  Formularz zadania wyczyszczony.");
            showAlert("Sukces", "Zadanie '" + newTask.getName() + "' zostało dodane pomyślnie.");
        } catch (SQLException e) {
            System.err.println("  Błąd SQL podczas zapisu zadania: " + e.getMessage());
            e.printStackTrace();
            showAlert("Błąd Bazy Danych", "Nie można dodać zadania:\n" + e.getMessage());
        } catch (Exception e) {
            System.err.println("  Nieoczekiwany błąd podczas zapisu zadania: " + e.getMessage());
            e.printStackTrace();
            showAlert("Błąd Aplikacji", "Wystąpił nieoczekiwany błąd:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        System.out.println("ManagerPanelController: Anulowano operację na formularzu zadania.");
        clearTaskForm();
        milestoneTable.getSelectionModel().clearSelection();
        // Nie trzeba resetować labeli tutaj, bo zrobi to listener tabeli po wyczyszczeniu zaznaczenia
    }

    private void showMilestoneDetails(Milestone milestone) {
        if (milestone == null) {
            System.out.println("ManagerPanelController: Wyczyszczono zaznaczenie kamienia milowego.");
            milestoneInfoLabel.setText("Wybierz kamień milowy z tabeli powyżej.");
            remainingWeightLabel.setText("");
            clearTaskForm();
            // Można też zablokować formularz dodawania zadań
            setTaskFormDisabled(true);
        } else {
            System.out.println("ManagerPanelController: Wybrano kamień milowy: ID=" + milestone.getId() + ", Nazwa=" + milestone.getName());
            // Usunięto przypisanie do pola selectedMilestone - nie jest już potrzebne
            milestoneInfoLabel.setText("Wybrany kamień milowy: " + milestone.getName() +
                    "\nCałkowita waga kamienia: " + milestone.getWeight());
            calculateRemainingWeight(milestone); // Oblicz i wyświetl pozostałą wagę
            setTaskFormDisabled(false); // Odblokuj formularz
        }
    }

    private void calculateRemainingWeight(Milestone milestone) {
        System.out.println("  Obliczanie pozostałej wagi dla kamienia ID: " + milestone.getId());
        try {
            int usedWeight = Task.getUsedWeightForMilestone(milestone.getId());
            System.out.println("    Użyta waga: " + usedWeight);
            // Usunięto pole remainingWeight - obliczamy na bieżąco
            int currentRemainingWeight = milestone.getWeight() - usedWeight;
            remainingWeightLabel.setText("Pozostało wagi do rozdysponowania: " + currentRemainingWeight);
            System.out.println("    Pozostała waga: " + currentRemainingWeight);
        } catch (SQLException e) {
            System.err.println("  Błąd SQL podczas obliczania pozostałej wagi: " + e.getMessage());
            e.printStackTrace();
            remainingWeightLabel.setText("Błąd obliczania wagi!");
            // Nie pokazuj Alert tutaj, żeby nie irytować przy każdym wyborze
            // showAlert("Błąd Bazy Danych", "Nie można obliczyć pozostałej wagi.");
        }
    }

    private void clearTaskForm() {
        taskNameField.clear();
        taskDescriptionField.clear();
        taskPriorityComboBox.getSelectionModel().clearSelection(); // Poprawny sposób czyszczenia ComboBox
        taskPriorityComboBox.setPromptText("Wybierz..."); // Przywróć prompt text
        taskWeightField.clear();
        taskDueDatePicker.setValue(null);
        System.out.println("  Formularz zadania wyczyszczony (pola).");
    }

    // Metoda blokująca/odblokowująca formularz zadania
    private void setTaskFormDisabled(boolean disabled) {
        taskNameField.setDisable(disabled);
        taskDescriptionField.setDisable(disabled);
        taskDueDatePicker.setDisable(disabled);
        taskPriorityComboBox.setDisable(disabled);
        taskWeightField.setDisable(disabled);
        saveTaskButton.setDisable(disabled);
        cancelButton.setDisable(disabled); // Przycisk Anuluj też warto kontrolować
    }

    // Metoda blokująca całą funkcjonalność (w przypadku braku sesji/zespołu)
    private void disableFunctionality() {
        System.out.println("ManagerPanelController: Blokowanie funkcjonalności panelu managera.");
        // Zablokuj tabele
        teamMembersTable.setDisable(true);
        milestoneTable.setDisable(true);
        // Zablokuj formularz
        setTaskFormDisabled(true);
        // Zaktualizuj etykiety
        milestoneInfoLabel.setText("Funkcjonalność niedostępna.");
        remainingWeightLabel.setText("");
    }

    // == Walidacja ==

    private Optional<String> validateTaskForm(Milestone currentMilestone) { // Przyjmuje aktualny kamień
        System.out.println("  Rozpoczęcie walidacji formularza zadania...");
        String name = taskNameField.getText();
        String description = taskDescriptionField.getText();
        LocalDate dueDate = taskDueDatePicker.getValue();
        Integer priority = taskPriorityComboBox.getValue();
        String weightStr = taskWeightField.getText();

        if (name == null || name.trim().isEmpty()) {
            return Optional.of("Nazwa zadania jest wymagana.");
        }
        if (description == null || description.trim().isEmpty()) {
            return Optional.of("Opis zadania jest wymagany.");
        }
        if (dueDate == null) {
            return Optional.of("Termin zadania jest wymagany.");
        }
        // Sprawdzenie, czy data nie jest z przeszłości (opcjonalne)
        if (dueDate.isBefore(LocalDate.now())) {
            return Optional.of("Termin zadania nie może być datą z przeszłości.");
        }
        if (priority == null) {
            return Optional.of("Priorytet zadania jest wymagany.");
        }
        if (weightStr == null || weightStr.trim().isEmpty()) {
            return Optional.of("Waga zadania jest wymagana.");
        }

        int weight;
        try {
            weight = Integer.parseInt(weightStr.trim());
        } catch (NumberFormatException e) {
            return Optional.of("Waga zadania musi być poprawną liczbą całkowitą.");
        }

        if (weight <= 0) {
            return Optional.of("Waga zadania musi być większa od 0.");
        }

        // Oblicz pozostałą wagę ponownie na moment walidacji
        try {
            int usedWeight = Task.getUsedWeightForMilestone(currentMilestone.getId());
            int currentRemainingWeight = currentMilestone.getWeight() - usedWeight;
            System.out.println("    Walidacja wagi: Wprowadzono=" + weight + ", Pozostało=" + currentRemainingWeight);
            if (weight > currentRemainingWeight) {
                return Optional.of("Waga zadania (" + weight + ") nie może przekraczać pozostałej dostępnej wagi w kamieniu milowym (" + currentRemainingWeight + ").");
            }
        } catch (SQLException e) {
            System.err.println("  Błąd SQL podczas walidacji wagi: " + e.getMessage());
            e.printStackTrace();
            // Zwróć błąd walidacji, jeśli nie można sprawdzić wagi
            return Optional.of("Nie można zweryfikować pozostałej wagi w bazie danych. Spróbuj ponownie.");
        }

        System.out.println("  Walidacja zakończona: OK.");
        return Optional.empty(); // Brak błędów
    }

    // == Pomocnicze ==

    private void showAlert(String title, String message) {
        // Upewnij się, że alert jest odpowiedniego typu
        Alert.AlertType alertType = title.toLowerCase().contains("błąd") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}