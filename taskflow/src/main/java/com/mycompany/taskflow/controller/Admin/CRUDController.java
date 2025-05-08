package com.mycompany.taskflow.controller.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.Admin.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CRUDController implements CRUDAddEditController.DataChangedListener {

    @FXML
    public Label sectionTitleLabel;
    @FXML
    public ListView<Object> dataListView;
    public String sectionName;
    public ObservableList<Object> data = FXCollections.observableArrayList();
    public AdminPanelController adminPanelController;

    public void setAdminPanelController(AdminPanelController adminPanelController) {
        this.adminPanelController = adminPanelController;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
        sectionTitleLabel.setText("Zarządzanie " + sectionName);
        loadData();
    }

    @FXML
    public void initialize() {
        dataListView.setItems(data);
    }

    public List<Object> fetchData(String sectionName) throws SQLException {
        List<Object> results = new ArrayList<>();
        try (Connection conn = DatabaseModel.connect(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM user"); // zmień zgodnie z sectionName
            while (rs.next()) {
                // Tworzenie obiektu User
            }
        }
        return results;
    }
    
    public void loadData() {
        data.clear();
        try (Connection connection = DatabaseModel.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = getDataFromDatabase(statement)) {
            if (resultSet != null) {
                while (resultSet.next()) {
                    Object item = null;
                    if (sectionName.equals("Użytkownicy")) {
                        User user = new User();
                        user.setId(resultSet.getInt("id"));
                        user.setFirstName(resultSet.getString("first_name"));
                        user.setLastName(resultSet.getString("last_name"));
                        item = user;
                    } else if (sectionName.equals("Zespoły")) {
                        Team team = new Team();
                        team.setId(resultSet.getInt("id"));
                        team.setName(resultSet.getString("name"));
                        item = team;
                    } else if (sectionName.equals("Projekty")) {
                        Project project = new Project();
                        project.setId(resultSet.getInt("id"));
                        project.setName(resultSet.getString("name"));
                        project.setDescription(resultSet.getString("description"));
                        item = project;
                    }
                    if (item != null) {
                        data.add(item);
                    }
                }
                dataListView.setCellFactory(param -> new ListCell<Object>() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else if (item instanceof User) {
                            setText(((User) item).getFirstName() + " " + ((User) item).getLastName());
                        } else if (item instanceof Team) {
                            setText(((Team) item).getName());
                        } else if (item instanceof Project) {
                            setText(((Project) item).getName());
                        }
                    }
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas ładowania danych.");
            alert.showAndWait();
        }
    }

    public ResultSet getDataFromDatabase(Statement statement) throws SQLException {
        ResultSet resultSet = null;
        if (sectionName.equals("Użytkownicy")) {
            resultSet = statement.executeQuery("SELECT id, first_name, last_name FROM \"user\"");
        } else if (sectionName.equals("Zespoły")) {
            resultSet = statement.executeQuery("SELECT id, name FROM team");
        } else if (sectionName.equals("Projekty")) {
            resultSet = statement.executeQuery("SELECT id, name, description FROM project");
        }
        return resultSet;
    }

    @FXML
    public void addItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/Admin/CRUDAddEditView.fxml"));
            Parent root = loader.load();
            CRUDAddEditController addEditController = loader.getController();
            addEditController.setSectionName(sectionName);
            addEditController.setOnDataChangedListener(this);

            Stage stage = new Stage();
            stage.setTitle("Dodaj " + sectionName);
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas ładowania widoku dodawania.");
            alert.showAndWait();
        }
    }

    @FXML
    public void editItem() {
        Object selectedItem = dataListView.getSelectionModel().getSelectedItem();
        if (selectedItem instanceof Team) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/Admin/TeamEditView.fxml"));
                Parent root = loader.load();
                TeamEditController teamEditController = loader.getController();
                teamEditController.setCRUDController(this);
                teamEditController.setTeam((Team) selectedItem);

                Stage stage = new Stage();
                stage.setTitle("Edytuj Zespół");
                stage.setScene(new Scene(root, 600, 400));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas ładowania widoku edycji zespołu.");
                alert.showAndWait();
            }
        } else if (selectedItem != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/Admin/CRUDAddEditView.fxml"));
                Parent root = loader.load();
                CRUDAddEditController addEditController = loader.getController();
                addEditController.setSectionName(sectionName);
                addEditController.setItem(selectedItem); // Pass the selected item for editing
                addEditController.setOnDataChangedListener(this);

                Stage stage = new Stage();
                stage.setTitle("Edytuj " + sectionName);
                stage.setScene(new Scene(root, 600, 400));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas ładowania widoku edycji.");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Proszę wybrać element do edycji.");
            alert.showAndWait();
        }
    }

    @FXML
    public void deleteItem() {
        Object selectedItem = dataListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try (Connection connection = DatabaseModel.connect();
                 PreparedStatement preparedStatement = createPreparedStatementForDelete(connection, selectedItem)) {
                if (preparedStatement != null) {
                    preparedStatement.executeUpdate();
                    loadData();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Błąd podczas usuwania z bazy danych.");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Proszę wybrać element do usunięcia.");
            alert.showAndWait();
        }
    }

    public PreparedStatement createPreparedStatementForDelete(Connection connection, Object item) throws SQLException {
        PreparedStatement preparedStatement = null;
        if (item instanceof User) {
            preparedStatement = connection.prepareStatement("DELETE FROM \"user\" WHERE id = ?");
            preparedStatement.setInt(1, ((User) item).getId());
        } else if (item instanceof Team) {
            preparedStatement = connection.prepareStatement("DELETE FROM team WHERE id = ?");
            preparedStatement.setInt(1, ((Team) item).getId());
        } else if (item instanceof Project) {
            preparedStatement = connection.prepareStatement("DELETE FROM project WHERE id = ?");
            preparedStatement.setInt(1, ((Project) item).getId());
        }
        return preparedStatement;
    }

    @FXML
    public void goBack() {
        Stage stage = (Stage) sectionTitleLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void saveChanges() {
        loadData();
    }

    @Override
    public void onDataChanged() {
        loadData();
        if (adminPanelController != null) {
            adminPanelController.refreshData();
        }
    }
}