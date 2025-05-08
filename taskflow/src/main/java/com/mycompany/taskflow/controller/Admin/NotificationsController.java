package com.mycompany.taskflow.controller.Admin;
import com.mycompany.taskflow.model.Admin.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class NotificationsController {

    @FXML
    private ListView<NotificationWrapper> latestNotificationsListView;
    @FXML
    private ListView<NotificationWrapper> unreadNotificationsListView;
    @FXML
    private ListView<NotificationWrapper> readNotificationsListView;
    @FXML
    private Button markAsReadButton;
    @FXML
    private Button deleteSelectedButton;

    private ObservableList<NotificationWrapper> allNotifications = FXCollections.observableArrayList();
    private ObservableList<NotificationWrapper> latestNotifications = FXCollections.observableArrayList();
    private ObservableList<NotificationWrapper> unreadNotifications = FXCollections.observableArrayList();
    private ObservableList<NotificationWrapper> readNotifications = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadNotificationsFromDatabase();

        // Sortowanie najnowszych powiadomień po dacie (od najnowszych)
        allNotifications.sort(Comparator.comparing((NotificationWrapper n) -> n.getNotification().getCreatedAt()).reversed());
        latestNotifications.addAll(allNotifications.subList(0, Math.min(5, allNotifications.size()))); // Wyświetl 5 najnowszych

        // Filtrowanie powiadomień do odpowiednich list
        updateNotificationLists();

        latestNotificationsListView.setItems(latestNotifications);
        unreadNotificationsListView.setItems(unreadNotifications);
        readNotificationsListView.setItems(readNotifications);

        latestNotificationsListView.setCellFactory(param -> new NotificationCell(this::markNotificationAsRead));
        unreadNotificationsListView.setCellFactory(param -> new NotificationCell(this::markNotificationAsRead));
        readNotificationsListView.setCellFactory(param -> new NotificationCell(this::markNotificationAsRead));

        // Wyłączenie przycisków na początku
        markAsReadButton.setDisable(true);
        deleteSelectedButton.setDisable(true);

        // Nasłuchiwanie na zmiany zaznaczenia (teraz na wszystkich listach)
        latestNotificationsListView.getSelectionModel().getSelectedIndices().addListener((javafx.collections.ListChangeListener.Change<? extends Integer> c) -> updateButtonStates());
        unreadNotificationsListView.getSelectionModel().getSelectedIndices().addListener((javafx.collections.ListChangeListener.Change<? extends Integer> c) -> updateButtonStates());
        readNotificationsListView.getSelectionModel().getSelectedIndices().addListener((javafx.collections.ListChangeListener.Change<? extends Integer> c) -> updateButtonStates());
    }

    private void loadNotificationsFromDatabase() {
        try {
            List<Notification> notifications = Notification.getAllNotifications();
            for (Notification notification : notifications) {
                allNotifications.add(new NotificationWrapper(notification));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Obsługa błędu ładowania powiadomień
        }
    }

    private void updateButtonStates() {
        boolean anySelectedLatest = !latestNotificationsListView.getSelectionModel().getSelectedIndices().isEmpty();
        boolean anySelectedUnread = !unreadNotificationsListView.getSelectionModel().getSelectedIndices().isEmpty();
        boolean anySelectedRead = !readNotificationsListView.getSelectionModel().getSelectedIndices().isEmpty();
        markAsReadButton.setDisable(!anySelectedUnread); // Tylko nieodczytane można oznaczyć jako przeczytane
        deleteSelectedButton.setDisable(false); // Można usuwać z każdej sekcji, więc zawsze włączony, gdy coś jest zaznaczone
        if (!anySelectedLatest && !anySelectedUnread && !anySelectedRead) {
            deleteSelectedButton.setDisable(true);
        }
    }

    @FXML
    public void markSelectedAsRead() {
        unreadNotificationsListView.getSelectionModel().getSelectedIndices().forEach(index -> {
            NotificationWrapper wrapper = unreadNotifications.get(index);
            Notification notification = wrapper.getNotification();
            if (!notification.isRead()) {
                try {
                    Notification.markAsRead(notification.getId());
                    notification.setRead(true);
                    wrapper.readProperty().set(true); // Aktualizacja property
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Obsługa błędu oznaczania jako przeczytane
                }
            }
        });
        updateNotificationLists();
    }

    public void markNotificationAsRead(NotificationWrapper wrapper) {
        Notification notification = wrapper.getNotification();
        if (!notification.isRead()) {
            try {
                Notification.markAsRead(notification.getId());
                notification.setRead(true);
                wrapper.readProperty().set(true); // Aktualizacja property
                updateNotificationLists();
            } catch (SQLException e) {
                e.printStackTrace();
                // Obsługa błędu oznaczania jako przeczytane
            }
        }
    }

    @FXML
    public void deleteSelected() {
        ObservableList<Integer> selectedLatest = latestNotificationsListView.getSelectionModel().getSelectedIndices();
        ObservableList<Integer> selectedUnread = unreadNotificationsListView.getSelectionModel().getSelectedIndices();
        ObservableList<Integer> selectedRead = readNotificationsListView.getSelectionModel().getSelectedIndices();

        // Usuwanie z najnowszych
        for (int i = selectedLatest.size() - 1; i >= 0; i--) {
            NotificationWrapper toRemoveWrapper = latestNotifications.get((int) selectedLatest.get(i));
            try {
                Notification.deleteNotification(toRemoveWrapper.getNotification().getId());
            } catch (SQLException e) {
                e.printStackTrace();
                // Obsługa błędu usuwania
            }
            latestNotifications.remove((int) selectedLatest.get(i));
            allNotifications.remove(toRemoveWrapper);
        }
        // Usuwanie z nieodczytanych
        for (int i = selectedUnread.size() - 1; i >= 0; i--) {
            NotificationWrapper toRemoveWrapper = unreadNotifications.get((int) selectedUnread.get(i));
            try {
                Notification.deleteNotification(toRemoveWrapper.getNotification().getId());
            } catch (SQLException e) {
                e.printStackTrace();
                // Obsługa błędu usuwania
            }
            unreadNotifications.remove(toRemoveWrapper);
            allNotifications.remove(toRemoveWrapper);
        }
        // Usuwanie z odczytanych
        for (int i = selectedRead.size() - 1; i >= 0; i--) {
            NotificationWrapper toRemoveWrapper = readNotifications.get((int) selectedRead.get(i));
            try {
                Notification.deleteNotification(toRemoveWrapper.getNotification().getId());
            } catch (SQLException e) {
                e.printStackTrace();
                // Obsługa błędu usuwania
            }
            readNotifications.remove(toRemoveWrapper);
            allNotifications.remove(toRemoveWrapper);
        }
        updateLatestNotifications();
        updateButtonStates();
    }

    private void updateNotificationLists() {
        unreadNotifications.setAll(allNotifications.filtered(n -> !n.getNotification().isRead()));
        readNotifications.setAll(allNotifications.filtered(n -> n.getNotification().isRead()));
        // Sortowanie od najnowszych w każdej sekcji
        unreadNotifications.sort(Comparator.comparing((NotificationWrapper n) -> n.getNotification().getCreatedAt()).reversed());
        readNotifications.sort(Comparator.comparing((NotificationWrapper n) -> n.getNotification().getCreatedAt()).reversed());
        latestNotifications.sort(Comparator.comparing((NotificationWrapper n) -> n.getNotification().getCreatedAt()).reversed());
    }

    private void updateLatestNotifications() {
        allNotifications.sort(Comparator.comparing((NotificationWrapper n) -> n.getNotification().getCreatedAt()).reversed());
        latestNotifications.setAll(allNotifications.subList(0, Math.min(5, allNotifications.size())));
    }

    // Wrapper dla powiadomienia, aby móc bindować readProperty w komórce
    public static class NotificationWrapper {
        private final Notification notification;
        private final BooleanProperty readProperty;

        public NotificationWrapper(Notification notification) {
            this.notification = notification;
            this.readProperty = new SimpleBooleanProperty(notification.isRead());
            this.readProperty.addListener((obs, oldVal, newVal) -> notification.setRead(newVal));
        }

        public Notification getNotification() { // Zmień na public
            return notification;
        }

        public BooleanProperty readProperty() {
            return readProperty;
        }
    }

    // Klasa komórki do wyświetlania powiadomień
    private static class NotificationCell extends ListCell<NotificationWrapper> {
        private HBox hbox;
        private Label messageLabel;
        private final java.util.function.Consumer<NotificationWrapper> onReadChanged;

        public NotificationCell(java.util.function.Consumer<NotificationWrapper> onReadChanged) {
            super();
            this.onReadChanged = onReadChanged;
            hbox = new HBox(10);
            messageLabel = new Label();
            hbox.getChildren().addAll(messageLabel);
            HBox.setHgrow(messageLabel, javafx.scene.layout.Priority.ALWAYS);

            setOnMouseClicked(event -> {
                NotificationWrapper item = getItem();
                if (item != null && !item.getNotification().isRead()) {
                    onReadChanged.accept(item);
                    try {
                        Notification.markAsRead(item.getNotification().getId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                        // Obsługa błędu oznaczania jako przeczytane
                    }
                    updateItem(item, false); // Aktualizacja wizualna natychmiast
                }
            });
        }

        @Override
        protected void updateItem(NotificationWrapper item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                messageLabel.setText(item.getNotification().getContent());
                setGraphic(hbox);
                setStyle(item.readProperty().get() ? "-fx-opacity: 0.6;" : "");
            }
        }
    }
}