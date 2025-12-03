package Controller.Doctor;

import CommonModel.SessionManager;
import MVP.Doctor.DoctorModel;
import MVP.Doctor.DoctorService;
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
        System.out.println("Doctor Account Controller Initialized");

        int doctorID = SessionManager.getDoctorID();
        if (doctorID <= 0) {
            System.err.println("⚠ No doctor session found.");
            return;
        }

        DoctorService service = new DoctorService();
        var doctor = service.getDoctorInfo(doctorID);

        if (doctor == null) {
            System.err.println("⚠ Failed to load doctor information.");
            return;
        }

        // Fill sidebar card
        doctorNameLabel.setText("Dr. " + doctor.name);
        specializationLabel.setText(doctor.specialization != null ? doctor.specialization : "N/A");

        // Fill editable fields
        fullNameField.setText(doctor.name);
        emailField.setText(doctor.email);
        phoneField.setText(doctor.phone);
        specializationField.setText(doctor.specialization);
        bioArea.setText(doctor.biography);
        passwordField.setText(""); // Always blank for security
    }


    /**
     * Handles all sidebar navigation button clicks.
     * @param event The ActionEvent triggered by the button click.
     */
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        String targetFxml = "";

        switch (buttonId) {
            case "accountButton":
                targetFxml = "DoctorAccount.fxml";
                break;
            case "casesButton":
                targetFxml = "History.fxml";
                break;
            case "recordsButton":
                targetFxml = "ViewMedicalRecordList.fxml";
                break;
            case "notificationsButton":
                targetFxml = "Chat.fxml";
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