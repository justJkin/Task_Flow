package com.mycompany.taskflow.model.Manager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MilestoneWrapper {

    private final SimpleStringProperty name;
    private final SimpleIntegerProperty weight;

    public MilestoneWrapper(String name, int weight) {
        this.name = new SimpleStringProperty(name);
        this.weight = new SimpleIntegerProperty(weight);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public int getWeight() {
        return weight.get();
    }

    public SimpleIntegerProperty weightProperty() {
        return weight;
    }
}