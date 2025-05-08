package com.mycompany.taskflow.controller.User;

import com.mycompany.taskflow.controller.JavaFXInitializer;
import com.mycompany.taskflow.controller.user.BoardController;
import com.mycompany.taskflow.controller.user.NotificationsController;
import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.UserSession;
import com.mycompany.taskflow.model.user.Subtask;
import com.mycompany.taskflow.model.user.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BoardControllerTest extends JavaFXInitializer {
    @InjectMocks
    private BoardController boardController;
    @Mock

    private ObservableList<Pair<Subtask, String>> todoSubtasks;
    private ObservableList<Pair<Subtask, String>> doneSubtasks;
    private Subtask subtask;
    private String taskName;
    private ListView<Task> todoListView;
    private ObservableList<Task> todoTasks;
    @Mock
    private BiConsumer<Task, String> onMoveMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        todoSubtasks = FXCollections.observableArrayList();
        doneSubtasks = FXCollections.observableArrayList();

        // Create subtask and set properties
        subtask = new Subtask();
        subtask.setId(1);
        subtask.setName("Test Subtask");
        subtask.setDone(false);

        taskName = "Test Task";

        todoSubtasks.add(new Pair<>(subtask, taskName));

        MockitoAnnotations.openMocks(this);

        todoTasks = FXCollections.observableArrayList();
        todoListView = new ListView<>(todoTasks);

        boardController.todoListView = todoListView;
    }


    @Test
    public void przetlumaczStatus_ToDo() {
        String status = "To Do";
        // BoardController board = new BoardController();
        String result = boardController.przetlumaczStatus(status);
        assertEquals("Do zrobienia", result);
    }

    @Test
    public void przetlumaczStatus_InProgress() {
        String status = "In Progress";
        BoardController board = new BoardController();
        String result = board.przetlumaczStatus(status);
        assertEquals("W trakcie", result);
    }

    @Test
    public void przetlumaczStatus_Done() {
        String status = "Done";
        BoardController board = new BoardController();
        String result = board.przetlumaczStatus(status);
        assertEquals("Zrobione", result);
    }

    @Test
    public void przetlumaczStatusPodzadania_Zrobione() {
        Boolean isDone = true;
        BoardController board = new BoardController();
        String result = board.przetlumaczStatusPodzadania(isDone);
        assertEquals("Zrobione", result);
    }

    @Test
    public void przetlumaczStatusPodzadania_DoZrobienia() {
        Boolean isDone = false;
        BoardController board = new BoardController();
        String result = board.przetlumaczStatusPodzadania(isDone);
        assertEquals("Do zrobienia", result);
    }

    @Test
    public void moveTask_ToDoToInProgress() {
        BoardController controller = Mockito.spy(new BoardController());
        ObservableList<Task> todoTasks = FXCollections.observableArrayList();
        ObservableList<Task> inProgressTasks = FXCollections.observableArrayList();
        ObservableList<Task> doneTasks = FXCollections.observableArrayList();
        controller.todoTasks = todoTasks;
        controller.inProgressTasks = inProgressTasks;
        controller.doneTasks = doneTasks;
        doNothing().when(controller).updateTaskStatusInDatabase(anyInt(), anyString());
        doNothing().when(controller).showNotification(anyString());
        Task task = new Task();
        task.setId(1);
        task.setName("Test Task");
        task.setStatus("To Do");
        todoTasks.add(task);
        controller.moveTask(task, "right");
        assertEquals(0, todoTasks.size());
        assertEquals(1, inProgressTasks.size());
        assertEquals(task, inProgressTasks.get(0));
        assertEquals("In Progress", task.getStatus());
        verify(controller).updateTaskStatusInDatabase(anyInt(), anyString());
        verify(controller).showNotification(anyString());
    }

    @Test
    public void moveTask_InProgressToToDo() {
        BoardController controller = Mockito.spy(new BoardController());
        ObservableList<Task> inProgressTasks = FXCollections.observableArrayList();
        ObservableList<Task> todoTasks = FXCollections.observableArrayList();
        ObservableList<Task> doneTasks = FXCollections.observableArrayList();

        controller.inProgressTasks = inProgressTasks;
        controller.doneTasks = doneTasks;
        controller.todoTasks = todoTasks;

        doNothing().when(controller).updateTaskStatusInDatabase(anyInt(), anyString());
        doNothing().when(controller).showNotification(anyString());

        Task task = new Task();
        task.setId(1);
        task.setName("Test Task");
        task.setStatus("In Progress");

        inProgressTasks.add(task);

        controller.moveTask(task, "left");

        assertEquals(0, inProgressTasks.size());
        assertEquals(1, todoTasks.size());
        assertEquals(task, todoTasks.get(0));

        verify(controller).updateTaskStatusInDatabase(anyInt(), anyString());
        verify(controller).showNotification(anyString());
    }

    @Test
    public void moveTask_DoneToInProgress() {
        BoardController controller = Mockito.spy(new BoardController());

        ObservableList<Task> doneTasks = FXCollections.observableArrayList();
        ObservableList<Task> inProgressTasks = FXCollections.observableArrayList();
        ObservableList<Task> todoTasks = FXCollections.observableArrayList();

        controller.doneTasks = doneTasks;
        controller.inProgressTasks = inProgressTasks;
        controller.todoTasks = todoTasks;

        doNothing().when(controller).updateTaskStatusInDatabase(anyInt(), anyString());
        doNothing().when(controller).showNotification(anyString());

        Task task = new Task();
        task.setId(1);
        task.setName("Test Task");
        task.setStatus("Done");

        doneTasks.add(task);
        controller.moveTask(task, "left");
        assertEquals(0, doneTasks.size());
        assertEquals(1, inProgressTasks.size());
        assertEquals(task, inProgressTasks.get(0));

        verify(controller).updateTaskStatusInDatabase(anyInt(), anyString());
        verify(controller).showNotification(anyString());

    }

    @Test
    public void moveTask_InProgressToDone() {
        BoardController controller = Mockito.spy(new BoardController());
        ObservableList<Task> inProgressTasks = FXCollections.observableArrayList();
        ObservableList<Task> doneTasks = FXCollections.observableArrayList();

        controller.inProgressTasks = inProgressTasks;
        controller.doneTasks = doneTasks;

        doNothing().when(controller).updateTaskStatusInDatabase(anyInt(), anyString());
        doNothing().when(controller).showNotification(anyString());

        Task task = new Task();
        task.setId(1);
        task.setName("Test Task");
        task.setStatus("In Progress");
        inProgressTasks.add(task);

        controller.moveTask(task, "right");
        controller.doneTasks = doneTasks;

        assertEquals(0, inProgressTasks.size());
        assertEquals(1, doneTasks.size());
        assertEquals(task, doneTasks.get(0));
        assertEquals("Done", task.getStatus());

        verify(controller).updateTaskStatusInDatabase(1, "Done");
        verify(controller).showNotification(anyString());

    }

    @Test
    public void testMoveSubtask_ToDoToDone() {
        BoardController controller = Mockito.spy(boardController);

        ObservableList<Pair<Subtask, String>> todoSubtasks = controller.todoSubtasks;
        ObservableList<Pair<Subtask, String>> doneSubtasks = controller.doneSubtasks;

        doNothing().when(controller).updateSubtaskIsDoneInDatabase(anyInt(), anyBoolean());
        doNothing().when(controller).showNotification(anyString());

        Subtask subtask = new Subtask();
        subtask.setId(1);
        subtask.setName("Test Subtask");
        subtask.setDone(false);

        Pair<Subtask, String> subtaskPair = new Pair<>(subtask, "Test Task");
        todoSubtasks.add(subtaskPair);

        controller.moveSubtask(subtaskPair, "right");

        assertEquals(0, todoSubtasks.size(), "To-Do list should be empty after move.");
        assertEquals(1, doneSubtasks.size(), "Done list should have one subtask.");
        assertEquals(subtask.getId(), doneSubtasks.get(0).getKey().getId(), "Subtask ID should match.");
        assertTrue(doneSubtasks.get(0).getKey().isDone(), "Subtask should be marked as done.");

        verify(controller).updateSubtaskIsDoneInDatabase(1, true);
        verify(controller).showNotification(anyString());
    }

    @Test
    public void saveNotificationToDatabase_zapisujeDoBazy() throws Exception {
        BoardController controller = new BoardController();
        int userId = 1;
        String content = "Testowa treść powiadomienia";

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);

        MockedStatic<DatabaseModel> databaseModelMockedStatic = mockStatic(DatabaseModel.class);
        databaseModelMockedStatic.when(DatabaseModel::connect).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);


        controller.saveNotificationToDatabase(userId, content);

        verify(mockStatement).setInt(eq(1), eq(userId));
        verify(mockStatement).setString(eq(2), eq(content));
        verify(mockStatement).setString(eq(3), eq("update"));
        verify(mockStatement).setTimestamp(eq(4), any());
        verify(mockStatement).setBoolean(eq(5), eq(false));
        verify(mockStatement).executeUpdate();

        databaseModelMockedStatic.close();
    }

    @Test
    public void updateTaskStatusInDatabase_aktualizujeStatusZadania() throws Exception {
        BoardController controller = new BoardController();
        int taskId = 1;
        String newStatus = "Done";

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);

        MockedStatic<DatabaseModel> dbMock = mockStatic(DatabaseModel.class);
        dbMock.when(DatabaseModel::connect).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        controller.updateTaskStatusInDatabase(taskId, newStatus);

        verify(mockStatement).setString(1, newStatus);
        verify(mockStatement).setInt(2, taskId);
        verify(mockStatement).executeUpdate();

        dbMock.close();
    }

    @Test
    public void setupUserAndLoadData_powinnoUstawicUserIdILadowacDane() {
        BoardController controller = Mockito.spy(new BoardController());

        UserSession mockSession = mock(UserSession.class);
        when(mockSession.getUserId()).thenReturn(3);
        MockedStatic<UserSession> sessionMocked = mockStatic(UserSession.class);
        sessionMocked.when(UserSession::getInstance).thenReturn(mockSession);

        doNothing().when(controller).loadTasksAndSubtasksFromDatabase();

        controller.setupUserAndLoadData();

        assertEquals(3, controller.loggedInUserId);
        verify(controller).loadTasksAndSubtasksFromDatabase();

        sessionMocked.close();
    }

    @Test
    public void updateSubtaskIsDoneInDatabase_powinnoWyslacUpdate() throws Exception {
        BoardController controller = new BoardController();
        int subtaskId = 5;
        boolean isDone = true;

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);

        MockedStatic<DatabaseModel> dbMock = mockStatic(DatabaseModel.class);
        dbMock.when(DatabaseModel::connect).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        controller.updateSubtaskIsDoneInDatabase(subtaskId, isDone);

        verify(mockStatement).setBoolean(1, isDone);
        verify(mockStatement).setInt(2, subtaskId);
        verify(mockStatement).executeUpdate();

        dbMock.close();
    }

    @Test
    public void setNotificationsController_powinnoUstawicController() {
        BoardController controller = new BoardController();
        NotificationsController mockNotificationsController = mock(NotificationsController.class);

        controller.setNotificationsController(mockNotificationsController);

        assertEquals(mockNotificationsController, controller.notificationsController);
    }

    @Test
    public void loadTasksAndSubtasksFromDatabase_powinnoDodacZadaniaDoList() throws Exception {
        BoardController controller = new BoardController();
        controller.loggedInUserId = 1;

        controller.todoTasks = FXCollections.observableArrayList();
        controller.inProgressTasks = FXCollections.observableArrayList();
        controller.doneTasks = FXCollections.observableArrayList();
        controller.todoSubtasks = FXCollections.observableArrayList();
        controller.doneSubtasks = FXCollections.observableArrayList();

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockTaskStmt = mock(PreparedStatement.class);
        PreparedStatement mockSubtaskStmt = mock(PreparedStatement.class);
        ResultSet mockTaskResultSet = mock(ResultSet.class);
        ResultSet mockSubtaskResultSet = mock(ResultSet.class);

        MockedStatic<DatabaseModel> dbMock = mockStatic(DatabaseModel.class);
        dbMock.when(DatabaseModel::connect).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(contains("SELECT t.id"))).thenReturn(mockTaskStmt);
        when(mockConnection.prepareStatement(contains("SELECT s.id"))).thenReturn(mockSubtaskStmt);

        when(mockTaskStmt.executeQuery()).thenReturn(mockTaskResultSet);
        when(mockTaskResultSet.next()).thenReturn(true, false);
        when(mockTaskResultSet.getInt("id")).thenReturn(1);
        when(mockTaskResultSet.getString("name")).thenReturn("Zadanie testowe");
        when(mockTaskResultSet.getString("status")).thenReturn("To Do");

        when(mockSubtaskStmt.executeQuery()).thenReturn(mockSubtaskResultSet);
        when(mockSubtaskResultSet.next()).thenReturn(false);

        controller.loadTasksAndSubtasksFromDatabase();

        assertEquals(1, controller.todoTasks.size());
        assertEquals("Zadanie testowe", controller.todoTasks.get(0).getName());
        assertEquals("To Do", controller.todoTasks.get(0).getStatus());

        dbMock.close();
    }

    @Test
    public void moveSubtask_DoZrobieniaKliknieLewo_NicSieNieZmienic() {
        BoardController controller = Mockito.spy(new BoardController());

        ObservableList<Pair<Subtask, String>> todoSubtasks = FXCollections.observableArrayList();
        ObservableList<Pair<Subtask, String>> doneSubtasks = FXCollections.observableArrayList();

        controller.todoSubtasks = todoSubtasks;
        controller.doneSubtasks = doneSubtasks;

        Subtask subtask = new Subtask();
        subtask.setId(1);
        subtask.setDescription("Testowy subtask");
        subtask.setDone(false);

        Pair<Subtask, String> subtaskPair = new Pair<>(subtask, "Zadanie 1");
        todoSubtasks.add(subtaskPair);

        doNothing().when(controller).updateSubtaskIsDoneInDatabase(anyInt(), anyBoolean());
        doNothing().when(controller).showNotification(anyString());

        controller.moveSubtask(subtaskPair, "left");

        assertEquals(1, todoSubtasks.size(), "Subtask powinien pozostać w To Do");
        assertEquals(0, doneSubtasks.size(), "Nie powinno być subtasków w Done");
        assertFalse(subtask.isDone(), "Subtask nadal powinien być oznaczony jako nieukończony");

        verify(controller, never()).updateSubtaskIsDoneInDatabase(anyInt(), anyBoolean());
        verify(controller, never()).showNotification(anyString());
    }

    @Test
    public void getColorForStatus_zwrociOdpowiedniKolorDlaStatusow() {
        assertEquals("#6495ED", com.mycompany.taskflow.controller.user.BoardController.TaskCell.getColorForStatus("To Do"));
        assertEquals("#FFA500", com.mycompany.taskflow.controller.user.BoardController.TaskCell.getColorForStatus("In Progress"));
        assertEquals("#3CB371", com.mycompany.taskflow.controller.user.BoardController.TaskCell.getColorForStatus("Done"));
        assertEquals("blue", com.mycompany.taskflow.controller.user.BoardController.TaskCell.getColorForStatus("Nieznany"));
    }
    @Test
    public void showNotification_powinienZapisacIPrzeladowac() {
        BoardController controller = Mockito.spy(new BoardController());
        controller.loggedInUserId = 42;

        NotificationsController mockNotificationsController = mock(NotificationsController.class);
        controller.setNotificationsController(mockNotificationsController);
        doNothing().when(controller).saveNotificationToDatabase(eq(42), anyString());

        controller.showNotification("Powiadomienie testowe");

        verify(controller).saveNotificationToDatabase(42, "Powiadomienie testowe");
        verify(mockNotificationsController).refreshNotifications();
    }

    @Test
    public void updateItem_powinienUstawicTekstIKolorWskaznikaDlaStatusu() {
        Task testTask = new Task();
        testTask.setName("Zadanie testowe");
        testTask.setStatus("In Progress");

        BoardController.TaskCell cell = new BoardController.TaskCell((t, d) -> {});

        cell.updateItem(testTask, false);

        assertEquals("Zadanie testowe", cell.taskLabel.getText());
        String expectedColor = "-fx-fill: #FFA500;";
        assertEquals(expectedColor, cell.indicator.getStyle());
    }
    @Test
    public void updateItem_emptyTrue_powinienUstawicNull() {
        BoardController.TaskCell cell = new BoardController.TaskCell((t, d) -> {});

        cell.updateItem(null, true);

        assertNull(cell.getGraphic());
        assertNull(cell.getText());
    }
    @Test
    public void updateItem_powinienUstawicTekstIKolorWskaznika() {
        Subtask subtask = new Subtask();
        subtask.setDescription("Zrób coś");
        subtask.setDone(true);

        String taskName = "Zadanie A";
        Pair<Subtask, String> item = new Pair<>(subtask, taskName);

        BoardController.SubtaskCell cell = new BoardController.SubtaskCell((s, d) -> {});

        cell.updateItem(item, false);

        assertEquals("Zrób coś (Zadanie: Zadanie A)", cell.subtaskLabel.getText());
        assertEquals("-fx-fill: #3CB371;", cell.indicator.getStyle());
    }
    @Test
    public void updateItem_emptyTrue_powinienWyczyscicKomorke() {
        BoardController.SubtaskCell cell = new BoardController.SubtaskCell((s, d) -> {});

        cell.updateItem(null, true);

        assertNull(cell.getGraphic());
        assertNull(cell.getText());
    }
    @Test
    public void initialize_powinnoZainicjalizowacKomponenty() {
        BoardController controller = Mockito.spy(new BoardController());

        controller.todoListView = new ListView<>();
        controller.inProgressListView = new ListView<>();
        controller.doneListView = new ListView<>();
        controller.todoSubtasksListView = new ListView<>();
        controller.doneSubtasksListView = new ListView<>();

        doNothing().when(controller).setupUserAndLoadData();

        controller.initialize();

        verify(controller).setupUserAndLoadData();

        assertSame(controller.todoTasks, controller.todoListView.getItems());
        assertSame(controller.inProgressTasks, controller.inProgressListView.getItems());
        assertSame(controller.doneTasks, controller.doneListView.getItems());
        assertSame(controller.todoSubtasks, controller.todoSubtasksListView.getItems());
        assertSame(controller.doneSubtasks, controller.doneSubtasksListView.getItems());

        assertNotNull(controller.todoListView.getCellFactory());
        assertNotNull(controller.doneListView.getCellFactory());
        assertNotNull(controller.inProgressListView.getCellFactory());
        assertNotNull(controller.todoSubtasksListView.getCellFactory());
        assertNotNull(controller.doneSubtasksListView.getCellFactory());
    }
    @Test
    public void taskCell_updateItem_nullPowinienWyczyscicGrafike() {
        BoardController.TaskCell cell = new BoardController.TaskCell((task, dir) -> {});
        cell.updateItem(null, false);

        assertNull(cell.getGraphic());
        assertNull(cell.getText());
    }
    @Test
    public void subtaskCell_updateItem_nullPowinienWyczyscicGrafike() {
        BoardController.SubtaskCell cell = new BoardController.SubtaskCell((s, d) -> {});

        cell.updateItem(null, false);

        assertNull(cell.getGraphic());
        assertNull(cell.getText());
    }
    @Test
    public void taskCell_leftButtonClick_powinienWywolacOnMove() {
        Task mockTask = new Task();
        mockTask.setName("Test Task");
        mockTask.setStatus("To Do");

        @SuppressWarnings("unchecked")
        BiConsumer<Task, String> onMoveMock = mock(BiConsumer.class);

        BoardController.TaskCell cell = new BoardController.TaskCell(onMoveMock);
        cell.updateItem(mockTask, false);

        cell.leftButton.fire();

        verify(onMoveMock).accept(mockTask, "left");
    }
    @Test
    public void subtaskCell_rightButtonClick_powinienWywolacOnMove() {
        Subtask mockSubtask = new Subtask();
        mockSubtask.setDescription("Testowy opis");
        mockSubtask.setDone(false);
        mockSubtask.setId(2);
        Pair<Subtask, String> pair = new Pair<>(mockSubtask, "Testowe Zadanie");

        @SuppressWarnings("unchecked")
        BiConsumer<Pair<Subtask, String>, String> onMoveMock = mock(BiConsumer.class);

        BoardController.SubtaskCell cell = new BoardController.SubtaskCell(onMoveMock);
        cell.updateItem(pair, false);

        cell.rightButton.fire();

        verify(onMoveMock).accept(pair, "right");
    }
    @Test
    public void saveNotificationToDatabase_throwsSQLException_logujeBlad() throws Exception {
        BoardController controller = new BoardController();
        int userId = 1;
        String content = "Błąd testowy";

        Connection mockConnection = mock(Connection.class);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        try (MockedStatic<DatabaseModel> mockedStatic = mockStatic(DatabaseModel.class)) {
            mockedStatic.when(DatabaseModel::connect).thenReturn(mockConnection);

            assertDoesNotThrow(() -> controller.saveNotificationToDatabase(userId, content));
        }
    }
    @Test
    public void moveTask_ToDoNaInProgress_powinnoPrzeniescZadanie() {
        BoardController controller = Mockito.spy(new BoardController());

        controller.todoTasks = FXCollections.observableArrayList();
        controller.inProgressTasks = FXCollections.observableArrayList();
        controller.doneTasks = FXCollections.observableArrayList();

        Task task = new Task();
        task.setId(1);
        task.setName("Zadanie testowe");
        task.setStatus("To Do");
        controller.todoTasks.add(task);

        doNothing().when(controller).updateTaskStatusInDatabase(anyInt(), anyString());
        doReturn("Do zrobienia").when(controller).przetlumaczStatus("To Do");
        doReturn("W trakcie").when(controller).przetlumaczStatus("In Progress");
        doNothing().when(controller).showNotification(anyString());

        controller.moveTask(task, "right");

        assertFalse(controller.todoTasks.contains(task));
        assertTrue(controller.inProgressTasks.contains(task));
        assertEquals("In Progress", task.getStatus());

        verify(controller).updateTaskStatusInDatabase(1, "In Progress");
        verify(controller).showNotification("Zadanie 'Zadanie testowe' przeniesiono z 'Do zrobienia' do 'W trakcie'");
    }

    @Test
    public void loadTasksAndSubtasksFromDatabase_throwsSQLException_shouldHandleException() throws Exception {
        BoardController controller = new BoardController();
        controller.loggedInUserId = 1;

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockTaskStmt = mock(PreparedStatement.class);
        PreparedStatement mockSubtaskStmt = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        MockedStatic<DatabaseModel> dbMock = mockStatic(DatabaseModel.class);
        dbMock.when(DatabaseModel::connect).thenReturn(mockConnection);

        BoardController controllerSpy = Mockito.spy(controller);

        assertDoesNotThrow(() -> controllerSpy.loadTasksAndSubtasksFromDatabase());

        assertEquals(0, controllerSpy.todoTasks.size());
        assertEquals(0, controllerSpy.inProgressTasks.size());
        assertEquals(0, controllerSpy.doneTasks.size());
        assertEquals(0, controllerSpy.todoSubtasks.size());
        assertEquals(0, controllerSpy.doneSubtasks.size());

        dbMock.close();
    }
    @Test
    public void taskCell_rightButtonClick_powinienWywolacOnMove() {
        Task mockTask = new Task();
        mockTask.setName("Test Task");
        mockTask.setStatus("To Do");
        @SuppressWarnings("unchecked")
        BiConsumer<Task, String> onMoveMock = mock(BiConsumer.class);

        BoardController.TaskCell cell = new BoardController.TaskCell(onMoveMock);
        cell.updateItem(mockTask, false);

        cell.rightButton.fire();

        verify(onMoveMock).accept(mockTask, "right");
    }
    @Test
    public void subtaskCell_leftButtonClick_powinienWywolacOnMove() {
        Subtask mockSubtask = new Subtask();
        mockSubtask.setDescription("Testowy opis");
        mockSubtask.setDone(false);
        mockSubtask.setId(2);
        Pair<Subtask, String> pair = new Pair<>(mockSubtask, "Testowe Zadanie");

        @SuppressWarnings("unchecked")
        BiConsumer<Pair<Subtask, String>, String> onMoveMock = mock(BiConsumer.class);

        BoardController.SubtaskCell cell = new BoardController.SubtaskCell(onMoveMock);
        cell.updateItem(pair, false);

        cell.leftButton.fire();

        verify(onMoveMock).accept(pair, "left");
    }
}