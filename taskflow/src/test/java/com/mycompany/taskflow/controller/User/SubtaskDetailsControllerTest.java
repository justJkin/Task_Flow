package com.mycompany.taskflow.controller.User;

import com.mycompany.taskflow.controller.JavaFXInitializer;
import com.mycompany.taskflow.controller.user.SubtaskDetailsController;
import com.mycompany.taskflow.controller.user.TaskController;
import com.mycompany.taskflow.model.user.Subtask;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class SubtaskDetailsControllerTest extends JavaFXInitializer {

    @InjectMocks
    private SubtaskDetailsController controller;

    @Mock
    private AnchorPane rootPane;
    @Mock
    private Label taskNameLabel;
    @Mock
    private Label descriptionLabel;
    @Mock
    private Label weightLabel;
    @Mock
    private Label deadlineLabel;
    @Mock
    private TaskController taskController;
    @Mock
    private Subtask currentSubtask;
    @Mock
    private Stage stage;
    @Mock
    private Scene scene;
    @Mock
    private Alert alert;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(rootPane.getScene()).thenReturn(scene);
        when(scene.getWindow()).thenReturn(stage);
    }

    @Test
    void setSubtaskDetails_shouldSetLabelsWithSubtaskDataAndFormatDate() {
        String taskName = "Test Task";
        LocalDate dueDate = LocalDate.now();
        Subtask subtask = new Subtask();
        subtask.setDescription("Test Description");
        subtask.setWeight(100);
        subtask.setDueDate(Date.valueOf(dueDate));

        controller.setSubtaskDetails(taskName, subtask, taskController);

        verify(taskNameLabel).setText(taskName);
        verify(descriptionLabel).setText("Test Description");
        verify(weightLabel).setText("100");
        verify(deadlineLabel).setText(dueDate.format(dateFormatter));
        assertEquals(subtask, controller.currentSubtask);
        assertEquals(taskController, controller.taskController);
        verify(descriptionLabel).setWrapText(true);
    }

    @Test
    void setSubtaskDetails_shouldSetDeadlineLabelToBrakTerminuIfDueDateIsNull() {
        String taskName = "Test Task";
        Subtask subtask = new Subtask();
        subtask.setDescription("Test Description");
        subtask.setWeight(100);
        subtask.setDueDate(null);

        controller.setSubtaskDetails(taskName, subtask, taskController);

        verify(deadlineLabel).setText("Brak terminu");
    }

    @Test
    void deleteSubtask_shouldShowConfirmationAlertAndNotDeleteSubtaskOnCancel() {
        Subtask subtaskToDelete = new Subtask();
        subtaskToDelete.setDescription("Subtask to delete");
        subtaskToDelete.setWeight(50);
        controller.currentSubtask = subtaskToDelete;

        Alert mockAlert = mock(Alert.class);
        when(mockAlert.showAndWait()).thenReturn(Optional.of(ButtonType.CANCEL));

        try (MockedConstruction<Alert> mockedConstruction = mockConstruction(Alert.class, (mock, context) -> {

        })) {
            controller.deleteSubtask();

            assertEquals(1, mockedConstruction.constructed().size());
            Alert createdAlert = mockedConstruction.constructed().get(0);
            verify(createdAlert).setTitle("Potwierdzenie usunięcia");
            verify(createdAlert).setHeaderText("Czy na pewno chcesz usunąć tę cząstkę?");
            verify(createdAlert).setContentText("Subtask to delete (Waga: 50)");
            verify(taskController, never()).deleteSubtask(any());
            verify(stage, never()).close();
        }
    }


    @Test
    void closeWindow_shouldCloseTheStage() {
        controller.closeWindow();
        verify(stage).close();
    }
}