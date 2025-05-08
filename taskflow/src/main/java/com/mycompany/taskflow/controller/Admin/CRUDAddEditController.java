package com.mycompany.taskflow.controller.Admin;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import com.mycompany.taskflow.controller.Admin.MilestonesTabController;
import com.mycompany.taskflow.model.Admin.Milestone;
import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.Admin.User;
import com.mycompany.taskflow.model.Admin.Team;
import com.mycompany.taskflow.model.Admin.Project;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;

public class CRUDAddEditController {

    @FXML
    private Label titleLabel;

    @FXML
    private VBox formContainer;

    @FXML private TabPane tabPane;
    @FXML private Tab dataTab;
    @FXML private Tab msTab;
    @FXML private MilestonesTabController msTabController;
    private boolean milestonesLoaded = false;

    String sectionName;
    private User currentUser;
    private Object item;
    private Map<String, Control> fieldMap = new HashMap<>();
    private DataChangedListener dataChangedListener;

    @FXML
    public TextField firstNameTextField;
    @FXML
    public TextField lastNameTextField;
    @FXML
    public TextField emailTextField;
    @FXML
    public ComboBox<String> roleComboBox;
    @FXML
    public ComboBox<Integer> teamIdComboBox;
    @FXML
    public PasswordField passwordField;
    @FXML
    public PasswordField confirmPasswordField;

    @FXML private VBox msTabContent; // odpowiada VBoxowi z MilestonesTab.fxml
    @FXML private VBox msTabInclude;

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
        titleLabel.setText((item == null ? "Dodaj " : "Edytuj ") + sectionName);

        createFormFields();
        populateFields();

        tabPane.getSelectionModel().select(dataTab);

        if (!"Projekty".equals(sectionName)) {
            tabPane.getTabs().remove(msTab);
        }

        if (msTabInclude != null) {
            Object controller = msTabInclude.getProperties().get("controller");
            if (controller instanceof MilestonesTabController) {
                msTabController = (MilestonesTabController) controller;
            }
        }



        if ("Projekty".equals(sectionName)) {
            // opóźnione ładowanie kamieni milowych po kliknięciu zakładki
            msTab.setOnSelectionChanged(event -> {
                if (msTab.isSelected() && !milestonesLoaded && item instanceof Project && msTabController != null) {
                    msTabController.loadForProject((Project) item);
                    milestonesLoaded = true;
                }
            });
        }
    }



    public void setItem(Object item) {
        this.item = item;
        this.currentUser = (item instanceof User) ? (User) item : null;

        if (sectionName != null) {
            populateFields();
            milestonesLoaded = false; // reset przy każdym przypisaniu
        }
    }



    public void setOnDataChangedListener(DataChangedListener listener) {
        this.dataChangedListener = listener;
    }

    private int insertProjectAndReturnId() throws SQLException {
        TextField nameField = (TextField) fieldMap.get("name");
        TextArea descField = (TextArea) fieldMap.get("description");
        DatePicker startPicker = (DatePicker) fieldMap.get("startDate");
        DatePicker endPicker = (DatePicker) fieldMap.get("endDate");

        return DatabaseModel.insertProjectAndReturnId(
                nameField.getText(),
                descField.getText(),
                startPicker.getValue(),
                endPicker.getValue()
        );
    }


    public void createFormFields() {
        formContainer.getChildren().clear();
        fieldMap.clear();

        switch (sectionName) {
            case "Użytkownicy":
                firstNameTextField = new TextField();
                lastNameTextField = new TextField();
                emailTextField = new TextField();
                roleComboBox = new ComboBox<>();
                teamIdComboBox = new ComboBox<>();
                passwordField = new PasswordField();
                confirmPasswordField = new PasswordField();

                addTextField("Imię", "firstName", firstNameTextField);
                addTextField("Nazwisko", "lastName", lastNameTextField);
                addTextField("Email", "email", emailTextField);
                addRoleComboBox("Rola", "role", roleComboBox);
                addTeamIdComboBox("Team ID", "teamId", teamIdComboBox);
                addPasswordField("Hasło", "password", passwordField);
                addPasswordField("Potwierdź hasło", "confirmPassword", confirmPasswordField);
                break;
            case "Zespoły":
                addTextField("Nazwa", "name");
                break;
            case "Projekty":
                addTextField("Nazwa", "name");
                addTextAreaField("Opis", "description");
                addDatePicker("Data rozpoczęcia", "startDate");
                addDatePicker("Data zakończenia", "endDate");
                break;
        }
    }

    public void addDatePicker(String labelText, String fieldName) {
        Label label = new Label(labelText + ":");
        DatePicker datePicker = new DatePicker();
        HBox hbox = new HBox(10, label, datePicker);
        formContainer.getChildren().add(hbox);
        fieldMap.put(fieldName, datePicker);
    }

    public void addPasswordField(String labelText, String fieldName, PasswordField passwordField) {
        Label label = new Label(labelText + ":");
        HBox hbox = new HBox(10, label, passwordField);
        formContainer.getChildren().add(hbox);
        fieldMap.put(fieldName, passwordField);
    }

    public void addRoleComboBox(String labelText, String fieldName, ComboBox<String> comboBox) {
        Label label = new Label(labelText + ":");
        try {
            List<String> roles = DatabaseModel.getRolesFromDatabase();
            comboBox.getItems().addAll(roles);
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas pobierania ról z bazy danych.");
            alert.showAndWait();
        }
        HBox hbox = new HBox(10, label, comboBox);
        formContainer.getChildren().add(hbox);
        fieldMap.put(fieldName, comboBox);
    }

    public void addTextField(String labelText, String fieldName) {
        Label label = new Label(labelText + ":");
        TextField textField = new TextField();
        HBox hbox = new HBox(10, label, textField);
        formContainer.getChildren().add(hbox);
        fieldMap.put(fieldName, textField);
    }

    public void addTextField(String labelText, String fieldName, TextField textField) {
        Label label = new Label(labelText + ":");
        HBox hbox = new HBox(10, label, textField);
        formContainer.getChildren().add(hbox);
        fieldMap.put(fieldName, textField);
    }

    public void addTextAreaField(String labelText, String fieldName) {
        Label label = new Label(labelText + ":");
        TextArea textArea = new TextArea();
        HBox hbox = new HBox(10, label, textArea);
        formContainer.getChildren().add(hbox);
        fieldMap.put(fieldName, textArea);
    }

    public void addTeamIdComboBox(String labelText, String fieldName, ComboBox<Integer> comboBox) {
        Label label = new Label(labelText + ":");
        try {
            List<Integer> teamIds = DatabaseModel.getTeamIdsFromDatabase();
            comboBox.getItems().addAll(teamIds);
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas pobierania ID zespołów z bazy danych.");
            alert.showAndWait();
        }
        HBox hbox = new HBox(10, label, comboBox);
        formContainer.getChildren().add(hbox);
        fieldMap.put(fieldName, comboBox);
    }

    public void populateFields() {
        if (item != null) {
            switch (sectionName) {
                case "Użytkownicy":
                    User user = (User) item;
                    firstNameTextField.setText(user.getFirstName());
                    lastNameTextField.setText(user.getLastName());
                    emailTextField.setText(user.getEmail());
                    roleComboBox.setValue(user.getRole());
                    teamIdComboBox.setValue(user.getTeamId());
                    break;
                case "Zespoły":
                    Team team = (Team) item;
                    if (fieldMap.containsKey("name")) {
                        ((TextField) fieldMap.get("name")).setText(team.getName());
                    }
                    break;
                case "Projekty":
                    Project project = (Project) item;
                    if (fieldMap.containsKey("name")) {
                        ((TextField) fieldMap.get("name")).setText(project.getName());
                    }
                    if (fieldMap.containsKey("description")) {
                        ((TextArea) fieldMap.get("description")).setText(project.getDescription());
                    }
                    if (project.getStartDate() != null && fieldMap.containsKey("startDate")) {
                        ((DatePicker) fieldMap.get("startDate")).setValue(project.getStartDate().toLocalDate());
                    }
                    if (project.getEndDate() != null && fieldMap.containsKey("endDate")) {
                        ((DatePicker) fieldMap.get("endDate")).setValue(project.getEndDate().toLocalDate());
                    }
                    break;
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Uwaga");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleSave() {
        try {
            if ("Użytkownicy".equals(sectionName)) {
                // WALIDACJA UŻYTKOWNIKA
                String password = passwordField.getText();
                String confirmPassword = confirmPasswordField.getText();
                try {
                    validateUserInput(
                            firstNameTextField.getText(),
                            lastNameTextField.getText(),
                            emailTextField.getText(),
                            password,
                            confirmPassword,
                            item == null
                    );
                } catch (IllegalArgumentException ex) {
                    new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
                    return;
                }

                // HASHOWANIE HASŁA
                String passwordHash = null;
                if (password != null && !password.isEmpty()) {
                    passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
                } else if (item instanceof User) {
                    passwordHash = ((User) item).getPasswordHash();
                }

                // INSERT lub UPDATE UŻYTKOWNIKA
                if (item == null) {
                    insertUser(passwordHash);
                } else {
                    updateUserSelective(passwordHash);
                }

                // ZAMKNIJ OKNO I ODŚWIEŻ LISTĘ
                Stage stage = (Stage) titleLabel.getScene().getWindow();
                stage.close();
                if (dataChangedListener != null) {
                    dataChangedListener.onDataChanged();
                }
                return;
            }
            else if ("Zespoły".equals(sectionName)) {
                // INSERT lub UPDATE ZESPOŁU
                if (item == null) {
                    insertData();

                } else {
                    updateData();
                }
                Stage stage = (Stage) titleLabel.getScene().getWindow();
                stage.close();
                if (dataChangedListener != null) {
                    dataChangedListener.onDataChanged();
                }
                return;
            }
            else if ("Projekty".equals(sectionName)) {

                // WALIDACJA POL DAT
                DatePicker startPicker = (DatePicker) fieldMap.get("startDate");
                DatePicker endPicker = (DatePicker) fieldMap.get("endDate");
                LocalDate start = startPicker.getValue();
                LocalDate end = endPicker.getValue();

// Przy edycji — domyślnie użyj poprzednich dat jeśli nie ustawiono nowych
                if (item instanceof Project) {
                    Project p = (Project) item;
                    if (start == null && p.getStartDate() != null) {
                        start = p.getStartDate().toLocalDate();
                        startPicker.setValue(start);
                    }
                    if (end == null && p.getEndDate() != null) {
                        end = p.getEndDate().toLocalDate();
                        endPicker.setValue(end);
                    }
                }

                if (start == null) {
                    showAlert("Data rozpoczęcia jest wymagana.");
                    return;
                }

                if (end == null) {
                    showAlert("Data zakończenia jest wymagana.");
                    return;
                }


                if (end.isBefore(start)) {
                    showAlert("Data zakończenia nie może być wcześniejsza niż data rozpoczęcia.");
                    return;
                }

                // WALIDACJA SUMY WAG
                if (msTabController != null) {
                    int totalWeight = msTabController.getMilestones()
                            .stream()
                            .mapToInt(Milestone::getWeight)
                            .sum();
                    if (totalWeight > 100) {
                        showAlert("Suma wag kamieni milowych nie może przekraczać 100 (obecnie: " + totalWeight + ")");
                        return;
                    }
                }

                int projectId;

                if (item == null) {
                    projectId = insertProjectAndReturnId();
                    msTabController.assignProjectIdToMilestones(projectId);
                } else {
                    updateData();
                    projectId = ((Project) item).getId();
                }

                // ZAPIS KAMIENI MILOWYCH
              //  Project p = (Project) item;

                // 1) USUŃ ISTNIEJĄCE
                try (Connection c = DatabaseModel.connect();
                     PreparedStatement d = c.prepareStatement("DELETE FROM milestone WHERE project_id = ?")) {
                    d.setInt(1, projectId);
                    d.executeUpdate();
                }

                // 2) WSTAW NOWE
                ObservableList<Milestone> toSave = msTabController.getMilestones();
                try (Connection c = DatabaseModel.connect();
                     PreparedStatement i = c.prepareStatement("INSERT INTO milestone (project_id, name, description, weight, team_id) VALUES (?, ?, ?, ?, ?)")) {
                    for (Milestone m : toSave) {
                        i.setInt(1, projectId);
                        i.setString(2, m.getName());
                        i.setString(3, m.getDescription());
                        i.setInt(4, m.getWeight());
                        i.setInt(5, m.getTeamId());
                        i.addBatch();
                    }
                    i.executeBatch();
                }

                // ZAMKNIJ OKNO I ODŚWIEŻ LISTĘ
                Stage stage = (Stage) titleLabel.getScene().getWindow();
                stage.close();
                if (dataChangedListener != null) {
                    dataChangedListener.onDataChanged();
                }
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Błąd bazy danych podczas zapisywania: " + e.getMessage()
            ).showAndWait();
        }
    }

    @FXML
    public void handleCancel() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }

    public void insertUser(String passwordHash) throws SQLException {
        try (Connection connection = DatabaseModel.connect()) {
            String query = "INSERT INTO \"user\" (first_name, last_name, email, role, team_id, password_hash) VALUES (?, ?, ?, CAST(? AS role_enum), ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, firstNameTextField.getText());
            preparedStatement.setString(2, lastNameTextField.getText());
            preparedStatement.setString(3, emailTextField.getText());
            preparedStatement.setString(4, roleComboBox.getValue());
            preparedStatement.setObject(5, teamIdComboBox.getValue());
            preparedStatement.setString(6, passwordHash);
            preparedStatement.executeUpdate();
        }
    }

    public void updateUserSelective(String passwordHash) throws SQLException {
        if (currentUser == null) {
            return;
        }

        StringBuilder queryBuilder = new StringBuilder("UPDATE \"user\" SET ");
        List<Object> parameters = new ArrayList<>();
        boolean first = true;

        if (!firstNameTextField.getText().equals(currentUser.getFirstName())) {
            if (!first) queryBuilder.append(", ");
            queryBuilder.append("first_name = ?");
            parameters.add(firstNameTextField.getText());
            first = false;
        }

        if (!lastNameTextField.getText().equals(currentUser.getLastName())) {
            if (!first) queryBuilder.append(", ");
            queryBuilder.append("last_name = ?");
            parameters.add(lastNameTextField.getText());
            first = false;
        }

        if (!emailTextField.getText().equals(currentUser.getEmail())) {
            if (!first) queryBuilder.append(", ");
            queryBuilder.append("email = ?");
            parameters.add(emailTextField.getText());
            first = false;
        }

        if (roleComboBox.getValue() != null && !roleComboBox.getValue().equals(currentUser.getRole())) {
            if (!first) queryBuilder.append(", ");
            queryBuilder.append("role = CAST(? AS role_enum)");
            parameters.add(roleComboBox.getValue());
            first = false;
        }

        if ((teamIdComboBox.getValue() != null && !teamIdComboBox.getValue().equals(currentUser.getTeamId())) ||
                (teamIdComboBox.getValue() == null && currentUser.getTeamId() != null)) {
            if (!first) queryBuilder.append(", ");
            queryBuilder.append("team_id = ?");
            parameters.add(teamIdComboBox.getValue());
            first = false;
        }

        if (passwordHash != null) {
            if (!first) queryBuilder.append(", ");
            queryBuilder.append("password_hash = ?");
            parameters.add(passwordHash);
            first = false;
        }

        if (first) {
            return;
        }

        queryBuilder.append(" WHERE id = ?");
        parameters.add(currentUser.getId());

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public void insertData() throws SQLException {
        try (Connection connection = DatabaseModel.connect()) {
            PreparedStatement preparedStatement = null;
            switch (sectionName) {
                case "Zespoły":
                    preparedStatement = connection.prepareStatement("INSERT INTO team (name) VALUES (?)");
                    TextField nameField = (TextField) fieldMap.get("name");
                    preparedStatement.setString(1, (nameField != null) ? nameField.getText() : null);
                    break;
                case "Projekty":
                    preparedStatement = connection.prepareStatement("INSERT INTO project (name, description, start_date, end_date, admin_id) VALUES (?, ?, ?, ?, ?)");
                    preparedStatement.setString(1, ((TextField) fieldMap.get("name")).getText());
                    preparedStatement.setString(2, ((TextArea) fieldMap.get("description")).getText());
                    LocalDate startDate = ((DatePicker) fieldMap.get("startDate")).getValue();
                    preparedStatement.setDate(3, startDate != null ? Date.valueOf(startDate) : null);
                    LocalDate endDate = ((DatePicker) fieldMap.get("endDate")).getValue();
                    preparedStatement.setDate(4, endDate != null ? Date.valueOf(endDate) : null);
                    preparedStatement.setInt(5, 1);
                    break;
            }
            if (preparedStatement != null) {
                preparedStatement.executeUpdate();
            }
        }
    }

    public void updateData() throws SQLException {
        try (Connection connection = DatabaseModel.connect()) {
            PreparedStatement preparedStatement = null;
            switch (sectionName) {
                case "Zespoły":
                    preparedStatement = connection.prepareStatement("UPDATE team SET name = ? WHERE id = ?");
                    TextField nameField = (TextField) fieldMap.get("name");
                    preparedStatement.setString(1, (nameField != null && !nameField.getText().isEmpty()) ? nameField.getText() : ((Team) item).getName());
                    preparedStatement.setInt(2, ((Team) item).getId());
                    break;
                case "Projekty":
                    preparedStatement = connection.prepareStatement("UPDATE project SET name = ?, description = ?, start_date = ?, end_date = ? WHERE id = ?");
                    TextField nameFieldProject = (TextField) fieldMap.get("name");
                    TextArea descriptionField = (TextArea) fieldMap.get("description");
                    DatePicker startDatePicker = (DatePicker) fieldMap.get("startDate");
                    DatePicker endDatePicker = (DatePicker) fieldMap.get("endDate");
                    preparedStatement.setString(1, (nameFieldProject != null && !nameFieldProject.getText().isEmpty()) ? nameFieldProject.getText() : ((Project) item).getName());
                    preparedStatement.setString(2, (descriptionField != null && !descriptionField.getText().isEmpty()) ? descriptionField.getText() : ((Project) item).getDescription());
                    preparedStatement.setDate(3, (startDatePicker != null && startDatePicker.getValue() != null) ? Date.valueOf(startDatePicker.getValue()) : ((Project) item).getStartDate());
                    preparedStatement.setDate(4, (endDatePicker != null && endDatePicker.getValue() != null) ? Date.valueOf(endDatePicker.getValue()) : ((Project) item).getEndDate());preparedStatement.setInt(5, ((Project) item).getId());
                    break;
            }
            if (preparedStatement != null) {
                preparedStatement.executeUpdate();
            }
        }
    }
    boolean performSaveLogicForTest(Map<String, Object> formData, Object currentItem) throws SQLException {
        switch (sectionName) {
            case "Użytkownicy":
                String password = (String) formData.get("password");
                String confirmPassword = (String) formData.get("confirmPassword");

                if (currentItem == null && (password == null || password.isEmpty())) {
                    throw new IllegalArgumentException("Hasło jest wymagane przy dodawaniu nowego użytkownika.");
                }

                if (password != null && !password.isEmpty() && !password.equals(confirmPassword)) {
                    throw new IllegalArgumentException("Hasła nie pasują do siebie.");
                }

                String passwordHash = null;
                if (password != null && !password.isEmpty()) {
                    passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
                } else if (currentItem instanceof User) {
                    passwordHash = ((User) currentItem).getPasswordHash();
                }

                try (Connection connection = DatabaseModel.connect()) {
                    String query;
                    PreparedStatement ps;

                    if (currentItem == null) {
                        query = "INSERT INTO \"user\" (first_name, last_name, email, role, team_id, password_hash) VALUES (?, ?, ?, CAST(? AS role_enum), ?, ?)";
                        ps = connection.prepareStatement(query);
                        ps.setString(1, (String) formData.get("firstName"));
                        ps.setString(2, (String) formData.get("lastName"));
                        ps.setString(3, (String) formData.get("email"));
                        ps.setString(4, (String) formData.get("role"));
                        ps.setObject(5, formData.get("teamId"));
                        ps.setString(6, passwordHash);
                        ps.executeUpdate();
                    } else {
                        // Minimalna wersja update — może być bardziej rozbudowana jeśli potrzebujesz
                        query = "UPDATE \"user\" SET first_name = ?, last_name = ?, email = ?, role = CAST(? AS role_enum), team_id = ?, password_hash = ? WHERE id = ?";
                        ps = connection.prepareStatement(query);
                        ps.setString(1, (String) formData.get("firstName"));
                        ps.setString(2, (String) formData.get("lastName"));
                        ps.setString(3, (String) formData.get("email"));
                        ps.setString(4, (String) formData.get("role"));
                        ps.setObject(5, formData.get("teamId"));
                        ps.setString(6, passwordHash);
                        ps.setInt(7, ((User) currentItem).getId());
                        ps.executeUpdate();
                    }
                }
                return true;

            case "Zespoły":
                try (Connection connection = DatabaseModel.connect()) {
                    String name = (String) formData.get("name");
                    if (currentItem == null) {
                        PreparedStatement ps = connection.prepareStatement("INSERT INTO team (name) VALUES (?)");
                        ps.setString(1, name);
                        ps.executeUpdate();
                    } else {
                        PreparedStatement ps = connection.prepareStatement("UPDATE team SET name = ? WHERE id = ?");
                        ps.setString(1, name);
                        ps.setInt(2, ((Team) currentItem).getId());
                        ps.executeUpdate();
                    }
                }
                return true;

            case "Projekty":
                LocalDate start = (LocalDate) formData.get("startDate");
                LocalDate end = (LocalDate) formData.get("endDate");

                if (start != null && end != null && end.isBefore(start)) {
                    throw new IllegalArgumentException("Data zakończenia nie może być wcześniejsza niż data rozpoczęcia.");
                }

                try (Connection connection = DatabaseModel.connect()) {
                    if (currentItem == null) {
                        PreparedStatement ps = connection.prepareStatement("INSERT INTO project (name, description, start_date, end_date, admin_id) VALUES (?, ?, ?, ?, ?)");
                        ps.setString(1, (String) formData.get("name"));
                        ps.setString(2, (String) formData.get("description"));
                        ps.setDate(3, start != null ? Date.valueOf(start) : null);
                        ps.setDate(4, end != null ? Date.valueOf(end) : null);
                        ps.setInt(5, 1);
                        ps.executeUpdate();
                    } else {
                        PreparedStatement ps = connection.prepareStatement("UPDATE project SET name = ?, description = ?, start_date = ?, end_date = ? WHERE id = ?");
                        ps.setString(1, (String) formData.get("name"));
                        ps.setString(2, (String) formData.get("description"));
                        ps.setDate(3, start != null ? Date.valueOf(start) : null);
                        ps.setDate(4, end != null ? Date.valueOf(end) : null);
                        ps.setInt(5, ((Project) currentItem).getId());
                        ps.executeUpdate();
                    }
                }
                return true;

            default:
                throw new IllegalStateException("Unknown section name: " + sectionName);
        }
    }
    private void validateUserInput(String firstName, String lastName, String email, String password, String confirmPassword, boolean isNewUser) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Imię jest wymagane.");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwisko jest wymagane.");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Nieprawidłowy adres e-mail.");
        }

        if (isNewUser || (password != null && !password.isEmpty())) {
            if (password.length() < 8) {
                throw new IllegalArgumentException("Hasło musi mieć co najmniej 8 znaków.");
            }
            if (!password.matches(".*[A-Z].*")) {
                throw new IllegalArgumentException("Hasło musi zawierać wielką literę.");
            }
            if (!password.matches(".*\\d.*")) {
                throw new IllegalArgumentException("Hasło musi zawierać cyfrę.");
            }
            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Hasła nie pasują do siebie.");
            }
        }
    }

    public interface DataChangedListener {
        void onDataChanged();
    }
}