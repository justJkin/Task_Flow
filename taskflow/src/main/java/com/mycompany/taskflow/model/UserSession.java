package com.mycompany.taskflow.model;

public class UserSession {

    private static UserSession instance;
    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private Integer teamId;

    private UserSession(Integer userId, String firstName, String lastName, String email, String role, Integer teamId) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.teamId = teamId;
    }

    public static UserSession getInstance(Integer userId, String firstName, String lastName, String email, String role, Integer teamId) {
        if (instance == null) {
            instance = new UserSession(userId, firstName, lastName, email, role, teamId);
        } else {
            // Jeśli sesja już istnieje, aktualizujemy jej dane (opcjonalne, zależy od potrzeb)
            instance.userId = userId;
            instance.firstName = firstName;
            instance.lastName = lastName;
            instance.email = email;
            instance.role = role;
            instance.teamId = teamId;
        }
        return instance;
    }

    public static UserSession getInstance() {
        return instance;
    }

    public static void clearSession() {
        instance = null;
    }

    public static void setInstance(Object o) {

    }

    public Integer getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setFirstName(String testowy) {
    }

    public void setUserId(int i) {
    }

}