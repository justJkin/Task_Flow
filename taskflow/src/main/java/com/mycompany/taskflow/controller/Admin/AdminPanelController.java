// AdminPanelController.java
package com.mycompany.taskflow.controller.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.Admin.User;
import com.mycompany.taskflow.model.Admin.Team;
import com.mycompany.taskflow.model.Admin.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdminPanelController {

    @FXML
    ListView<User> userListView;
    @FXML
    ListView<Team> teamListView;
    @FXML
    ListView<Project> projectListView;

    private ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<Team> teams = FXCollections.observableArrayList();
    private ObservableList<Project> projects = FXCollections.observableArrayList();

    private CRUDController userCRUDController;
    private CRUDController teamCRUDController;
    private CRUDController projectCRUDController;

    @FXML
    public void initialize() {
        loadUserData();
        loadTeamData();
        loadProjectData();

        userListView.setItems(users);
        teamListView.setItems(teams);
        projectListView.setItems(projects);

        // Set cell factories to display names
        userListView.setCellFactory(param -> new javafx.scene.control.ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFirstName() + " " + item.getLastName());
                }
            }
        });

        teamListView.setCellFactory(param -> new javafx.scene.control.ListCell<Team>() {
            @Override
            protected void updateItem(Team item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        projectListView.setCellFactory(param -> new javafx.scene.control.ListCell<Project>() {
            @Override
            protected void updateItem(Project item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }

    @FXML
    public void openUserCRUDView() {
        loadCRUDView("Użytkownicy");
    }

    @FXML
    public void openTeamCRUDView() {
        loadCRUDView("Zespoły");
    }

    @FXML
    public void openProjectCRUDView() {
        loadCRUDView("Projekty");
    }

    private void loadCRUDView(String sectionName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/Admin/CRUDView.fxml"));
            Parent root = loader.load();
            CRUDController crudController = loader.getController();
            crudController.setSectionName(sectionName);
            crudController.setAdminPanelController(this); // Pass the reference

            Stage stage = new Stage();
            stage.setTitle("CRUD - " + sectionName);
            stage.setScene(new Scene(root, 600, 400));
            stage.show();

            // Keep a reference to the controller if needed
            if (sectionName.equals("Użytkownicy")) {
                this.userCRUDController = crudController;
            } else if (sectionName.equals("Zespoły")) {
                this.teamCRUDController = crudController;
            } else if (sectionName.equals("Projekty")) {
                this.projectCRUDController = crudController;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas ładowania widoku CRUD.");
            alert.showAndWait();
        }
    }

    public void refreshData() {
        loadUserData();
        loadTeamData();
        loadProjectData();
    }

    void loadUserData() {
        users.clear();
        try (Connection connection = DatabaseModel.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, first_name, last_name, email, role, team_id FROM \"user\"")) {
            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setFirstName(resultSet.getString("first_name"));
                user.setLastName(resultSet.getString("last_name"));
                user.setEmail(resultSet.getString("email"));
                user.setRole(resultSet.getString("role"));
                user.setTeamId(resultSet.getInt("team_id"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas ładowania danych użytkowników.");
            alert.showAndWait();
        }
    }

    void loadTeamData() {
        teams.clear();
        try (Connection connection = DatabaseModel.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, name FROM team")) {
            while (resultSet.next()) {
                Team team = new Team();
                team.setId(resultSet.getInt("id"));
                team.setName(resultSet.getString("name"));
                teams.add(team);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas ładowania danych zespołów.");
            alert.showAndWait();
        }
    }

    void loadProjectData() {
        projects.clear();
        try (Connection connection = DatabaseModel.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, name, description, start_date, end_date FROM project")) {
            while (resultSet.next()) {
                Project project = new Project();
                project.setId(resultSet.getInt("id"));
                project.setName(resultSet.getString("name"));
                project.setDescription(resultSet.getString("description"));
                project.setStartDate(resultSet.getDate("start_date"));
                project.setEndDate(resultSet.getDate("end_date"));
                projects.add(project);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas ładowania danych projektów.");
            alert.showAndWait();
        }
    }
}