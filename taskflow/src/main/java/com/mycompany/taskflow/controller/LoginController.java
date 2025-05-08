package com.mycompany.taskflow.controller;

import com.mycompany.taskflow.model.DatabaseModel;
import com.mycompany.taskflow.model.UserSession; // Zakładam, że masz klasę do przechowywania sesji użytkownika
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt; // Dodaj import biblioteki BCrypt
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label loginStatusLabel;

    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/taskflow/view/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 300, 300); // Ustaw preferowane wymiary sceny

            Stage stage = new Stage();
            stage.setTitle("Logowanie");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            loginStatusLabel.setText("Błąd ładowania widoku logowania: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogin() {
        String email = usernameField.getText();
        String password = passwordField.getText();

        try (Connection connection = DatabaseModel.connect();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, first_name, last_name, email, role, team_id, password_hash FROM \"user\" WHERE email = ?")) {
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int userId = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String dbEmail = resultSet.getString("email");
                String role = resultSet.getString("role");
                Integer teamId = (Integer) resultSet.getObject("team_id");
                String hashedPasswordFromDB = resultSet.getString("password_hash");

                // Weryfikacja hasła przy użyciu BCrypt.checkpw()
                if (BCrypt.checkpw(password, hashedPasswordFromDB)) {
                    // Hasło poprawne, zaloguj użytkownika
                    UserSession.getInstance(userId, firstName, lastName, dbEmail, role, teamId);
                    loginStatusLabel.setText("Logowanie pomyślne!");
                    openMainView(role);
                } else {
                    // Hasło niepoprawne
                    loginStatusLabel.setText("Nieprawidłowy email lub hasło.");
                }

            } else {
                // Użytkownik o podanym emailu nie istnieje
                loginStatusLabel.setText("Nieprawidłowy email lub hasło.");
            }

        } catch (SQLException | IOException e) {
            loginStatusLabel.setText("Błąd logowania: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openMainView(String role) throws IOException {
        String fxmlFile;

        switch (role.toLowerCase()) {
            case "admin":
                fxmlFile = "/com/mycompany/taskflow/view/Admin/AdminView.fxml";
                break;
            case "manager":
                fxmlFile = "/com/mycompany/taskflow/view/Manager/ManagerView.fxml";
                break;
            default:
                fxmlFile = "/com/mycompany/taskflow/view/User/UserView.fxml";
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Scene scene = new Scene(root, 800, 600);

        // Dodaj arkusz stylów do nowej sceny
        scene.getStylesheets().add(getClass().getResource("/com/mycompany/taskflow/styles.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }
}