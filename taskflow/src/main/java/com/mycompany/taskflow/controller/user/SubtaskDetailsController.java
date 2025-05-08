package com.mycompany.taskflow.controller.user;

import com.mycompany.taskflow.model.user.Subtask;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SubtaskDetailsController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label taskNameLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label weightLabel;
    @FXML
    private Label deadlineLabel;

    public TaskController taskController;
    public Subtask currentSubtask;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void setSubtaskDetails(String taskName, Subtask subtaskToShow, TaskController taskController) {
        taskNameLabel.setText(taskName);
        descriptionLabel.setText(subtaskToShow.getDescription());
        weightLabel.setText(String.valueOf(subtaskToShow.getWeight()));
        if (subtaskToShow.getDueDate() != null) {
            LocalDate localDate = subtaskToShow.getDueDate().toLocalDate();
            deadlineLabel.setText(localDate.format(dateFormatter));
        } else {
            deadlineLabel.setText("Brak terminu");
        }
        this.taskController = taskController;
        this.currentSubtask = subtaskToShow;
        descriptionLabel.setWrapText(true);
    }

    @FXML
    public void deleteSubtask() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie usunięcia");
        alert.setHeaderText("Czy na pewno chcesz usunąć tę cząstkę?");
        alert.setContentText(currentSubtask.getDescription() + " (Waga: " + currentSubtask.getWeight() + ")");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (taskController != null) {
                taskController.deleteSubtask(currentSubtask);
            }
            closeWindow();
        }
    }

    @FXML
    public void closeWindow() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
    public void setTaskController(TaskController taskController) {
        this.taskController = taskController;
    }
}