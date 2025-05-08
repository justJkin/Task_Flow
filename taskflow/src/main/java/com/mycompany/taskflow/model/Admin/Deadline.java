package com.mycompany.taskflow.model.Admin;

import java.time.LocalDate;
import java.util.List;

public class Deadline {
    private String name;
    private LocalDate deadline;
    private String type;
    private String user;
    private List<String> team;

    public Deadline(String name, LocalDate deadline, String type, String user, List<String> team) {
        this.name = name;
        this.deadline = deadline;
        this.type = type;
        this.user = user;
        this.team = team;
    }

    public String getName() { return name; }
    public LocalDate getDeadline() { return deadline; }
    public String getType() { return type; }
    public String getUser() { return user; }
    public List<String> getTeam() { return team; }
}