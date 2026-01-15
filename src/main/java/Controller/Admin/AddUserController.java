package Controller.Admin;

import DAO.*;
import MVP.Login;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

public class AddUserController {

    @FXML private TextField nameField;
    @FXML private TextField passwordField;
    @FXML private ComboBox<String> roleBox;

    private RecordDAO recordDAO;
    private Connection conn;

    @FXML
    public void initialize() {
        Connection conn = DatabaseConnection.getConnection();
        recordDAO = new RecordDAO(conn);

        roleBox.getItems().addAll("PATIENT", "DOCTOR");
        roleBox.setValue("PATIENT"); // default
    }

    @FXML
    private void handleCreateUser() {

        String name = nameField.getText();
        String password = passwordField.getText();
        String role = roleBox.getValue();

        System.out.println("=== CREATE USER DEBUG ===");
        System.out.println("Name: '" + name + "'");
        System.out.println("Role: " + role);

        if (name == null || name.trim().isEmpty()) {
            showAlert("Validation Error", "Name is required.");
            return;
        }

        boolean success;

        if ("PATIENT".equals(role)) {
            success = recordDAO.createPatient(name,password);
        } else {
            if (password == null || password.trim().isEmpty()) {
                showAlert("Validation Error", "Password is required for doctors.");
                return;
            }
            success = recordDAO.createDoctor(name, password);
        }

        if (success) {
            System.out.println("✅ User created successfully");
            //closeWindow();
        } else {
            showAlert("Error", "Failed to create user.");
        }
    }

    // ---------------- NAVIGATION ----------------

    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();

        String targetFxml = switch (id) {
            case "backButton" -> "ManageUsers.fxml";
            case "settingsButton" -> "Settings.fxml";
            case "logOutButton" -> "Login.fxml";
            default -> null;
        };

        if (targetFxml == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource(targetFxml));
            Scene scene = new Scene(loader.load(), 1280, 768);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------- HELPERS ----------------

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
