package com.mycompany.taskflow.controller.user;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.user.Subtask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SubtaskController {

    @FXML
    private Label taskNameLabel;
    @FXML
    private ListView<String> subtasksListView;

    private String taskName;
    private ObservableList<Subtask> subtasks = FXCollections.observableArrayList();
    private TaskController taskController;

    @FXML
    public void initialize() {
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
        taskNameLabel.setText("Edycja Zadań: " + taskName);
        updateSubtaskListDisplay();
    }

    public void setTaskController(TaskController taskController) {
        this.taskController = taskController;
        if (taskController != null && taskController.currentTask != null) {
            int taskId = taskController.currentTask.getId();
            ObservableList<Subtask> initialSubtasks = Subtask.getSubtasksByTaskId(taskId);
            setSubtasks(initialSubtasks);
        }
    }

    public TaskController getTaskController() {
        return taskController;
    }

    @FXML
    public void addItem() {
        showSubtaskDialog(null, -1);
    }

    @FXML
    public void editItem() {
        String selectedItemString = subtasksListView.getSelectionModel().getSelectedItem();
        if (selectedItemString != null) {
            int index = subtasksListView.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < subtasks.size()) {
                Subtask selectedSubtask = subtasks.get(index);
                showSubtaskDialog(selectedSubtask.getDescription() + " (Waga: " + String.format("%d", selectedSubtask.getWeight()) + ")", index);
            }
        }
    }

    private void showSubtaskDialog(String item, int index) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/User/AddSubtaskView.fxml"));
            Parent root = loader.load();

            AddSubtaskController addSubtaskController = loader.getController();
            addSubtaskController.setSubtaskController(this);
            addSubtaskController.setInitialData(item);
            addSubtaskController.setEditIndex(index);

            Stage stage = new Stage();
            stage.setTitle(item == null ? "Dodaj Cząstkę Zadania" : "Edytuj Cząstkę Zadania");
            stage.setScene(new Scene(root, 400, 300));
            stage.setOnHidden(e -> {
                subtasksListView.getSelectionModel().clearSelection();
                addSubtaskController.resetEditIndex();
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSubtask(String name, String description, String weightStr, int index, short priority, LocalDate dueDate) {
        System.out.println("SubtaskController - addSubtask() - index: " + index);
        System.out.println("  Name: " + name + ", Description: " + description + ", Weight: " + weightStr + ", Priority: " + priority + ", DueDate: " + dueDate);
        System.out.println("  subtasks.size(): " + subtasks.size());
        if (index >= 0 && index < subtasks.size()) {
            System.out.println("  -> Editing subtask at index: " + index);
            Subtask existingSubtask = subtasks.get(index);
            System.out.println("     Existing: " + existingSubtask.getDescription() + " (Waga: " + existingSubtask.getWeight() + ")");
            System.out.println("     New: " + description + " (Waga: " + weightStr + ")");
            existingSubtask.setName(name);
            existingSubtask.setDescription(description);
            existingSubtask.setWeight(Integer.parseInt(weightStr));
            existingSubtask.setPriority(priority);
            existingSubtask.setDueDate(dueDate != null ? Date.valueOf(dueDate) : null);
            updateSubtaskInDatabase(existingSubtask);
            subtasks.set(index, existingSubtask);
        } else {
            System.out.println("  -> Adding new subtask");
            try {
                int weight = Integer.parseInt(weightStr);
                Subtask newSubtask = new Subtask();
                newSubtask.setName(name);
                newSubtask.setDescription(description);
                newSubtask.setWeight(weight);
                newSubtask.setPriority(priority);
                newSubtask.setDueDate(dueDate != null ? Date.valueOf(dueDate) : null);
                if (taskController != null && taskController.currentTask != null) {
                    newSubtask.setTaskId(taskController.currentTask.getId());
                    saveNewSubtaskToDatabase(newSubtask);
                    subtasks.add(newSubtask);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        updateSubtaskListDisplay();
    }

    private void saveNewSubtaskToDatabase(Subtask subtask) {
        String insertQuery = "INSERT INTO subtask (task_id, name, description, weight, is_done, priority, due_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseModel.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setInt(1, subtask.getTaskId());
            pstmt.setString(2, subtask.getName());
            pstmt.setString(3, subtask.getDescription());
            pstmt.setInt(4, subtask.getWeight());
            pstmt.setBoolean(5, subtask.isDone());
            pstmt.setShort(6, subtask.getPriority());
            pstmt.setDate(7, subtask.getDueDate() != null ? Date.valueOf(subtask.getDueDate().toLocalDate()) : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSubtaskInDatabase(Subtask subtask) {
        String updateQuery = "UPDATE subtask SET description = ?, weight = ?, is_done = ?, priority = ?, due_date = ? WHERE id = ?";
        try (Connection conn = DatabaseModel.connect();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setString(1, subtask.getDescription());
            pstmt.setInt(2, subtask.getWeight());
            pstmt.setBoolean(3, subtask.isDone());
            pstmt.setShort(4, subtask.getPriority());
            pstmt.setDate(5, subtask.getDueDate() != null ? Date.valueOf(subtask.getDueDate().toLocalDate()) : null);
            pstmt.setInt(6, subtask.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void deleteItem() {
        int selectedIndex = subtasksListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < subtasks.size()) {
            Subtask subtaskToDelete = subtasks.get(selectedIndex);
            deleteSubtaskFromDatabase(subtaskToDelete.getId());
            subtasks.remove(selectedIndex);
            updateSubtaskListDisplay();
        }
    }

    private void deleteSubtaskFromDatabase(int subtaskId) {
        String deleteQuery = "DELETE FROM subtask WHERE id = ?";
        try (Connection conn = DatabaseModel.connect();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, subtaskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack() {
        Stage stage = (Stage) taskNameLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void saveSubtasks() {
        if (taskController != null) {
            taskController.setSubtasks(FXCollections.observableArrayList(subtasks));
        }
        goBack();
    }

    public ObservableList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ObservableList<Subtask> newSubtasks) {
        System.out.println("SubtaskController - setSubtasks() - Clearing existing subtasks. Current size: " + this.subtasks.size());
        this.subtasks.clear();
        System.out.println("SubtaskController - setSubtasks() - Adding new subtasks. Size to add: " + newSubtasks.size());
        this.subtasks.addAll(newSubtasks);
        updateSubtaskListDisplay();
        System.out.println("SubtaskController - setSubtasks() - New size of subtasks: " + this.subtasks.size());
    }

    private void updateSubtaskListDisplay() {
        ObservableList<String> displayList = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Subtask sub : subtasks) {
            String createdAtFormatted = "";
            Timestamp createdAt = sub.getCreatedAt();
            if (createdAt != null) {
                createdAtFormatted = createdAt.toLocalDateTime().format(formatter);
            }
            String dueDateFormatted = sub.getDueDate() != null ? sub.getDueDate().toString() : "[brak terminu]";
            displayList.add(sub.getDescription() + " (Waga: " + String.format("%d", sub.getWeight()) + ") Utworzono: " + createdAtFormatted + ", Termin: " + dueDateFormatted);
        }
        subtasksListView.setItems(displayList);
    }

    public int calculateCurrentTotalWeight() {
        int totalWeight = 0;
        for (Subtask sub : subtasks) {
            totalWeight += sub.getWeight();
        }
        return totalWeight;
    }

    public void setTaskId(int id) {
    }
}