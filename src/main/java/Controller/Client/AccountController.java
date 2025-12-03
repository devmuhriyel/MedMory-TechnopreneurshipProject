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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AccountController {

    @FXML private TextField fullNameField;

    @FXML private Button accountButton;
    @FXML private Button casesButton;
    @FXML private Button calendarButton;
    @FXML private Button notificationsButton;
    @FXML private Button settingsButton;

    @FXML
    public void initialize() {
        System.out.println("Client Account Controller Initialized");

        int userID = SessionManager.getUserID();

        if (userID <= 0) {
            System.err.println("⚠ No client session found.");
            return;
        }

        ClientModel client = ClientService.getClientInfo(userID);

        if (client == null) {
            System.err.println("⚠ Failed to load client information.");
            return;
        }

        fullNameField.setText(client.name);
    }

    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();

        String targetFxml = switch (id) {
            case "homeButton" -> "ClientHomepage.fxml";
            case "accountButton" -> "ClientAccount.fxml";
            case "casesButton" -> "MedicalRecord.fxml";
            case "calendarButton" -> "ClientCalendar.fxml";
            case "notificationsButton" -> "Chat.fxml";
            case "settingsButton" -> "ClientSettings.fxml";
            default -> null;
        };

        if (targetFxml == null) {
            System.err.println("Unknown navigation target: " + id);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource(targetFxml));
            Scene scene = new Scene(loader.load(), 1280, 768);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Failed to load: " + targetFxml);
            e.printStackTrace();
        }
    }

    public void handleSaveChanges(ActionEvent event) {
        System.out.println("Saving client changes...");
        System.out.println("Updated Name: " + fullNameField.getText());
    }

    public void handleLogout(ActionEvent event) {
        System.out.println("Logging out...");

        SessionManager.clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("Login.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 768);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Failed to load Login.fxml");
            e.printStackTrace();
        }
    }
}
