package Controller.Doctor;

import CommonModel.DoctorModel;
import DAO.DatabaseConnection;
import DAO.RecordDAO;
import MVP.Login;
import MVP.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller for the Doctor Account Management FXML.
 * Handles user input for profile details and navigation events.
 * Updated to use RecordDAO and proper models.
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
    @FXML private TextField addressField;
    @FXML private TextArea bioArea;
    @FXML private PasswordField passwordField;  // Retype Password
    @FXML private PasswordField passwordField1; // New Password
    @FXML private PasswordField passwordField2; // Current Password

    // FXML Injections for Navigation Buttons
    @FXML private Button accountButton;
    @FXML private Button casesButton;
    @FXML private Button calendarButton;
    @FXML private Button notificationsButton;
    @FXML private Button settingsButton;

    private RecordDAO recordDAO;

    /**
     * Initializes the controller. This method is called automatically after
     * the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        System.out.println("Doctor Account Controller Initialized");

        // Initialize DAO
        Connection conn = DatabaseConnection.getConnection();
        recordDAO = new RecordDAO(conn);

        // Get current doctor session
        int doctorID = SessionManager.getCurrentUserId();
        if (doctorID <= 0) {
            System.err.println("⚠ No doctor session found.");
            showAlert(Alert.AlertType.WARNING, "Session Error", "No active doctor session found. Please log in again.");
            return;
        }

        // Load doctor information using RecordDAO
        loadDoctorInfo(doctorID);
    }

    /**
     * Load doctor information from database using RecordDAO
     */
    private void loadDoctorInfo(int doctorID) {
        try {
            DoctorModel doctor = recordDAO.getDoctorById(doctorID);

            if (doctor == null) {
                System.err.println("⚠ Failed to load doctor information.");
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load doctor information.");
                return;
            }

            // Fill sidebar card
            doctorNameLabel.setText("Dr. " + doctor.getName());
            specializationLabel.setText(doctor.getSpecialization() != null ? doctor.getSpecialization() : "N/A");

            // Fill editable fields
            fullNameField.setText(doctor.getName());
            emailField.setText(doctor.getEmail());
            phoneField.setText(doctor.getPhone());
            specializationField.setText(doctor.getSpecialization());
            if (addressField != null) {
                addressField.setText(doctor.getAddress());
            }
            bioArea.setText(doctor.getBiography());

            // Clear all password fields for security
            passwordField.clear();
            passwordField1.clear();
            passwordField2.clear();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load doctor information: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles all sidebar navigation button clicks.
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
            FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource(targetFxml));
            Scene scene = new Scene(fxmlLoader.load(), 1280, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load scene: " + targetFxml);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to " + targetFxml);
        }
    }

    /**
     * Handles the action to change the doctor's profile picture.
     */
    public void handleChangePicture(ActionEvent actionEvent) {
        System.out.println("Action: Change Profile Picture initiated.");
        showAlert(Alert.AlertType.INFORMATION, "Feature Coming Soon", "Profile picture upload will be available in a future update.");
    }

    /**
     * Handles saving all changes made in the profile fields.
     */
    public void handleSaveChanges(ActionEvent actionEvent) {
        System.out.println("Action: Saving changes...");

        // Get the current doctor ID from session
        int doctorID = SessionManager.getCurrentUserId();
        if (doctorID <= 0) {
            showAlert(Alert.AlertType.ERROR, "Error", "No doctor session found. Please log in again.");
            return;
        }

        // Retrieve data from fields
        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String specialization = specializationField.getText().trim();
        String address = addressField != null ? addressField.getText().trim() : "";
        String biography = bioArea.getText().trim();

        // Password fields
        String currentPassword = passwordField2.getText().trim();
        String newPassword = passwordField1.getText().trim();
        String retypePassword = passwordField.getText().trim();

        // Validate Full Name
        if (!validateFullName(name)) {
            return;
        }

        // Validate Email
        if (!validateEmail(email)) {
            return;
        }

        // Validate Phone Number
        if (!validatePhoneNumber(phone)) {
            return;
        }

        // Validate Specialization
        if (!validateSpecialization(specialization)) {
            return;
        }

        // Validate Biography (optional but validate if provided)
        if (!biography.isEmpty() && biography.length() > 1000) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Biography cannot exceed 1000 characters.");
            bioArea.requestFocus();
            return;
        }

        // Validate password change if any password field is filled
        boolean attemptingPasswordChange = !currentPassword.isEmpty() || !newPassword.isEmpty() || !retypePassword.isEmpty();

        if (attemptingPasswordChange) {
            if (!validatePasswordChange(doctorID, currentPassword, newPassword, retypePassword)) {
                return;
            }
        }

        // Update doctor information using RecordDAO
        try {
            boolean success = recordDAO.updateDoctor(doctorID, name, email, phone, specialization, biography, address);

            if (success) {
                // Handle password update if needed
                if (attemptingPasswordChange) {
                    updatePassword(doctorID, newPassword);
                }

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

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while saving: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validate full name
     */
    private boolean validateFullName(String name) {
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Full Name cannot be empty.");
            fullNameField.requestFocus();
            return false;
        }
        if (name.length() < 2) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Full Name must be at least 2 characters long.");
            fullNameField.requestFocus();
            return false;
        }
        if (!name.matches("^[a-zA-Z\\s\\-']+$")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Full Name can only contain letters, spaces, hyphens, and apostrophes.");
            fullNameField.requestFocus();
            return false;
        }
        if (name.split("\\s+").length < 2) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter your full name (first and last name).");
            fullNameField.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Validate email
     */
    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Email cannot be empty.");
            emailField.requestFocus();
            return false;
        }
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!email.matches(emailPattern)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid email address (e.g., example@email.com).");
            emailField.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Validate phone number
     */
    private boolean validatePhoneNumber(String phone) {
        if (phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Phone Number cannot be empty.");
            phoneField.requestFocus();
            return false;
        }
        String phoneDigits = phone.replaceAll("[\\s\\-\\(\\)\\+]", "");
        if (!phoneDigits.matches("^\\d{10,15}$")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid phone number (10-15 digits).");
            phoneField.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Validate specialization
     */
    private boolean validateSpecialization(String specialization) {
        if (specialization.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Specialization cannot be empty.");
            specializationField.requestFocus();
            return false;
        }
        if (specialization.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Specialization must be at least 3 characters long.");
            specializationField.requestFocus();
            return false;
        }
        if (!specialization.matches("^[a-zA-Z\\s\\-&]+$")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Specialization can only contain letters, spaces, hyphens, and ampersands.");
            specializationField.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Validate password change
     */
    private boolean validatePasswordChange(int doctorID, String currentPassword, String newPassword, String retypePassword) {
        // All three password fields must be filled to change password
        if (currentPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Password Error", "Please enter your current password to change it.");
            return false;
        }

        if (newPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Password Error", "Please enter a new password.");
            return false;
        }

        if (retypePassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Password Error", "Please retype your new password.");
            return false;
        }

        // Verify that new password and retype password match
        if (!newPassword.equals(retypePassword)) {
            showAlert(Alert.AlertType.ERROR, "Password Mismatch", "New password and retype password do not match. Please try again.");
            return false;
        }

        // Verify current password
        if (!verifyCurrentPassword(doctorID, currentPassword)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Password", "Current password is incorrect. Please try again.");
            return false;
        }

        return true;
    }

    /**
     * Verify the current password
     */
    private boolean verifyCurrentPassword(int doctorID, String currentPassword) {
        String sql = "SELECT password_hash FROM users WHERE user_id = ? AND role = 'DOCTOR'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password_hash");
                    // Note: In production, you should use proper password hashing (BCrypt, etc.)
                    return currentPassword.equals(storedPassword);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error verifying password: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Update the password
     */
    private void updatePassword(int doctorID, String newPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ? AND role = 'DOCTOR'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Note: In production, you should hash the password before storing
            ps.setString(1, newPassword);
            ps.setInt(2, doctorID);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
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
            // Clear session
            SessionManager.clearSession();

            // Load the Login screen
            FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource("Login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1280, 768);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load Login scene");
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Failed to return to login screen.");
        }
    }
}