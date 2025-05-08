package com.mycompany.taskflow.controller.user;

import com.mycompany.taskflow.model.user.Subtask;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AddSubtaskController {

    @FXML
    public Label titleLabel;
    @FXML
    public TextField nameTextField;
    @FXML
    public TextArea descriptionTextArea;
    public TaskController taskController;
    @FXML
    private Label weightLabel;
    @FXML
    public TextField weightTextField;
    @FXML
    private Label priorityLabel;
    @FXML
    public ComboBox<Short> priorityComboBox;
    @FXML
    public DatePicker dueDatePicker;

    public SubtaskController subtaskController;
    public int editIndex = -1;
    public int originalWeight = 0;

    @FXML
    public void initialize() {
        priorityComboBox.setItems(FXCollections.observableArrayList((short) 1, (short) 2, (short) 3, (short) 4, (short) 5));
        priorityComboBox.setValue((short) 1);

        if (editIndex != -1) {
            titleLabel.setText("Edytuj Cząstkę Zadania");
            if (subtaskController != null && editIndex >= 0 && editIndex < subtaskController.getSubtasks().size()) {
                Subtask subtaskToEdit = subtaskController.getSubtasks().get(editIndex);
                nameTextField.setText(subtaskToEdit.getName());
                descriptionTextArea.setText(subtaskToEdit.getDescription());
                weightTextField.setText(String.valueOf(subtaskToEdit.getWeight()));
                priorityComboBox.setValue(subtaskToEdit.getPriority());
                originalWeight = subtaskToEdit.getWeight();
            }
        } else {
            nameTextField.setText("");
        }
    }

    public void setSubtaskController(SubtaskController subtaskController) {
        this.subtaskController = subtaskController;
    }
    public void setInitialData(String item) {
        if (item != null && !item.isEmpty()) {
            int weightStartIndex = item.lastIndexOf(" (");
            if (weightStartIndex > 0 && item.endsWith(")")) {
                String description = item.substring(0, weightStartIndex);
                String weightStr = item.substring(weightStartIndex + 2, item.length() - 1);
                try {
                    Integer.parseInt(weightStr);
                    descriptionTextArea.setText(description);
                    weightTextField.setText(weightStr);
                } catch (NumberFormatException e) {
                    // Jeśli format wagi jest nieprawidłowy, ustawiamy cały 'item' jako opis
                    descriptionTextArea.setText(item);
                    weightTextField.setText("");
                }
            } else {
                // Jeśli waga nie jest obecna lub format jest nieprawidłowy, ustawiamy cały 'item' jako opis i czyscimy wagę
                descriptionTextArea.setText(item);
                weightTextField.setText("");
            }
        } else {
            descriptionTextArea.setText("");
            weightTextField.setText("");
        }
    }

    public void setEditIndex(int index) {
        this.editIndex = index;
        initialize();
    }

    @FXML
    public void saveSubtask() {
        String name = nameTextField.getText();
        String description = descriptionTextArea.getText();
        String weightStr = weightTextField.getText();
        Short priority = priorityComboBox.getValue();
        LocalDate dueDate = dueDatePicker.getValue();

        if (name == null || name.isEmpty()) {
            showAlert("Błąd", "Wypełnij pole nazwy.");
            return;
        }

        if (description == null || description.isEmpty()) {
            showAlert("Błąd", "Wypełnij pole opisu.");
            return;
        }

        if (weightStr == null || weightStr.isEmpty()) {
            showAlert("Błąd", "Wypełnij pole wagi.");
            return;
        }

        try {
            int weight = Integer.parseInt(weightStr);
            if (weight <= 0) {
                showAlert("Błąd", "Waga musi być liczbą nieujemną.");
                return;
            }

            if (subtaskController != null && subtaskController.getTaskController() != null && subtaskController.getTaskController().currentTask != null) {
                int taskWeight = subtaskController.getTaskController().currentTask.getWeight();
                int currentTotalWeight = subtaskController.calculateCurrentTotalWeight();
                int newTotalWeight = currentTotalWeight - originalWeight + weight;

                if (newTotalWeight > taskWeight) {
                    showAlert("Błąd", "Suma wag cząstek (" + newTotalWeight + ") przekroczy wagę zadania (" + taskWeight + ").");
                    return;
                }
            }

            if (subtaskController != null) {
                subtaskController.addSubtask(name, description, weightStr, editIndex, priority, dueDate);
            }
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Błąd", "Waga musi być liczbą.");
        }
    }
    @FXML
    public void cancel() {
        closeWindow();
    }

    public void closeWindow() {
        Stage stage = (Stage) descriptionTextArea.getScene().getWindow();
        stage.close();
    }

    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public void resetEditIndex() {
        this.editIndex = -1;
    }


}