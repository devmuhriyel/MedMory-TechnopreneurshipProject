package Controller.Client;

import MVP.Login;
import MVP.SessionManager;
import MVP.Client.ClientService;
import MVP.Client.ClientModel;
import CommonModel.*;
import MVP.Client.ClientMedicalRecord;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientHomepageController {

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;

    // fields for medical record if needed:
    @FXML private TextArea subjArea;
    @FXML private TextArea ccArea;
    @FXML private TextArea hpiArea;

    @FXML
    public void initialize() {
        int userId = SessionManager.getCurrentPatientId();

        if (userId <= 0) {
            System.out.println("⚠ No client logged in.");
            return;
        }

        // Load client info
        PatientModel patient = ClientService.getClientInfo(userId);
        if (patient != null) {
            System.out.println("Name: " + patient.getName());
            System.out.println("Email: " + patient.getEmail());
        }


    }

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
}
