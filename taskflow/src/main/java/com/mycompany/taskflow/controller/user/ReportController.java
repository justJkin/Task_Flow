package com.mycompany.taskflow.controller.user;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class ReportController {

    @FXML
    private ComboBox<String> timeRangeComboBox;
    @FXML
    private ComboBox<String> projectFilterComboBox;
    @FXML
    private ComboBox<String> priorityFilterComboBox;
    @FXML
    private Label summaryLabel;
    @FXML
    private ListView<String> tasksListView;
    @FXML
    private Button exportToPDF;
    @FXML
    private Button exportToExcel;

    private ObservableList<String> tasks = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        timeRangeComboBox.getItems().addAll("Ostatnie 7 dni", "Ten miesiąc", "Niestandardowy");
        projectFilterComboBox.getItems().addAll("Wszystkie projekty", "Projekt X", "Projekt Y");
        priorityFilterComboBox.getItems().addAll("Wszystkie priorytety", "Wysoki", "Średni", "Niski");

        tasksListView.setItems(tasks);
    }

    @FXML
    public void generateReport() {
        // Logika generowania raportu na podstawie wybranych filtrów
        String timeRange = timeRangeComboBox.getValue();
        String projectFilter = projectFilterComboBox.getValue();
        String priorityFilter = priorityFilterComboBox.getValue();

        // Przykładowe dane podsumowania
        summaryLabel.setText("Zakończone zadania: 15/20 (75%)\nŚredni czas na zadanie: 3h 20min");

        // Przykładowe zadania
        tasks.clear();
        tasks.addAll("Naprawa bugu logowania - Aplikacja X - ✅", "Testy API - System Y - ❌", "Nowa funkcja - Aplikacja Z - ✅");
    }

    @FXML
    public void exportToPDF() {
        // Logika eksportu raportu do PDF
        System.out.println("Eksport do PDF");
    }

    @FXML
    public void exportToExcel() {
        // Logika eksportu raportu do Excel
        System.out.println("Eksport do Excel");
    }
}