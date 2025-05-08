package com.mycompany.taskflow.controller.Manager;

import com.mycompany.taskflow.model.DatabaseModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RaportyViewController {

    @FXML
    private Label statystykiLabel;

    @FXML
    private ListView<String> rankingListView;

    @FXML
    private Label raportowanieLabel;

    public void initialize() {
        wyswietlStatystyki();
        generujRanking();
        eksportujRaport();
    }

    public void wyswietlStatystyki() {
        statystykiLabel.setText(pobierzStatystykiZBazy());
    }

    public void generujRanking() {
        rankingListView.getItems().clear();
        List<String> ranking = pobierzRankingUzytkownikow();
        rankingListView.getItems().addAll(ranking);
    }

    public void eksportujRaport() {
        raportowanieLabel.setText(generujTekstRaportu());
    }

    private String pobierzStatystykiZBazy() {
        String statystyki = "Błąd pobierania statystyk.";
        try (Connection conn = DatabaseModel.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM task WHERE status = 'Done'")) {
            if (rs.next()) {
                int liczbaZakonczonychZadan = rs.getInt(1);
                statystyki = "Liczba zakończonych zadań: " + liczbaZakonczonychZadan;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statystyki;
    }

    private List<String> pobierzRankingUzytkownikow() {
        List<String> ranking = new ArrayList<>();
        try (Connection conn = DatabaseModel.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT u.first_name, u.last_name, COUNT(t.id) AS completed_tasks " +
                     "FROM \"user\" u LEFT JOIN task t ON t.id = u.id " +
                     "WHERE t.status = 'Done' GROUP BY u.id ORDER BY COUNT(t.id) DESC")) {
            while (rs.next()) {
                String pozycja = rs.getString("first_name") + " " + rs.getString("last_name") +
                        " - Ukończone zadania: " + rs.getInt("completed_tasks");
                ranking.add(pozycja);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ranking;
    }

    private String generujTekstRaportu() {
        return "Tutaj znajdzie się logika generowania tekstu raportu do eksportu.";
        // Możesz tutaj dodać pobieranie danych i formatowanie ich do raportu
    }
}