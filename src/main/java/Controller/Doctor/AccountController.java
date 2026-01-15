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
    @FXML private PasswordField passwordField;  // Retype Password
    @FXML private PasswordField passwordField1; // New Password
    @FXML private PasswordField passwordField2; // Current Password

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
        // Clear all password fields for security
        passwordField.clear();
        passwordField1.clear();
        passwordField2.clear();
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
        
        // Password fields
        String currentPassword = passwordField2.getText().trim();
        String newPassword = passwordField1.getText().trim();
        String retypePassword = passwordField.getText().trim();

        // Validate required fields
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Name cannot be empty.");
            return;
        }
        
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Email cannot be empty.");
            return;
        }

        // Validate password change if any password field is filled
        boolean attemptingPasswordChange = !currentPassword.isEmpty() || !newPassword.isEmpty() || !retypePassword.isEmpty();
        
        if (attemptingPasswordChange) {
            // All three password fields must be filled to change password
            if (currentPassword.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Password Error", "Please enter your current password to change it.");
                return;
            }
            
            if (newPassword.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Password Error", "Please enter a new password.");
                return;
            }
            
            if (retypePassword.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Password Error", "Please retype your new password.");
                return;
            }
            
            // Verify that new password and retype password match
            if (!newPassword.equals(retypePassword)) {
                showAlert(Alert.AlertType.ERROR, "Password Mismatch", "New password and retype password do not match. Please try again.");
                return;
            }
            
            // Verify current password
            DoctorService service = new DoctorService();
            if (!service.verifyCurrentPassword(doctorID, currentPassword)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Password", "Current password is incorrect. Please try again.");
                return;
            }
        }

        // Create service instance and update doctor information
        DoctorService service = new DoctorService();
        // Only pass new password if password change is being attempted
        String passwordToUpdate = attemptingPasswordChange ? newPassword : null;
        boolean success = service.updateDoctor(doctorID, name, email, phone, specialization, biography, passwordToUpdate);

        if (success) {
            // Clear all password fields after successful save
            passwordField.clear();
            passwordField1.clear();
            passwordField2.clear();
            
            // Update displayed labels
            doctorNameLabel.setText("Dr. " + name);
            if (specialization != null && !specialization.isEmpty()) {
                specializationLabel.setText(specialization);
            }
            
            String successMessage = "Your changes have been saved successfully!";
            if (attemptingPasswordChange) {
                successMessage = "Your profile and password have been updated successfully!";
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", successMessage);
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