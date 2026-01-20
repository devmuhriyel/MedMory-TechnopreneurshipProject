package Controller.Client;



import MVP.Login;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientChatController {

    // Sidebar Buttons
    @FXML private Button accountButton;
    @FXML private Button records;
    @FXML private Button casesButton;
    @FXML private Button calendarButton;
    @FXML private Button notificationsButton;
    @FXML private Button settingsButton;

    // Search / Filter Buttons (Optional, add fx:id if needed)
    @FXML private Button filterAllButton;
    @FXML private Button filterPatientsButton;
    @FXML private Button filterDoctorsButton;
    @FXML private TextField searchField;

    /**
     * Handles all sidebar navigation button clicks
     */
    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        String targetFxml = "";

        switch (buttonId) {
            case "homeButton":
                targetFxml = "ClientHomepage.fxml";
                break;
            case "accountButton":
                targetFxml = "ClientAccount.fxml";
                break;
            case "medicalRecordButton":
                targetFxml = "MedicalRecord.fxml";
                break;
            case "calendarButton":
                targetFxml = "ClientCalendar.fxml";
                break;
            case "chatsButton":
                targetFxml = "ClientChat.fxml"; // Stay in Chat
                break;
            case "settingsButton":
                targetFxml = "ClientSettings.fxml";
                break;
            default:
                System.out.println("Unknown navigation target: " + buttonId);
                return;
        }

        System.out.println("Navigating to: " + targetFxml);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource(targetFxml));
            Scene scene = new Scene(fxmlLoader.load(), 1280, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load scene: " + targetFxml);
        }
    }

    /**
     * Handles clicking a notification item in the chat list
     */
    @FXML
    public void handleNotificationClick(ActionEvent event) {
        System.out.println("Notification clicked!");

    }

    /**
     * Handles sending a message from the input field
     */
    @FXML
    public void handleSendMessage(ActionEvent event) {
        System.out.println("Message sent: " + (searchField != null ? searchField.getText() : ""));

    }

    /**
     * Handles filtering chats by All, Patients, or Doctors/Lab
     */
    @FXML
    public void handleFilterChange(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String filter = clickedButton.getText();
        System.out.println("Filter applied: " + filter);

    }

    /**
     * Optional: initialize method if you want to set default states
     */
    @FXML
    public void initialize() {
        System.out.println("ChatController initialized.");
    }
}

