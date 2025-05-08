package com.mycompany.taskflow.controller.user;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.UserSession;
import com.mycompany.taskflow.model.user.Subtask;
import com.mycompany.taskflow.model.user.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;

public class BoardController {

    public ObservableList<Pair<Subtask, String>> inProgressSubtasks;
    public ObservableList<Pair<Subtask, String>> todoSubtasksPairs;
    public ObservableList<Pair<Subtask, String>> doneSubtasksPairs;
    @FXML
    public ListView<Task> todoListView;
    @FXML
    public ListView<Task> inProgressListView;
    @FXML
    public ListView<Task> doneListView;
    @FXML
    public ListView<Pair<Subtask, String>> todoSubtasksListView;
    @FXML
    public ListView<Pair<Subtask, String>> doneSubtasksListView;

    public ObservableList<Task> todoTasks = FXCollections.observableArrayList();
    public ObservableList<Task> inProgressTasks = FXCollections.observableArrayList();
    public ObservableList<Task> doneTasks = FXCollections.observableArrayList();
    public ObservableList<Pair<Subtask, String>> todoSubtasks = FXCollections.observableArrayList();
    public ObservableList<Pair<Subtask, String>> doneSubtasks = FXCollections.observableArrayList();

    public Integer loggedInUserId;
    public NotificationsController notificationsController;
    public UserSession userSession;

    public void setNotificationsController(NotificationsController notificationsController) {
        this.notificationsController = notificationsController;
    }

    public void setupUserAndLoadData() {
        UserSession userSession = UserSession.getInstance();
        if (userSession != null) {
            loggedInUserId = userSession.getUserId();
            loadTasksAndSubtasksFromDatabase();
        } else {
            System.err.println("User session not initialized.");
        }
    }

    @FXML
    public void initialize() {
        setupUserAndLoadData();

        todoListView.setItems(todoTasks);
        inProgressListView.setItems(inProgressTasks);
        doneListView.setItems(doneTasks);
        todoSubtasksListView.setItems(todoSubtasks);
        doneSubtasksListView.setItems(doneSubtasks);

        todoListView.setCellFactory(param -> new TaskCell(this::moveTask));
        inProgressListView.setCellFactory(param -> new TaskCell(this::moveTask));
        doneListView.setCellFactory(param -> new TaskCell(this::moveTask));
        todoSubtasksListView.setCellFactory(param -> new SubtaskCell(this::moveSubtask));
        doneSubtasksListView.setCellFactory(param -> new SubtaskCell(this::moveSubtask));
    }


    public static class TaskCell extends ListCell<Task> {
        protected HBox hbox;
        public Label taskLabel;
        public Button leftButton;
        public Button rightButton;
        public final BiConsumer<Task, String> onMove;
        public Circle indicator;

        public TaskCell(BiConsumer<Task, String> onMove) {
            super();
            this.onMove = onMove;
            hbox = new HBox(10);
            hbox.setFocusTraversable(false);

            indicator = new Circle(6);
            indicator.getStyleClass().add("indicator");
            indicator.setFocusTraversable(false);

            taskLabel = new Label();
            taskLabel.setFocusTraversable(false);

            leftButton = new Button("<");
            rightButton = new Button(">");
            leftButton.getStyleClass().add("button");
            rightButton.getStyleClass().add("button");
            leftButton.setFocusTraversable(false);
            rightButton.setFocusTraversable(false);

            leftButton.setOnAction(event -> {
                if (getItem() != null) {
                    onMove.accept(getItem(), "left");
                }
            });

            rightButton.setOnAction(event -> {
                if (getItem() != null) {
                    onMove.accept(getItem(), "right");
                }
            });

            hbox.getChildren().addAll(indicator, taskLabel, leftButton, rightButton);
            HBox.setHgrow(taskLabel, javafx.scene.layout.Priority.ALWAYS);
            setGraphic(hbox);
            setFocusTraversable(false);
        }
        public void handleRightButtonClick() {
            Task task = getItem();
            if (task != null && onMove != null) {
                onMove.accept(task, "right");
            }
        }

        public static String getColorForStatus(String status) {
            return switch (status) {
                case "To Do" -> "#6495ED";
                case "In Progress" -> "#FFA500";
                case "Done" -> "#3CB371";
                default -> "blue";
            };
        }




    @Override
        public void updateItem(Task item, boolean empty) {
            super.updateItem(item, empty);
            setFocusTraversable(false);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                taskLabel.setText(item.getName());
                setGraphic(hbox);

                switch (item.getStatus()) {
                    case "To Do":
                        indicator.setStyle("-fx-fill: #6495ED;");
                        break;
                    case "In Progress":
                        indicator.setStyle("-fx-fill: #FFA500;");
                        break;
                    case "Done":
                        indicator.setStyle("-fx-fill: #3CB371;");
                        break;
                    default:
                        indicator.setStyle("-fx-fill: blue;");
                        break;
                }
            }
        }
    }

    public static class SubtaskCell extends ListCell<Pair<Subtask, String>> {
        protected HBox hbox;
        public Label subtaskLabel;
        public Button leftButton;
        public Button rightButton;
        protected final BiConsumer<Pair<Subtask, String>, String> onMove;
        public Circle indicator;

        public SubtaskCell(BiConsumer<Pair<Subtask, String>, String> onMove) {
            super();
            this.onMove = onMove;
            hbox = new HBox(10);
            hbox.setFocusTraversable(false);

            indicator = new Circle(6);
            indicator.getStyleClass().add("indicator");
            indicator.setFocusTraversable(false);

            subtaskLabel = new Label();
            subtaskLabel.setFocusTraversable(false);

            leftButton = new Button("<");
            rightButton = new Button(">");
            leftButton.getStyleClass().add("button");
            rightButton.getStyleClass().add("button");
            leftButton.setFocusTraversable(false);
            rightButton.setFocusTraversable(false);

            leftButton.setOnAction(event -> {
                if (getItem() != null) {
                    onMove.accept(getItem(), "left");
                }
            });

            rightButton.setOnAction(event -> {
                if (getItem() != null) {
                    onMove.accept(getItem(), "right");
                }
            });

            hbox.getChildren().addAll(indicator, subtaskLabel, new Label(" "), leftButton, rightButton);
            HBox.setHgrow(subtaskLabel, javafx.scene.layout.Priority.ALWAYS);
            setGraphic(hbox);
            setFocusTraversable(false);
        }

        @Override
        public void updateItem(Pair<Subtask, String> item, boolean empty) {
            super.updateItem(item, empty);
            setFocusTraversable(false);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                subtaskLabel.setText(item.getKey().getDescription() + " (Zadanie: " + item.getValue() + ")");
                setGraphic(hbox);

                if (item.getKey().isDone()) {
                    indicator.setStyle("-fx-fill: #3CB371;");
                } else {
                    indicator.setStyle("-fx-fill: #6495ED;");
                }
            }
        }
    }

    public void loadTasksAndSubtasksFromDatabase() {
        if (loggedInUserId == null) {
            System.err.println("Nie można załadować danych, brak ID użytkownika.");
            return;
        }

        todoTasks.clear();
        inProgressTasks.clear();
        doneTasks.clear();
        todoSubtasks.clear();
        doneSubtasks.clear();

        String taskQuery = "SELECT t.id, t.name, t.status FROM task t " +
                "INNER JOIN task_assignment ta ON t.id = ta.task_id " +
                "WHERE ta.user_id = ? AND t.status IN ('To Do', 'In Progress', 'Done')";

        String subtaskQuery = "SELECT s.id, s.task_id, s.name, s.description, s.weight, s.is_done, s.priority, s.due_date, t.name AS task_name " +
                "FROM subtask s JOIN task t ON s.task_id = t.id " +
                "INNER JOIN task_assignment ta ON t.id = ta.task_id " +
                "WHERE ta.user_id = ?";
        try (Connection conn = DatabaseModel.connect();
             PreparedStatement taskStmt = conn.prepareStatement(taskQuery);
             PreparedStatement subtaskStmt = conn.prepareStatement(subtaskQuery)) {

            taskStmt.setInt(1, loggedInUserId);
            ResultSet taskRs = taskStmt.executeQuery();
            while (taskRs.next()) {
                Task task = new Task();
                task.setId(taskRs.getInt("id"));
                task.setName(taskRs.getString("name"));
                task.setStatus(taskRs.getString("status"));
                switch (task.getStatus()) {
                    case "To Do":
                        todoTasks.add(task);
                        break;
                    case "In Progress":
                        inProgressTasks.add(task);
                        break;
                    case "Done":
                        doneTasks.add(task);
                        break;
                }
            }

            subtaskStmt.setInt(1, loggedInUserId);
            ResultSet subtaskRs = subtaskStmt.executeQuery();
            while (subtaskRs.next()) {
                Subtask subtask = new Subtask();
                subtask.setId(subtaskRs.getInt("id"));
                subtask.setTaskId(subtaskRs.getInt("task_id"));
                subtask.setName(subtaskRs.getString("name"));
                subtask.setDescription(subtaskRs.getString("description"));
                subtask.setWeight(subtaskRs.getInt("weight"));
                subtask.setDone(subtaskRs.getBoolean("is_done"));
                subtask.setPriority(subtaskRs.getShort("priority"));
                subtask.setDueDate(subtaskRs.getDate("due_date"));
                subtask.setTaskName(subtaskRs.getString("task_name"));

                String subtaskStatus = subtask.isDone() ? "Done" : "To Do";
                Pair<Subtask, String> subtaskPair = new Pair<>(subtask, subtask.getTaskName());
                if ("To Do".equals(subtaskStatus)) {
                    todoSubtasks.add(subtaskPair);
                } else if ("Done".equals(subtaskStatus)) {
                    doneSubtasks.add(subtaskPair);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //przenoszenie miedzy statusami w tablicy
    //obiekt task
    //string direction w która strone
    public void moveTask(Task task, String direction) {
        //aktualna wartość statusu zadania
        String oldStatus = task.getStatus();
        String newStatus = null;

        if (direction.equals("right")) {
            if (todoTasks.contains(task)) {
                todoTasks.remove(task);
                inProgressTasks.add(task);
                newStatus = "In Progress";
                task.setStatus(newStatus);
            } else if (inProgressTasks.contains(task)) {
                inProgressTasks.remove(task);
                doneTasks.add(task);
                newStatus = "Done";
                task.setStatus(newStatus);
            }
        } else if (direction.equals("left")) {
            if (inProgressTasks.contains(task)) {
                inProgressTasks.remove(task);
                todoTasks.add(task);
                newStatus = "To Do";
                task.setStatus(newStatus);
            } else if (doneTasks.contains(task)) {
                doneTasks.remove(task);
                inProgressTasks.add(task);
                newStatus = "In Progress";
                task.setStatus(newStatus);
            }
        }

        if (newStatus != null) {
            updateTaskStatusInDatabase(task.getId(), newStatus);
            showNotification(String.format("Zadanie '%s' przeniesiono z '%s' do '%s'", task.getName(), przetlumaczStatus(oldStatus), przetlumaczStatus(newStatus)));
        }
    }
    //służy do tłumaczenia statusów zadań,kóre są wyśwetlane w dla użytkownika(dashboard i powiadomienia)
    public String przetlumaczStatus(String status) {
        return switch (status) {
            case "To Do" -> "Do zrobienia";
            case "In Progress" -> "W trakcie";
            case "Done" -> "Zrobione";
            default -> status;
        };
    }

    public void updateTaskStatusInDatabase(int taskId, String newStatus) {
        String query = "UPDATE task SET status = CAST(? AS task_status_enum) WHERE id = ?";
        try (Connection conn = DatabaseModel.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
//przeniesienie cząsteczek
public void moveSubtask(Pair<Subtask, String> subtaskPair, String direction) {
    Subtask subtask = subtaskPair.getKey();
    String taskName = subtaskPair.getValue();
    boolean oldIsDone = subtask.isDone();
    boolean newIsDone = oldIsDone;
    String oldStatus = przetlumaczStatusPodzadania(oldIsDone);
    String newStatus = przetlumaczStatusPodzadania(newIsDone);

    if ("right".equals(direction)) {
        if (!oldIsDone) {
            newIsDone = true;
            newStatus = przetlumaczStatusPodzadania(true);

            todoSubtasks.remove(subtaskPair);
            doneSubtasks.add(new Pair<>(subtask, taskName));
        }
    } else if ("left".equals(direction)) {
        if (oldIsDone) {
            newIsDone = false;
            newStatus = przetlumaczStatusPodzadania(false);

            doneSubtasks.remove(subtaskPair);
            todoSubtasks.add(new Pair<>(subtask, taskName));
        }
    }

    if (newIsDone != oldIsDone) {
        subtask.setDone(newIsDone);
        updateSubtaskIsDoneInDatabase(subtask.getId(), newIsDone);
        showNotification(String.format("Podzadanie '%s' zadania '%s' przeniesiono z '%s' do '%s'",
                subtask.getDescription(), taskName, oldStatus, newStatus));
    }
}

    //przy generowaniu powiadomień
    public String przetlumaczStatusPodzadania(boolean isDone) {
        return isDone ? "Zrobione" : "Do zrobienia";
    }

    public void updateSubtaskIsDoneInDatabase(int subtaskId, boolean isDone) {
        String query = "UPDATE subtask SET is_done = ? WHERE id = ?";
        try (Connection conn = DatabaseModel.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, isDone);
            stmt.setInt(2, subtaskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void showNotification(String message) {
        System.out.println("Powiadomienie: " + message);
        if (loggedInUserId != null) {
            saveNotificationToDatabase(loggedInUserId, message);
            if (notificationsController != null) {
                notificationsController.refreshNotifications();
            }
        }
    }

    public void saveNotificationToDatabase(int userId, String content) {
        String query = "INSERT INTO notification (user_id, content, type, created_at, is_read) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseModel.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, content);
            pstmt.setString(3, "update"); // Zmieniono na 'update'
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setBoolean(5, false);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}