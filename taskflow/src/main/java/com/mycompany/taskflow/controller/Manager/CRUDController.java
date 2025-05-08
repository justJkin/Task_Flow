package com.mycompany.taskflow.controller.Manager;

import com.mycompany.taskflow.model.Manager.*; // Import modeli
import com.mycompany.taskflow.model.DatabaseModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CRUDController {

    @FXML
    public Label sectionTitleLabel;
    @FXML
    public ListView<String> dataListView; // Zmiana: ListView dla Object
    @FXML
    public Button editItemButton;
    @FXML
    public Button deleteItemButton;
    @FXML
    public Button goBackButton;
    @FXML
    public Button addItemButton;
    @FXML
    public Button saveChangesButton;

    public String currentObjectType; // Dodano pole do przechowywania typu obiektu

    public void initialize() {
        // Set the title of the section (will be determined by which CRUD view is loaded)
        // Przykład użycia:
        // sectionTitleLabel.setText("Zarządzanie Użytkownikami");
        // currentObjectType = "user";
    }

    public void setObjectType(String objectType) { // Metoda do ustawiania typu obiektu
        this.currentObjectType = objectType;
        sectionTitleLabel.setText("Zarządzanie " + getObjectDisplayName(objectType));
        loadData();
    }

    public String getObjectDisplayName(String objectType) {
        switch (objectType) {
            case "user":
                return "Użytkownikami";
            case "team":
                return "Zespołami";
            case "project":
                return "Projektami";
            case "milestone":
                return "Kamieniami Milowymi";
            case "task":
                return "Zadaniami";
            case "subtask":
                return "Cząstkami";
            default:
                return "[Obiekt]";
        }
    }

    public void loadData() {
        dataListView.getItems().clear(); // Czyszczenie listy
        String sql = "";
        ResultSet rs = null;
        try (Connection conn = DatabaseModel.connect();
             Statement stmt = conn.createStatement();
             ) {
            switch (currentObjectType) {
                case "user":
                    sql = "SELECT first_name, last_name FROM \"user\"";
                    rs = stmt.executeQuery(sql);
                    while (rs.next()) {
                        dataListView.getItems().add(rs.getString("first_name") + " " + rs.getString("last_name"));
                    }
                    break;
                case "team":
                    sql = "SELECT name FROM team";
                    rs = stmt.executeQuery(sql);
                    while (rs.next()) {
                        dataListView.getItems().add(rs.getString("name"));
                    }
                    break;
                // ... obsługa innych typów obiektów
                default:
                    dataListView.getItems().addAll("Element 1", "Element 2", "Element 3"); // Domyślne
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error
        }
    }

    @FXML
    public void editItem(ActionEvent event) {
        String selectedItem = dataListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            System.out.println("Editing: " + selectedItem);
            // Implement logic to open an edit form or dialog
            // W zależności od typu obiektu, wyświetl odpowiedni formularz edycji
        }
    }

    @FXML
    public void deleteItem(ActionEvent event) {
        int selectedIndex = dataListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            dataListView.getItems().remove(selectedIndex);
            // Implement logic to delete the item from the database
            // Usuń element z bazy danych na podstawie typu obiektu i wybranego elementu
        }
    }

    @FXML
    public void goBack(ActionEvent event) {
        // Implement logic to navigate back to the previous view
        System.out.println("Going back...");
    }

    @FXML
    public void addItem(ActionEvent event) {
        System.out.println("Adding new item...");
        // Implement logic to open an add new item form or dialog
        // W zależności od typu obiektu, wyświetl odpowiedni formularz dodawania
    }

    @FXML
    public void saveChanges(ActionEvent event) {
        System.out.println("Saving changes...");
        // Implement logic to save changes to the database
        // Zapisz zmiany w bazie danych na podstawie typu obiektu i zmodyfikowanych danych
    }
}