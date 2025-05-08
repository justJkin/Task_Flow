package com.mycompany.taskflow.model.Manager;

import java.time.LocalDateTime;

public class TaskAssignment {
    private User user; // Relacja ManyToOne
    private Task task; // Relacja ManyToOne
    private LocalDateTime assignedAt;


    public TaskAssignment(User user, Task task, LocalDateTime assignedAt) {
        this.user = user;
        this.task = task;
        this.assignedAt = assignedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    @Override
    public String toString() {
        return "TaskAssignment{" +
                "user=" + user +
                ", task=" + task +
                ", assignedAt=" + assignedAt +
                '}';
    }
}