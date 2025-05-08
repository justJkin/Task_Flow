package com.mycompany.taskflow.controller.user;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.UserSession;
import com.mycompany.taskflow.model.user.Notification;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class NotificationsController {

    @FXML
    public ListView<Notification> latestNotificationsListView;
    @FXML
    public ListView<Notification> unreadNotificationsListView;
    @FXML
    public ListView<Notification> readNotificationsListView;
    @FXML
    public Button markAsReadButton;

    public ObservableList<Notification> allNotifications = FXCollections.observableArrayList();
    public ObservableList<Notification> latestNotifications = FXCollections.observableArrayList();
    public ObservableList<Notification> unreadNotifications = FXCollections.observableArrayList();
    public ObservableList<Notification> readNotifications = FXCollections.observableArrayList();

    public Integer userId;

    @FXML
    public void initialize() {
        loadUserDataFromSession();
        setupNotificationLists();
        setupCellFactories();
        setupButtonListener();
    }

    public void loadUserDataFromSession() {
        UserSession currentSession = UserSession.getInstance();
        if (currentSession != null) {
            this.userId = currentSession.getUserId();
            refreshNotifications();
        } else {
            System.err.println("NotificationsController: Brak aktywnej sesji użytkownika.");
            latestNotifications.clear();
            unreadNotifications.clear();
            readNotifications.clear();
        }
    }

    public void refreshNotifications() {
        if (userId != null) {
            loadNotificationsFromDatabase(userId);
            updateNotificationLists();
        } else {
            System.err.println("NotificationsController: Nie można odświeżyć powiadomień - brak ID użytkownika.");
        }
    }

    public void loadNotificationsFromDatabase(int userId) {
        allNotifications.clear();
        String query = "SELECT id, user_id, task_id, subtask_id, type, content, is_read, created_at " +
                "FROM notification WHERE user_id = ? " +
                "ORDER BY created_at DESC";
        try (Connection conn = DatabaseModel.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                allNotifications.add(new Notification(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getObject("task_id", Integer.class),
                        rs.getObject("subtask_id", Integer.class),
                        rs.getString("type"),
                        rs.getString("content"),
                        rs.getBoolean("is_read"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateNotificationLists() {
        unreadNotifications.setAll(allNotifications.stream().filter(n -> !n.isRead()).collect(Collectors.toList()));
        readNotifications.setAll(allNotifications.stream().filter(Notification::isRead).collect(Collectors.toList()));
        latestNotifications.setAll(allNotifications.stream().limit(10).collect(Collectors.toList()));
    }

    public void setupNotificationLists() {
        latestNotificationsListView.setItems(latestNotifications);
        unreadNotificationsListView.setItems(unreadNotifications);
        readNotificationsListView.setItems(readNotifications);
    }

    public void setupCellFactories() {
        unreadNotificationsListView.setCellFactory(param -> new NotificationCell(this::markNotificationAsRead));
        readNotificationsListView.setCellFactory(param -> new NotificationCell(null)); // Nie reaguj na kliknięcie przeczytanych
        latestNotificationsListView.setCellFactory(param -> new NotificationCell(this::markNotificationAsRead));
    }

    public void setupButtonListener() {
        markAsReadButton.setOnAction(event -> markSelectedAsRead());
        unreadNotificationsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            markAsReadButton.setDisable(newSelection == null);
        });
    }

    @FXML
    public void markSelectedAsRead() {
        Notification selected = unreadNotificationsListView.getSelectionModel().getSelectedItem();
        if (selected != null && userId != null) {
            markNotificationAsRead(selected);
        } else if (userId == null) {
            System.err.println("NotificationsController: Nie można oznaczyć jako przeczytane - brak ID użytkownika.");
        }
    }

    public void markNotificationAsRead(Notification notification) {
        if (userId != null && !notification.isRead()) {
            try (Connection conn = DatabaseModel.connect();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "UPDATE notification SET is_read = true WHERE id = ?")) {
                pstmt.setInt(1, notification.getId());
                pstmt.executeUpdate();
                // Nie trzeba ręcznie ustawiać notification.setRead(true), bo refreshNotifications() zaktualizuje listy
                refreshNotifications();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (userId == null) {
            System.err.println("NotificationsController: Nie można oznaczyć powiadomienia jako przeczytanego - brak ID użytkownika.");
            // Możesz tutaj wyświetlić komunikat użytkownikowi
        }
    }

    public static class NotificationCell extends ListCell<Notification> {
        public final HBox hbox = new HBox(10);
        public final Label messageLabel = new Label();
        public final java.util.function.Consumer<Notification> onReadChanged;

        public NotificationCell(java.util.function.Consumer<Notification> onReadChanged) {
            this.onReadChanged = onReadChanged;
            hbox.getChildren().add(messageLabel);
            HBox.setHgrow(messageLabel, javafx.scene.layout.Priority.ALWAYS);
            if (this.onReadChanged != null) {
                setOnMouseClicked(event -> {
                    if (!isEmpty() && !getItem().isRead()) {
                        this.onReadChanged.accept(getItem());
                    }
                });
            }
        }

        @Override
        public void updateItem(Notification item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                messageLabel.setText(item.getContent());
                setGraphic(hbox);
                setStyle(item.isRead() ? "-fx-opacity: 0.6;" : "-fx-font-weight: bold;");
            }
        }
    }
}