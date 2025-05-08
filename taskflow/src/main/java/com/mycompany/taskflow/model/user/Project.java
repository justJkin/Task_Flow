package com.mycompany.taskflow.model.user;

import com.mycompany.taskflow.model.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Project {
    private int id;
    private String name;
    private String description;
    private int totalWeight;
    private ProjectStatus status; // Użycie enum
    private LocalDate startDate;
    private LocalDate endDate;
    private User admin; // Relacja ManyToOne
    private LocalDateTime createdAt;
    private short progress;

    public Project() {
    }

    public Project(int id, String name, String description, int totalWeight, ProjectStatus status, LocalDate startDate, LocalDate endDate, User admin, LocalDateTime createdAt, short progress) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.totalWeight = totalWeight;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.admin = admin;
        this.createdAt = createdAt;
        this.progress = progress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(int totalWeight) {
        this.totalWeight = totalWeight;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public short getProgress() {
        return progress;
    }

    public void setProgress(short progress) {
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", totalWeight=" + totalWeight +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", admin=" + admin +
                ", createdAt=" + createdAt +
                ", progress=" + progress +
                '}';
    }
}