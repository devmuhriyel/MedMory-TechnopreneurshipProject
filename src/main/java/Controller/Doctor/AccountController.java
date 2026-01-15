package Controller.Doctor;

import CommonModel.SessionManager;
import MVP.Doctor.DoctorService;
import MVP.Login; // Assuming MVP.Login is used for resource loading path
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
            case "records":
            case "recordsButton":
                targetFxml = "ViewMedicalRecordList.fxml";
                break;
            case "casesButton":
                targetFxml = "Patient.fxml";
                break;
            case "calendarButton":
                targetFxml = "Calendar.fxml";
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
        
        // Get the current doctor ID from session
        int doctorID = SessionManager.getDoctorID();
        if (doctorID <= 0) {
            showAlert(Alert.AlertType.ERROR, "Error", "No doctor session found. Please log in again.");
            return;
        }
        
        // Retrieve data from fields
        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String specialization = specializationField.getText().trim();
        String biography = bioArea.getText().trim();
        String newPassword = passwordField.getText().trim();

        // Validate required fields
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Name cannot be empty.");
            return;
        }
        
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Email cannot be empty.");
            return;
        }

        // Create service instance and update doctor information
        DoctorService service = new DoctorService();
        boolean success = service.updateDoctor(doctorID, name, email, phone, specialization, biography, newPassword);

        if (success) {
            // Clear password field after successful save
            passwordField.clear();
            
            // Update displayed labels
            doctorNameLabel.setText("Dr. " + name);
            if (specialization != null && !specialization.isEmpty()) {
                specializationLabel.setText(specialization);
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your changes have been saved successfully!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save changes. Please try again.");
        }
    }
    
    /**
     * Helper method to show alert dialogs
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Handles the user clicking the Logout button.
     * Clears the session and returns to the Login screen.
     */
    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        System.out.println("Action: Logging out...");
        
        try {
            // Clear session if needed
            // SessionManager.clearSession(); // Uncomment if you have session clearing logic
            
            // Load the Login screen
            FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource("Login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1280, 768);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load Login scene");
        }
    }
}