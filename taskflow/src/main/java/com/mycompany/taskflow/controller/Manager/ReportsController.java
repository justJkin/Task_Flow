package com.mycompany.taskflow.controller.Manager;

import com.mycompany.taskflow.model.DatabaseModel;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReportsController {

    @FXML
    private ListView<String> rankingListView;

    public void initialize() {
        generateRanking(); // Wywołanie generowania rankingu przy inicjalizacji
    }

    public void generateRanking() {
        rankingListView.getItems().clear();
        List<String> ranking = pobierzRankingUzytkownikow();
        rankingListView.getItems().addAll(ranking);
    }

    private List<String> pobierzRankingUzytkownikow() {
        List<String> ranking = new ArrayList<>();
        try (Connection conn = DatabaseModel.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT u.first_name, u.last_name, COUNT(t.id) AS completed_tasks " +
                             "FROM \"user\" u LEFT JOIN task t ON t.id = u.id " +
                             "WHERE t.status = 'Done' GROUP BY u.id ORDER BY COUNT(t.id) DESC"
             )) {
            while (rs.next()) {
                String pozycja = rs.getString("first_name") + " " + rs.getString("last_name") +
                        " - Ukończone zadania: " + rs.getInt("completed_tasks");
                ranking.add(pozycja);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Obsługa błędów (np. wyświetlenie komunikatu dla użytkownika)
        }
        return ranking;
    }

    // Metody do generowania statystyk i raportów (będą implementowane w przyszłości)
    // Poniżej tylko puste szkielety, aby zachować spójność

    public void generateStatistics() {
        System.out.println("Generowanie statystyk...");
        // Tutaj dodaj logikę do obliczania i wyświetlania statystyk
    }

    public void generateReport() {
        System.out.println("Generowanie raportu...");
        // Tutaj dodaj logikę do generowania różnych raportów
    }
}