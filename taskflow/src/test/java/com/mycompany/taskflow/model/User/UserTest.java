package com.mycompany.taskflow.model.User;

import com.mycompany.taskflow.model.Role;
import com.mycompany.taskflow.model.user.User;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testDefaultConstructor() {
        User user = new User();

        assertNotNull(user);
        assertEquals(0, user.getId());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getEmail());
        assertNull(user.getPasswordHash());
        assertNull(user.getRole());
        assertNull(user.getCreatedAt());
        assertNull(user.getLastLogin());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastLogin = now.minusDays(1);
        User user = new User(1, "John", "Doe", "john.doe@example.com",
                "hashed123", Role.USER, now, lastLogin);

        assertEquals(1, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("hashed123", user.getPasswordHash());
        assertEquals(Role.USER, user.getRole());
        assertEquals(now, user.getCreatedAt());
        assertEquals(lastLogin, user.getLastLogin());
    }

    @Test
    void testSettersAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastLogin = now.minusHours(2);
        User user = new User();

        user.setId(2);
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setEmail("jane.smith@example.com");
        user.setPasswordHash("hashed456");
        user.setRole(Role.ADMIN);
        user.setCreatedAt(now);
        user.setLastLogin(lastLogin);

        assertEquals(2, user.getId());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("jane.smith@example.com", user.getEmail());
        assertEquals("hashed456", user.getPasswordHash());
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals(now, user.getCreatedAt());
        assertEquals(lastLogin, user.getLastLogin());
    }

    @Test
    void testNullValues() {
        User user = new User();

        user.setFirstName(null);
        user.setLastName(null);
        user.setEmail(null);
        user.setPasswordHash(null);
        user.setRole(null);
        user.setCreatedAt(null);
        user.setLastLogin(null);

        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getEmail());
        assertNull(user.getPasswordHash());
        assertNull(user.getRole());
        assertNull(user.getCreatedAt());
        assertNull(user.getLastLogin());
    }

    @Test
    void testToString() {
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 12, 0);
        LocalDateTime lastLogin = LocalDateTime.of(2023, 1, 2, 10, 30);
        User user = new User(1, "John", "Doe", "john@example.com",
                "hash123", Role.MANAGER, createdAt, lastLogin);

        String expected = "User{id=1, firstName='John', lastName='Doe', " +
                "email='john@example.com', passwordHash='hash123', " +
                "role=MANAGER, createdAt=2023-01-01T12:00, " +
                "lastLogin=2023-01-02T10:30}";

        assertEquals(expected, user.toString());
    }

    @Test
    void testEdgeCases() {
        User user = new User();

        user.setId(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, user.getId());

        user.setId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, user.getId());

        user.setFirstName("");
        user.setLastName("");
        user.setEmail("");
        user.setPasswordHash("");
        assertEquals("", user.getFirstName());
        assertEquals("", user.getLastName());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPasswordHash());

        for (Role role : Role.values()) {
            user.setRole(role);
            assertEquals(role, user.getRole());
        }
    }

    @Test
    void testEmailValidationPattern() {
        User user = new User();

        String[] validEmails = {
                "test@example.com",
                "first.last@example.com",
                "user+tag@example.com",
                "user@sub.example.com"
        };

        for (String email : validEmails) {
            user.setEmail(email);
            assertEquals(email, user.getEmail());
        }
    }
}