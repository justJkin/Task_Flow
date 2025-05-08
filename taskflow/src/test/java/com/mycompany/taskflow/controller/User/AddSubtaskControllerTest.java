package com.mycompany.taskflow.controller.User;

import com.mycompany.taskflow.controller.JavaFXInitializer;
import com.mycompany.taskflow.controller.user.AddSubtaskController;
import com.mycompany.taskflow.controller.user.SubtaskController;
import com.mycompany.taskflow.model.user.Subtask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)

class AddSubtaskControllerTest extends JavaFXInitializer {

    @InjectMocks
    private AddSubtaskController controller;

    @Mock
    private TextArea descriptionTextArea;

    @Mock
    private Scene scene;

    @Mock
    private Stage stage;

    @Mock
    private javafx.scene.control.TextField weightTextField;

    @Mock
    private javafx.scene.control.Label titleLabel;
    @Mock
    private SubtaskController subtaskController;

    @Mock
    private javafx.scene.control.ComboBox<Short> priorityComboBox;

    @Mock
    private javafx.scene.control.TextField nameTextField;

    @Mock
    private DatePicker dueDatePicker;


    @BeforeEach
    void setUpForSetInitialData() {
        controller = new AddSubtaskController();
        MockitoAnnotations.openMocks(this);


        when(descriptionTextArea.getScene()).thenReturn(scene);
        when(scene.getWindow()).thenReturn(stage);

        controller.descriptionTextArea = descriptionTextArea;
        controller.weightTextField = weightTextField;

    }

    @BeforeEach
    void setUp() throws IOException {
        controller = new AddSubtaskController();
        MockitoAnnotations.openMocks(this);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/User/AddSubtaskView.fxml"));

        Parent root = loader.load();
        controller = loader.getController();

        MockitoAnnotations.openMocks(this);

        controller.descriptionTextArea = descriptionTextArea;
        controller.weightTextField = weightTextField;
        controller.titleLabel = titleLabel;
        controller.subtaskController = subtaskController;
        controller.priorityComboBox = priorityComboBox;
        controller.titleLabel = titleLabel;
        controller.nameTextField = nameTextField;
        controller.dueDatePicker = dueDatePicker;

        when(subtaskController.getSubtasks()).thenReturn(FXCollections.observableArrayList());
        when(descriptionTextArea.getScene()).thenReturn(scene);
        when(scene.getWindow()).thenReturn(stage);
        when(priorityComboBox.getValue()).thenReturn(null);
        when(priorityComboBox.getValue()).thenReturn((short) 1);

    }



    @Test
    void testCloseWindow_shouldCloseStage() {
        controller.closeWindow();
        verify(stage).close();

    }

    @Test

    void testResetEditIndex() {
        AddSubtaskController controller = new AddSubtaskController();
        controller.editIndex = 10;
        controller.resetEditIndex();
        assertEquals(-1, controller.editIndex, "Metoda resetEditIndex powinna ustawić editIndex na -1.");

    }

    @Test
    void testSetSubtaskController() {
        AddSubtaskController controller = new AddSubtaskController();
        SubtaskController mockSubtaskController = mock(SubtaskController.class);

        controller.setSubtaskController(mockSubtaskController);
        assertEquals(mockSubtaskController, controller.subtaskController, "Metoda setSubtaskController powinna ustawić subtaskController.");

    }

    @Nested
    class TestSetInitialData {
        @Test
        void shouldSetDescriptionWhenItemHasNoWeight() {
            String item = "Przykładowy opis bez wagi";
            controller.setInitialData(item);
            verify(descriptionTextArea).setText(item);
            verify(weightTextField).setText("");

        }

        @Test
        void shouldSetDescriptionAndWeightWhenItemHasWeight() {
            String item = "Inny opis (50)";
            String expectedDescription = "Inny opis";
            String expectedWeight = "50";

            controller.setInitialData(item);
            verify(descriptionTextArea).setText(expectedDescription);
            verify(weightTextField).setText(expectedWeight);

        }
        @Test

        void shouldSetDescriptionWhenWeightFormatIsInvalid() {
            String item = "Opis z nieprawidłową wagą (abc)";
            controller.setInitialData(item);
            verify(descriptionTextArea).setText(item);
            verify(weightTextField).setText("");

        }

        @Test

        void shouldSetDescriptionForEmptyItem() {

            String item = "";
            controller.setInitialData(item);
            verify(descriptionTextArea).setText("");
            verify(weightTextField).setText("");

        }


        @Test

        void shouldSetDescriptionForNullItem() {
            controller.setInitialData(null);
            verify(descriptionTextArea).setText("");
            verify(weightTextField).setText("");

        }

    }

    @Test

    void testSetEditIndex_shouldLoadDataIfIndexIsValid() {
        int testIndex = 2;
        String testDescription = "Opis edytowanego podzadania";
        int testWeight = 75;

        Subtask mockSubtask = mock(Subtask.class);
        when(mockSubtask.getDescription()).thenReturn(testDescription);
        when(mockSubtask.getWeight()).thenReturn(testWeight);
        when(subtaskController.getSubtasks()).thenReturn(FXCollections.observableArrayList(
                mock(Subtask.class), mock(Subtask.class), mockSubtask

        ));

        controller.setEditIndex(testIndex);
        verify(descriptionTextArea).setText(testDescription);
        verify(weightTextField).setText(String.valueOf(testWeight));
    }

    @Test

    void testInitialize_shouldSetTitleLabelForEditingSubtask() {
        controller.editIndex = 2;
        controller.initialize();
        verify(titleLabel).setText("Edytuj Cząstkę Zadania");
    }



    @Test

    void testInitialize_shouldFillTextFieldsWithDataIfEditIndexIsValid() {
        int testIndex = 1;
        String testName = "Edytowana nazwa";
        String testDescription = "Edytowany opis";
        int testWeight = 99;
        short testPriority = 3;

        Subtask mockSubtask = mock(Subtask.class);
        when(mockSubtask.getName()).thenReturn(testName);
        when(mockSubtask.getDescription()).thenReturn(testDescription);
        when(mockSubtask.getWeight()).thenReturn(testWeight);
        when(mockSubtask.getPriority()).thenReturn(testPriority);
        when(subtaskController.getSubtasks()).thenReturn(FXCollections.observableArrayList(
                mock(Subtask.class), mockSubtask, mock(Subtask.class)

        ));

        controller.editIndex = testIndex;
        controller.initialize();

        verify(descriptionTextArea).setText(testDescription);
        verify(weightTextField).setText(String.valueOf(testWeight));

        when(priorityComboBox.getValue()).thenReturn(testPriority);

        assertEquals(testPriority, priorityComboBox.getValue(), "Priorytet powinien zostać ustawiony.");

    }

    @Test

    void testInitialize_shouldNotFillTextFieldsIfEditIndexIsInvalid() {

        controller.editIndex = -1;
        controller.initialize();

        verify(descriptionTextArea, never()).setText(anyString());
        verify(weightTextField, never()).setText(anyString());

        assertEquals((short) 1, controller.priorityComboBox.getValue(), "Priorytet powinien pozostać domyślny.");

    }


    @Test
    void testSaveSubtask_validInputShouldAddSubtaskAndCloseWindow() {
        when(nameTextField.getText()).thenReturn("Nazwa podzadania");
        when(descriptionTextArea.getText()).thenReturn("Opis podzadania");
        when(weightTextField.getText()).thenReturn("30");
        when(priorityComboBox.getValue()).thenReturn((short) 2);
        when(dueDatePicker.getValue()).thenReturn(LocalDate.now());

        controller.saveSubtask();

        verify(subtaskController).addSubtask(
                eq("Nazwa podzadania"),
                eq("Opis podzadania"),
                eq("30"),
                eq(-1),
                eq((short) 2),
                any(LocalDate.class)
        );
        verify(stage).close();
    }
    @Test
    void testSaveSubtask_validInputForEditShouldUpdateSubtaskAndCloseWindow() {
        controller.editIndex = 1;

        when(nameTextField.getText()).thenReturn("Zmieniona nazwa");
        when(descriptionTextArea.getText()).thenReturn("Zmieniony opis");
        when(weightTextField.getText()).thenReturn("40");
        when(priorityComboBox.getValue()).thenReturn((short) 4);
        when(dueDatePicker.getValue()).thenReturn(LocalDate.now().plusDays(7));

        controller.saveSubtask();

        verify(subtaskController).addSubtask(
                eq("Zmieniona nazwa"),
                eq("Zmieniony opis"),
                eq("40"),
                eq(1),
                eq((short) 4),
                any(LocalDate.class)
        );
        verify(stage).close();

    }
    @Test
    void testCancel_shouldCloseWindow() {
        controller.cancel();
        verify(stage).close();
    }


}