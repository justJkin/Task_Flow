package com.mycompany.taskflow.controller.user;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.UserSession;
import com.mycompany.taskflow.model.user.Subtask;
import com.mycompany.taskflow.model.user.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TaskController {

    @FXML private ListView<String> taskPoolListView;
    @FXML private ListView<String> selectedTasksListView;
    @FXML private ListView<String> todoTasksListView;
    @FXML private ListView<String> taskPartsListView;

    private ObservableList<Task> taskPool = FXCollections.observableArrayList();
    private ObservableList<Task> selectedTasks = FXCollections.observableArrayList();
    private ObservableList<Task> todoTasks = FXCollections.observableArrayList();
    private ObservableList<String> taskParts = FXCollections.observableArrayList();

    public Task currentTask;
    private Map<String, ObservableList<Subtask>> taskSubtasksMap = new HashMap<>();
    private Integer loggedInUserId;

    @FXML
    public void initialize() {
        initializeUserSession();
        loadInitialTasks();
        setupListViewListeners();

        if (!todoTasks.isEmpty()) {
            currentTask = todoTasks.get(0);
            loadSubtasksForCurrentTask();
        }
    }
    private void initializeUserSession() {
        UserSession userSession = UserSession.getInstance();
        if (userSession != null) {
            loggedInUserId = userSession.getUserId();
        } else {
            System.err.println("Sesja użytkownika nie została poprawnie skonfigurowana");
        }
    }
    private void loadInitialTasks() {
        loadAllTasksIntoPool();
        loadUserTasks();
        updateAllListViews();
    }
    private void setupListViewListeners() {
        taskPoolListView.setOnMouseClicked(event -> handleTaskPoolSelection());
        selectedTasksListView.setOnMouseClicked(event -> handleSelectedTaskClick());
        todoTasksListView.setOnMouseClicked(event -> handleTodoTaskSelection());
        taskPartsListView.setOnMouseClicked(event -> handleSubtaskClick());
    }
    private void handleTaskPoolSelection() {
        String selectedTaskName = taskPoolListView.getSelectionModel().getSelectedItem();
        if (selectedTaskName != null) {
            Task selectedTask = findTaskByName(taskPool, selectedTaskName);
            if (selectedTask != null) {
                moveTaskBetweenLists(selectedTask, taskPool, selectedTasks);
                updateTaskPoolListView();
                updateSelectedTasksListView();
            }
        }
    }


    private void handleSelectedTaskClick() {
        String selectedTaskName = selectedTasksListView.getSelectionModel().getSelectedItem();
        if (selectedTaskName != null) {
            Task selectedTask = findTaskByName(selectedTasks, selectedTaskName);
            if (selectedTask != null) {
                moveTaskBetweenLists(selectedTask, selectedTasks, taskPool);
                updateTaskPoolListView();
                updateSelectedTasksListView();
            }
        }
    }

    private void handleTodoTaskSelection() {
        String selectedTaskName = todoTasksListView.getSelectionModel().getSelectedItem();
        if (selectedTaskName != null) {
            currentTask = findTaskByName(todoTasks, selectedTaskName);
            if (currentTask != null) {
                loadSubtasksForCurrentTask();
            }
        }
    }

    private void handleSubtaskClick() {
        String selectedSubtaskString = taskPartsListView.getSelectionModel().getSelectedItem();
        if (selectedSubtaskString != null && currentTask != null) {
            showSubtaskDetails(currentTask.getName(), selectedSubtaskString);
        }
    }

    private Task findTaskByName(ObservableList<Task> tasks, String displayString) {
        return tasks.stream()
                .filter(task -> {
                    String dueDateString = (task.getDueDate() != null)
                            ? " (Termin: " + task.getDueDate().toString() + ")"
                            : " (No Due Date)";
                    return (task.getName() + dueDateString).equals(displayString);
                })
                .findFirst()
                .orElse(null);
    }

    private void moveTaskBetweenLists(Task task, ObservableList<Task> source, ObservableList<Task> destination) {
        source.remove(task);
        destination.add(task);
    }

    private void loadAllTasksIntoPool() {
        taskPool.clear();
        String query = "SELECT id, milestone_id, name, description, status, priority, weight, progress, due_date, created_at, updated_at FROM task";

        try (Connection conn = DatabaseModel.connect();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Task task = createTaskFromResultSet(rs);
                taskPool.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserTasks() {
        todoTasks.clear();
        String query = "SELECT t.id, t.milestone_id, t.name, t.description, t.status, t.priority, t.weight, t.progress, t.due_date, t.created_at, t.updated_at " +
                "FROM task t INNER JOIN task_assignment ta ON t.id = ta.task_id " +
                "WHERE ta.user_id = ? AND t.status != 'Done'";

        try (Connection conn = DatabaseModel.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = createTaskFromResultSet(rs);
                todoTasks.add(task);
                taskPool.removeIf(t -> t.getId() == task.getId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private Task createTaskFromResultSet(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setMilestoneId(rs.getInt("milestone_id"));
        task.setName(rs.getString("name"));
        task.setDescription(rs.getString("description"));
        task.setStatus(rs.getString("status"));
        task.setPriority(rs.getShort("priority"));
        task.setWeight(rs.getInt("weight"));
        task.setProgress(rs.getShort("progress"));
        task.setDueDate(rs.getDate("due_date"));
        task.setCreatedAt(rs.getTimestamp("created_at"));
        task.setUpdatedAt(rs.getTimestamp("updated_at"));
        return task;
    }

    private void loadSubtasksForCurrentTask() {
        if (currentTask != null) {
            ObservableList<Subtask> subtasks = loadSubtasksForTask(currentTask.getId());
            taskSubtasksMap.put(currentTask.getName(), subtasks);
            updateTaskParts();
        }
    }

    private ObservableList<Subtask> loadSubtasksForTask(int taskId) {
        ObservableList<Subtask> subtasks = FXCollections.observableArrayList();
        String query = "SELECT id, task_id, name, description, weight, is_done, priority, due_date, created_at FROM subtask WHERE task_id = ?";

        try (Connection conn = DatabaseModel.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Subtask subtask = createSubtaskFromResultSet(rs);
                subtasks.add(subtask);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subtasks;
    }

    private Subtask createSubtaskFromResultSet(ResultSet rs) throws SQLException {
        Subtask subtask = new Subtask();
        subtask.setId(rs.getInt("id"));
        subtask.setTaskId(rs.getInt("task_id"));
        subtask.setName(rs.getString("name"));
        subtask.setDescription(rs.getString("description"));
        subtask.setWeight(rs.getInt("weight"));
        subtask.setDone(rs.getBoolean("is_done"));
        subtask.setPriority(rs.getShort("priority"));
        subtask.setDueDate(rs.getDate("due_date"));
        subtask.setCreatedAt(rs.getTimestamp("created_at"));
        return subtask;
    }

    @FXML
    public void undoSelection() {
        handleSelectedTaskClick();
    }

    @FXML
    public void confirmSelection() {
        for (Task taskToAssign : selectedTasks) {
            assignTaskToUser(taskToAssign.getId(), loggedInUserId);
            if (!todoTasks.contains(taskToAssign)) {
                todoTasks.add(taskToAssign);
            }
        }
        selectedTasks.clear();
        updateTodoTasksListView();
        updateSelectedTasksListView();
    }

    private void assignTaskToUser(int taskId, int userId) {
        String query = "INSERT INTO task_assignment (task_id, user_id) VALUES (?, ?)";
        try (Connection conn = DatabaseModel.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, taskId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate key value violates unique constraint")) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void editTaskDetails() {
        String selectedTaskName = todoTasksListView.getSelectionModel().getSelectedItem();
        if (selectedTaskName != null) {
            Task selectedTask = findTaskByName(todoTasks, selectedTaskName);
            if (selectedTask != null) {
                loadTaskDetailsView(selectedTask);
                currentTask = selectedTask;
                updateTaskParts();
            }
        }
    }

    private void loadTaskDetailsView(Task task) {
        try {
            URL fxmlLocation = getClass().getResource("/com/mycompany/taskflow/view/User/SubtaskView.fxml");
            if (fxmlLocation == null) {
                System.err.println("Nie znaleziono pliku FXML: /com/mycompany/taskflow/view/User/SubtaskView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            SubtaskController subtaskController = loader.getController();
            subtaskController.setTaskName(task.getName());
            subtaskController.setTaskId(task.getId());
            subtaskController.setTaskController(this);

            ObservableList<Subtask> existingSubtasks = loadSubtasksForTask(task.getId());
            subtaskController.setSubtasks(existingSubtasks);
            taskSubtasksMap.put(task.getName(), existingSubtasks);

            Stage stage = new Stage();
            stage.setTitle("Edycja Zadań - " + task.getName());
            stage.setScene(new Scene(root, 600, 400));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setSubtasks(String taskName, ObservableList<Subtask> subtasks) {
        taskSubtasksMap.put(taskName, subtasks);
        updateTaskParts();
    }


    public void setSubtasks(ObservableList<Subtask> subtasks) {
        if (currentTask != null) {
            taskSubtasksMap.put(currentTask.getName(), subtasks);
            updateTaskParts();
        }
    }

    private void showSubtaskDetails(String taskName, String subtaskDetailsString) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/User/SubtaskDetailsView.fxml"));
            Parent root = loader.load();

            SubtaskDetailsController subtaskDetailsController = loader.getController();

            if (currentTask != null) {
                ObservableList<Subtask> subtasks = taskSubtasksMap.getOrDefault(currentTask.getName(), FXCollections.observableArrayList());
                for (Subtask sub : subtasks) {
                    if ((sub.getDescription() + " (Waga: " + sub.getWeight() + ")").equals(subtaskDetailsString)) {
                        subtaskDetailsController.setSubtaskDetails(taskName, sub, this);
                        Stage stage = new Stage();
                        stage.setTitle("Szczegóły Cząsteczki Zadania");
                        stage.setScene(new Scene(root, 400, 300));
                        stage.show();
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateTaskParts() {
        taskParts.clear();
        if (currentTask != null) {
            ObservableList<Subtask> subtasks = taskSubtasksMap.getOrDefault(currentTask.getName(), FXCollections.observableArrayList());
            for (Subtask subtask : subtasks) {
                String displayText = subtask.getDescription() + " (Waga: " + subtask.getWeight() + ")";
                if (subtask.isDone()) {
                    Text completedText = new Text(displayText);
                    completedText.setFont(Font.font("System", FontWeight.NORMAL, 12));
                    completedText.setStrikethrough(true);
                    taskParts.add(completedText.getText());
                } else {
                    taskParts.add(displayText);
                }
            }
        }
        taskPartsListView.setItems(taskParts);
    }

    public void markSubtaskCompleted(String subtaskDetailsString) {
        if (currentTask != null) {
            ObservableList<Subtask> subtasks = taskSubtasksMap.getOrDefault(currentTask.getName(), FXCollections.observableArrayList());
            for (Subtask subtask : subtasks) {
                if ((subtask.getDescription() + " (Waga: " + subtask.getWeight() + ")").equals(subtaskDetailsString)) {
                    subtask.setDone(true);
                    updateSubtaskInDatabase(subtask);
                    updateTaskParts();
                    break;
                }
            }
        }
    }

    public void deleteSubtask(Subtask subtaskToDelete) {
        if (currentTask != null) {
            ObservableList<Subtask> subtasks = taskSubtasksMap.getOrDefault(currentTask.getName(), FXCollections.observableArrayList());
            subtasks.removeIf(subtask -> subtask.getId() == subtaskToDelete.getId());
            taskSubtasksMap.put(currentTask.getName(), subtasks);
            deleteSubtaskFromDatabase(subtaskToDelete);
            updateTaskParts();
        }
    }

    private void deleteSubtaskFromDatabase(Subtask subtask) {
        String deleteQuery = "DELETE FROM subtask WHERE id = ?";
        try (Connection conn = DatabaseModel.connect();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, subtask.getId());
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
            pstmt.setDate(5, subtask.getDueDate());
            pstmt.setInt(6, subtask.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateAllListViews() {
        updateTaskPoolListView();
        updateSelectedTasksListView();
        updateTodoTasksListView();
    }

    private void updateTaskPoolListView() {
        ObservableList<String> displayStrings = FXCollections.observableArrayList();
        for (Task task : taskPool) {
            String dueDateString = (task.getDueDate() != null) ? " (Termin: " + task.getDueDate().toString() + ")" : " (No Due Date)";
            displayStrings.add(task.getName() + dueDateString);
        }
        taskPoolListView.setItems(displayStrings);
    }

    private void updateSelectedTasksListView() {
        ObservableList<String> taskNames = FXCollections.observableArrayList();
        for (Task task : selectedTasks) {
            String dueDateString = (task.getDueDate() != null)
                    ? " (Termin: " + task.getDueDate().toString() + ")"
                    : " (Brak terminu)";
            taskNames.add(task.getName() + dueDateString);  // Dodajemy nazwę zadania + termin
        }
        selectedTasksListView.setItems(taskNames);
    }

    private void updateTodoTasksListView() {
        ObservableList<String> taskNames = FXCollections.observableArrayList();
        for (Task task : todoTasks) {
            String dueDateString = (task.getDueDate() != null)
                    ? " (Termin: " + task.getDueDate().toString() + ")"
                    : " (Brak terminu)";
            taskNames.add(task.getName() + dueDateString);  // Dodajemy nazwę zadania + termin
        }
        todoTasksListView.setItems(taskNames);
    }

}