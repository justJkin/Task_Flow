package com.mycompany.taskflow.model.User;

import com.mycompany.taskflow.model.user.Notification;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void testDefaultConstructor() {
        Notification notification = new Notification();

        assertNotNull(notification);
        assertEquals(0, notification.getId());
        assertEquals(0, notification.getUserId());
        assertNull(notification.getTaskId());
        assertNull(notification.getSubtaskId());
        assertNull(notification.getType());
        assertNull(notification.getContent());
        assertFalse(notification.isRead());
        assertNull(notification.getCreatedAt());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Notification notification = new Notification(
                1, 2, 3, 4, "REMINDER", "Test content", true, now
        );

        assertEquals(1, notification.getId());
        assertEquals(2, notification.getUserId());
        assertEquals(3, notification.getTaskId());
        assertEquals(4, notification.getSubtaskId());
        assertEquals("REMINDER", notification.getType());
        assertEquals("Test content", notification.getContent());
        assertTrue(notification.isRead());
        assertEquals(now, notification.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        Notification notification = new Notification();

        notification.setId(5);
        notification.setUserId(6);
        notification.setTaskId(7);
        notification.setSubtaskId(8);
        notification.setType("ALERT");
        notification.setContent("New content");
        notification.setRead(true);
        notification.setCreatedAt(now);

        assertEquals(5, notification.getId());
        assertEquals(6, notification.getUserId());
        assertEquals(7, notification.getTaskId());
        assertEquals(8, notification.getSubtaskId());
        assertEquals("ALERT", notification.getType());
        assertEquals("New content", notification.getContent());
        assertTrue(notification.isRead());
        assertEquals(now, notification.getCreatedAt());
    }

    @Test
    void testNullValues() {
        Notification notification = new Notification();

        notification.setTaskId(null);
        notification.setSubtaskId(null);
        notification.setType(null);
        notification.setContent(null);
        notification.setCreatedAt(null);

        assertNull(notification.getTaskId());
        assertNull(notification.getSubtaskId());
        assertNull(notification.getType());
        assertNull(notification.getContent());
        assertNull(notification.getCreatedAt());
    }

    @Test
    void testToString() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        Notification notification = new Notification(
                1, 2, 3, 4, "REMINDER", "Test", false, dateTime
        );

        String expected = "Notification{id=1, userId=2, taskId=3, subtaskId=4, " +
                "type='REMINDER', content='Test', isRead=false, " +
                "createdAt=2023-01-01T12:00}";

        assertEquals(expected, notification.toString());
    }

    @Test
    void testEdgeCases() {
        Notification notification = new Notification();

        notification.setId(Integer.MIN_VALUE);
        notification.setUserId(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, notification.getId());
        assertEquals(Integer.MIN_VALUE, notification.getUserId());

        notification.setId(Integer.MAX_VALUE);
        notification.setUserId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, notification.getId());
        assertEquals(Integer.MAX_VALUE, notification.getUserId());

        notification.setType("");
        notification.setContent("");
        assertEquals("", notification.getType());
        assertEquals("", notification.getContent());
    }
}