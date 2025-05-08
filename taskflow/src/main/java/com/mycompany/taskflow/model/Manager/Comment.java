package com.mycompany.taskflow.model.Manager;

import com.mycompany.taskflow.model.Manager.*;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private Task task; // Relacja ManyToOne
    private Subtask subtask; // Relacja ManyToOne
    private User user; // Relacja ManyToOne
    private String content;
    private LocalDateTime createdAt;

    // Konstruktory, Gettery i Settery
    public Comment() {
    }

    public Comment(int id, Task task, Subtask subtask, User user, String content, LocalDateTime createdAt) {
        this.id = id;
        this.task = task;
        this.subtask = subtask;
        this.user = user;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Subtask getSubtask() {
        return subtask;
    }

    public void setSubtask(Subtask subtask) {
        this.subtask = subtask;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", task=" + task +
                ", subtask=" + subtask +
                ", user=" + user +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}