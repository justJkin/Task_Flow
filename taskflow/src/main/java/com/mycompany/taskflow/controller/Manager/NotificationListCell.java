package com.mycompany.taskflow.controller.Manager;

import com.mycompany.taskflow.model.Manager.Notification;
import javafx.scene.control.ListCell;

public class NotificationListCell extends ListCell<Notification> {
    @Override
    protected void updateItem(Notification notification, boolean empty) {
        super.updateItem(notification, empty);
        if (empty || notification == null) {
            setText(null);
        } else {
            setText(notification.getContent());
            // You can add more formatting here if needed
        }
    }
}