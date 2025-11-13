package Controller.Doctor;

import MVP.Login; // Assuming MVP.Login is used for resource loading path
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the Doctor Account Management FXML.
 * Handles user input for profile details and navigation events.
 */
public class AccountController {

    // FXML Injections for Profile Snapshot
    @FXML private Label doctorNameLabel;
    @FXML private Label specializationLabel;

    // FXML Injections for Editable Fields
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField specializationField;
    @FXML private TextArea bioArea;
    @FXML private PasswordField passwordField;

    // FXML Injections for Navigation Buttons (to read their IDs in handleNavigation)
    @FXML private Button accountButton;
    @FXML private Button casesButton;
    @FXML private Button calendarButton;
    @FXML private Button notificationsButton;
    @FXML private Button settingsButton;


    /**
     * Initializes the controller. This method is called automatically after
     * the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        System.out.println("Account controller initialized.");
        // Example: Load initial data (in a real app, this would be from a database)
        doctorNameLabel.setText("Dr. Jane Doe");
        specializationLabel.setText("Pediatrician");
        fullNameField.setText("Jane A. Doe");
        emailField.setText("jane.doe@clinic.com");
        // Clear password field on load for security
        passwordField.setText("");
    }

    /**
     * Handles all sidebar navigation button clicks.
     * @param event The ActionEvent triggered by the button click.
     */
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        String targetFxml = "";

        // Determine which FXML to load based on the button ID
        switch (buttonId) {
            case "accountButton":
                targetFxml = "DoctorAccount.fxml"; // Updated to the new FXML name
                break;
            case "casesButton":
                targetFxml = "History.fxml";
                break;
            case "calendarButton":
                targetFxml = "Calendar.fxml";
                break;
            case "notificationsButton":
                targetFxml = "Chat.fxml"; // Navigate to the Chat/Notifications screen
                break;
            case "settingsButton":
                targetFxml = "DoctorSettings.fxml";
                break;
            default:
                System.out.println("Unknown navigation target: " + buttonId);
                return;
        }

        System.out.println("Navigating to: " + targetFxml);
        try {
            // Load the FXML scene
            FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource(targetFxml));
            Scene scene = new Scene(fxmlLoader.load(), 1280, 768);

            // Get the stage from the current event's source node
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load scene: " + targetFxml);
        }
    }

    /**
     * Handles the action to change the doctor's profile picture.
     * In a real app, this would open a file chooser dialog.
     */
    public void handleChangePicture(ActionEvent actionEvent) {
        System.out.println("Action: Change Profile Picture initiated.");
        // Logic to open a file chooser and upload image goes here.
    }

    /**
     * Handles saving all changes made in the profile fields.
     */
    public void handleSaveChanges(ActionEvent actionEvent) {
        System.out.println("Action: Saving changes...");
        // Retrieve data from fields
        String name = fullNameField.getText();
        String email = emailField.getText();
        String specialization = specializationField.getText();
        String newPassword = passwordField.getText();

        // Print to console (Mock save operation)
        System.out.println("Name to save: " + name);
        System.out.println("Email to save: " + email);
        if (!newPassword.isEmpty()) {
            System.out.println("Password will be updated.");
        }

        // Logic to update the database goes here.
    }

    /**
     * Handles the user clicking the Logout button.
     * In a real app, this would clear the session and return to the Login screen.
     */
    public void handleLogout(ActionEvent actionEvent) {
        System.out.println("Action: Logging out...");
        // Logic to clear user session and load the Login screen FXML goes here.
    }
}