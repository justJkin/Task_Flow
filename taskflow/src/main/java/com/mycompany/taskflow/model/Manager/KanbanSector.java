package com.mycompany.taskflow.model.Manager;

import com.mycompany.taskflow.model.DatabaseModel; // Import klasy do połączenia z DB

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class KanbanSector {
    private int id;
    private String name;
    private short orderPosition;

    // Konstruktory, Gettery i Settery
    public KanbanSector() {
    }

    public KanbanSector(int id, String name, short orderPosition) {
        this.id = id;
        this.name = name;
        this.orderPosition = orderPosition;
    }

    // DODAJ TĘ METODĘ: Metoda statyczna pobierająca sektory Kanban z bazy danych
    public static List<KanbanSector> getKanbanSectorsFromDatabase() throws SQLException {
        List<KanbanSector> sectors = new ArrayList<>();
        String query = "SELECT id, name, order_position FROM kanban_sector ORDER BY order_position"; // Zapytanie pobierające sektory

        try (Connection connection = DatabaseModel.connect();
             Statement statement = connection.createStatement(); // Używamy Statement dla prostego zapytania bez parametrów
             ResultSet rs = statement.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                short orderPosition = rs.getShort("order_position");

                sectors.add(new KanbanSector(id, name, orderPosition));
            }
        } // Obsługa SQLException (zgodnie z modelem, można ją obsłużyć w metodzie lub rzucić dalej)
        catch (SQLException e) {
            System.err.println("Error fetching Kanban sectors: " + e.getMessage());
            // Zgodnie z testem, metoda powinna rzucić SQLException lub zwrócić pustą listę.
            // Jeśli test oczekuje rzucenia wyjątku, zmień catch block na rzucenie wyjątku: throw e;
            // Jeśli test oczekuje pustej listy, zostaw jak jest (lub zwróć pustą listę jawnie).
            throw e; // Zakładamy, że metoda rzuca SQLException w przypadku błędu
        }
        return sectors;
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

    public short getOrderPosition() {
        return orderPosition;
    }

    public void setOrderPosition(short orderPosition) {
        this.orderPosition = orderPosition;
    }
}