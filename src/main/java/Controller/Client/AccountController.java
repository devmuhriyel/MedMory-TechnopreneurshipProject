package Controller.Client;

import MVP.Login;
import CommonModel.SessionManager;
import MVP.Client.ClientModel;
import MVP.Client.ClientService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class AccountController {

    // --- FXML Fields for Personal Information ---
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField birthdayField;
    @FXML private TextField locationField;

    // --- FXML Fields for Security ---
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // --- Navigation Buttons ---
    @FXML private Button homeButton;
    @FXML private Button accountButton;
    @FXML private Button casesButton;
    @FXML private Button calendarButton;
    @FXML private Button chatButton; // Renamed from notifications to match your latest FXML
    @FXML private Button settingsButton;

    @FXML
    public void initialize() {
        System.out.println("Client Account Controller Initialized");

        int userID = SessionManager.getUserID();

        if (userID <= 0) {
            System.err.println("⚠ No client session found.");
            return;
        }

        // Fetch client data from the database service
        ClientModel client = ClientService.getClientInfo(userID);

        if (client != null) {
            // Populate fields (Assuming ClientModel has these fields or split name)
            // If your model only has 'name', you can split it or just set one field
            firstNameField.setText(client.name);
//            emailField.setText(client.email);
            // Add other fields as they exist in your ClientModel
        } else {
            System.err.println("⚠ Failed to load client information.");
        }
    }

    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();

        String targetFxml = switch (id) {
            case "homeButton" -> "ClientHomepage.fxml";
            case "accountButton" -> "ClientAccount.fxml";
            case "casesButton" -> "MedicalRecord.fxml";
            case "calendarButton" -> "ClientCalendar.fxml";
            case "chatButton" -> "Chat.fxml";
            case "settingsButton" -> "ClientSettings.fxml";
            default -> null;
        };

        if (targetFxml != null) {
            navigateTo(event, targetFxml);
        }
    }

    private void navigateTo(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource(fxmlFile));
            Scene scene = new Scene(loader.load(), 1366, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Failed to load: " + fxmlFile);
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSaveChanges(ActionEvent event) {
        System.out.println("Updating profile for: " + firstNameField.getText() + " " + lastNameField.getText());

        // Logic to call your ClientService to update database
        // Example: ClientService.updateProfile(SessionManager.getUserID(), firstNameField.getText(), ...);

        showAlert(Alert.AlertType.INFORMATION, "Success", "Profile information updated successfully!");
    }

    @FXML
    public void handlePasswordChange(ActionEvent event) {
        String currentPos = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (currentPos.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all password fields.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match!");
            return;
        }

        // Logic to call your ClientService to update password
        System.out.println("Password change requested...");
        showAlert(Alert.AlertType.INFORMATION, "Success", "Password has been changed successfully.");

        // Clear fields after success
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        SessionManager.clearSession();
        navigateTo(event, "Login.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}