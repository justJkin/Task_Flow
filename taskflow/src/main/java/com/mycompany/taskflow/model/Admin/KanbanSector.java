package com.mycompany.taskflow.model.Admin;

import com.mycompany.taskflow.model.DatabaseModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KanbanSector {
    private int id;
    private String name;
    private short orderPosition;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public short getOrderPosition() { return orderPosition; }
    public void setOrderPosition(short orderPosition) { this.orderPosition = orderPosition; }

    // Metody do interakcji z bazą danych (przykładowe)
    public static KanbanSector getSectorById(int id) {
        KanbanSector sector = null;
        String query = "SELECT name, order_position FROM kanban_sector WHERE id = ?";
        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                sector = new KanbanSector();
                sector.setId(id);
                sector.setName(resultSet.getString("name"));
                sector.setOrderPosition(resultSet.getShort("order_position"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sector;
    }
}