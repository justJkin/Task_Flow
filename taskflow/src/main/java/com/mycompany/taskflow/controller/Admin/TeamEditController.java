package com.mycompany.taskflow.controller.Admin;

import com.mycompany.taskflow.model.Admin.Team;
import com.mycompany.taskflow.model.Admin.User;
import com.mycompany.taskflow.model.DatabaseModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TeamEditController {

    public ListView<Object> allUsersListView;
    @FXML
    private Label titleLabel;
    @FXML
    TextField nameTextField;
    @FXML
    ListView<User> usersInTeamListView;
    @FXML
    ListView<User> otherUsersListView;
    @FXML
    Button saveButton;
    @FXML
    private Button cancelButton;

    private Team currentTeam;
    ObservableList<User> usersInTeam = FXCollections.observableArrayList();
    private ObservableList<User> otherUsers = FXCollections.observableArrayList();
    private CRUDController crudController;
    private Set<Integer> initialUsersInTeamIds = new HashSet<>();
    Map<Integer, String> teamNameCache = new HashMap<>(); // Cache dla nazw zespołów

    public void setCRUDController(CRUDController crudController) {
        this.crudController = crudController;
    }

    public void setTeam(Team team) {
        this.currentTeam = team;
        nameTextField.setText(team.getName());
        loadTeamsCache(); // Załaduj cache zespołów przy otwarciu okna
        loadUsers();
        setupDragAndDrop();
    }

    @FXML
    public void initialize() {
        usersInTeamListView.setItems(usersInTeam);
        otherUsersListView.setItems(otherUsers);

        // Set cell factory for displaying user names with team
        usersInTeamListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(""); // Resetuj style
                } else {
                    HBox hbox = new HBox();
                    Label nameLabel = new Label(user.getFirstName() + " " + user.getLastName());
                    Label teamLabel = new Label();

                    if (user.getTeamId() != null && teamNameCache.containsKey(user.getTeamId())) {
                        teamLabel.setText("(" + teamNameCache.get(user.getTeamId()) + ")");
                        teamLabel.setFont(Font.font("System", FontWeight.LIGHT, 10));
                        teamLabel.setTextFill(Color.GRAY);
                        HBox.setMargin(teamLabel, new Insets(0, 0, 0, 5));
                    }
                    hbox.getChildren().addAll(nameLabel, teamLabel);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(hbox);

                    // Podświetlanie managerów
                    if (user.getRole().equalsIgnoreCase("manager")) {
                        if (user.getId() % 2 == 0) { // Prosty sposób na różne kolory
                            setStyle("-fx-background-color: lightblue;");
                        } else {
                            setStyle("-fx-background-color: lightgreen;");
                        }
                    } else {
                        setStyle(""); // Resetuj tło dla nie-managerów
                    }
                }
            }
        });

        otherUsersListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(""); // Resetuj style
                } else {
                    HBox hbox = new HBox();
                    Label nameLabel = new Label(user.getFirstName() + " " + user.getLastName());
                    Label teamLabel = new Label();

                    if (user.getTeamId() != null && teamNameCache.containsKey(user.getTeamId())) {
                        teamLabel.setText("(" + teamNameCache.get(user.getTeamId()) + ")");
                        teamLabel.setFont(Font.font("System", FontWeight.LIGHT, 10));
                        teamLabel.setTextFill(Color.GRAY);
                        HBox.setMargin(teamLabel, new Insets(0, 0, 0, 5));
                    }
                    hbox.getChildren().addAll(nameLabel, teamLabel);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(hbox);

                    // Podświetlanie managerów
                    if (user.getRole().equalsIgnoreCase("manager")) {
                        if (user.getId() % 2 == 0) { // Prosty sposób na różne kolory
                            setStyle("-fx-background-color: lightblue;");
                        } else {
                            setStyle("-fx-background-color: lightgreen;");
                        }
                    } else {
                        setStyle(""); // Resetuj tło dla nie-managerów
                    }
                }
            }
        });
    }

    void loadTeamsCache() {
        teamNameCache.clear();
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement stmt = connection.prepareStatement("SELECT id, name FROM team")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                teamNameCache.put(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd podczas ładowania informacji o zespołach.");
        }
    }


    private void loadUsers() {
        usersInTeam.clear();
        otherUsers.clear();
        initialUsersInTeamIds.clear();
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement usersInTeamStmt = connection.prepareStatement("SELECT * FROM \"user\" WHERE team_id = ?");
             PreparedStatement allUsersStmt = connection.prepareStatement("SELECT * FROM \"user\"")) {

            usersInTeamStmt.setInt(1, currentTeam.getId());
            ResultSet inTeamRs = usersInTeamStmt.executeQuery();
            while (inTeamRs.next()) {
                User user = mapResultSetToUser(inTeamRs);
                usersInTeam.add(user);
                initialUsersInTeamIds.add(user.getId());
            }

            ResultSet allUsersRs = allUsersStmt.executeQuery();
            while (allUsersRs.next()) {
                User user = mapResultSetToUser(allUsersRs);
                if (!initialUsersInTeamIds.contains(user.getId()) && !user.getRole().equalsIgnoreCase("admin")) { // Dodaj tylko nie-adminów, którzy nie są jeszcze w tym zespole
                    otherUsers.add(user);
                }
            }

            otherUsers.removeIf(user -> usersInTeam.stream().anyMatch(u -> u.getId() == user.getId()));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd podczas ładowania użytkowników.");
        }
    }

    User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setTeamId(rs.getObject("team_id") != null ? rs.getInt("team_id") : null);
        user.setPasswordHash(rs.getString("password_hash"));
        return user;
    }

    private void setupDragAndDrop() {
        // Drag from usersInTeam to otherUsers
        usersInTeamListView.setOnDragDetected(event -> {
            User selectedUser = usersInTeamListView.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                Dragboard db = usersInTeamListView.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(selectedUser.getId()));
                db.setContent(content);
                event.consume();
            }
        });
        otherUsersListView.setOnDragOver(event -> {
            if (event.getDragboard().hasString() && event.getGestureSource() == usersInTeamListView) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        usersInTeamListView.setOnDragDropped(event -> {
            if (event.getDragboard().hasString() && event.getGestureSource() == otherUsersListView) {
                try {
                    int userId = Integer.parseInt(event.getDragboard().getString());
                    User userToMove = otherUsers.stream().filter(u -> u.getId() == userId).findFirst().orElse(null);
                    if (userToMove != null) {
                        if (userToMove.getRole().equalsIgnoreCase("manager")) {
                            // Sprawdź, czy w zespole nie ma już managera
                            boolean hasManager = usersInTeam.stream().anyMatch(user -> user.getRole().equalsIgnoreCase("manager"));
                            if (hasManager) {
                                showAlert(Alert.AlertType.WARNING, "W zespole może być tylko jeden manager.");
                                event.setDropCompleted(false); // Nie pozwól na upuszczenie
                                event.consume();
                                return;
                            }
                        }
                        otherUsers.remove(userToMove);
                        usersInTeam.add(userToMove);
                        event.setDropCompleted(true);
                        event.consume();
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Błąd: Nieprawidłowy format ID użytkownika podczas przeciągania.");
                }
            }
        });

        // Drag from otherUsers to usersInTeam
        otherUsersListView.setOnDragDetected(event -> {
            User selectedUser = otherUsersListView.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                Dragboard db = otherUsersListView.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(selectedUser.getId()));
                db.setContent(content);
                event.consume();
            }
        });
        usersInTeamListView.setOnDragOver(event -> {
            if (event.getDragboard().hasString() && event.getGestureSource() == otherUsersListView) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        usersInTeamListView.setOnDragDropped(event -> {
            if (event.getDragboard().hasString() && event.getGestureSource() == otherUsersListView) {
                try {
                    int userId = Integer.parseInt(event.getDragboard().getString());
                    User userToMove = otherUsers.stream().filter(u -> u.getId() == userId).findFirst().orElse(null);
                    if (userToMove != null) {
                        otherUsers.remove(userToMove);
                        usersInTeam.add(userToMove);
                    }
                    event.setDropCompleted(true);
                    event.consume();
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Błąd: Nieprawidłowy format ID użytkownika podczas przeciągania.");
                }
            }
        });
    }

    @FXML
    void handleSave() {
        String newTeamName = nameTextField.getText();
        if (newTeamName != null && !newTeamName.trim().isEmpty() && !newTeamName.equals(currentTeam.getName())) {
            try (Connection connection = DatabaseModel.connect();
                 PreparedStatement updateTeamStmt = connection.prepareStatement("UPDATE team SET name = ? WHERE id = ?")) {
                updateTeamStmt.setString(1, newTeamName);
                updateTeamStmt.setInt(2, currentTeam.getId());
                updateTeamStmt.executeUpdate();
                currentTeam.setName(newTeamName);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Błąd podczas aktualizacji nazwy zespołu.");
                return;
            }
        }

        long managerCount = usersInTeam.stream().filter(user -> user.getRole().equalsIgnoreCase("manager")).count();
        if (managerCount > 1) {
            showAlert(Alert.AlertType.ERROR, "W zespole może być tylko jeden manager. Popraw przynależność użytkowników.");
            return; // Nie zapisuj, jeśli jest więcej niż jeden manager
        }

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement updateUserTeamStmt = connection.prepareStatement("UPDATE \"user\" SET team_id = ? WHERE id = ?")) {
            // Aktualizuj team_id dla użytkowników, którzy są TERAZ w zespole
            for (User user : usersInTeam) {
                updateUserTeamStmt.setInt(1, currentTeam.getId());
                updateUserTeamStmt.setInt(2, user.getId());
                updateUserTeamStmt.addBatch();
            }

            // Aktualizuj team_id na NULL tylko dla użytkowników, którzy BYLI w zespole, a TERAZ ich tam nie ma
            Set<Integer> currentUsersInTeamIds = usersInTeam.stream().map(User::getId).collect(Collectors.toSet());
            for (Integer initialUserId : initialUsersInTeamIds) {
                if (!currentUsersInTeamIds.contains(initialUserId)) {
                    updateUserTeamStmt.setNull(1, java.sql.Types.INTEGER);
                    updateUserTeamStmt.setInt(2, initialUserId);
                    updateUserTeamStmt.addBatch();
                }
            }

            Set<Integer> initialOtherUsersIds = otherUsers.stream().map(User::getId).collect(Collectors.toSet());
            for (User currentUserInTeam : usersInTeam) {
                if (!initialUsersInTeamIds.contains(currentUserInTeam.getId()) && !initialOtherUsersIds.contains(currentUserInTeam.getId())) {
                    updateUserTeamStmt.setInt(1, currentTeam.getId());
                    updateUserTeamStmt.setInt(2, currentUserInTeam.getId());
                    updateUserTeamStmt.addBatch();
                }
            }

            updateUserTeamStmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd podczas aktualizacji przynależności użytkowników do zespołu.");
            return;
        }

        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
        if (crudController != null) {
            crudController.onDataChanged();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }
}