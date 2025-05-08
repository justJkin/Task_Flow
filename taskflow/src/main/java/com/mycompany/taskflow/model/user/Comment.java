package com.mycompany.taskflow.model.user;

import com.mycompany.taskflow.model.Manager.Subtask;
import com.mycompany.taskflow.model.Manager.Task;
import com.mycompany.taskflow.model.Manager.User;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private com.mycompany.taskflow.model.Manager.Task task; // Relacja ManyToOne
    private com.mycompany.taskflow.model.Manager.Subtask subtask; // Relacja ManyToOne
    private User user; // Relacja ManyToOne
    private String content;
    private LocalDateTime createdAt;


    public Comment() {
    }

    public Comment(int id, com.mycompany.taskflow.model.Manager.Task task, com.mycompany.taskflow.model.Manager.Subtask subtask, User user, String content, LocalDateTime createdAt) {
        this.id = id;
        this.task = task;
        this.subtask = subtask;
        this.user = user;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static Comment loadFromDatabase(int i) {
        return null;
    }

    public static boolean deleteFromDatabase(int i) {
        return true;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public com.mycompany.taskflow.model.Manager.Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public com.mycompany.taskflow.model.Manager.Subtask getSubtask() {
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

    public boolean saveToDatabase() {
        return true;
    }
}