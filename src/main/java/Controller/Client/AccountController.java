package Controller.Client;

import Controller.LoginService;
import MVP.SessionManager;
import DAO.DatabaseConnection;
import DAO.RecordDAO;
import MVP.Login;
import CommonModel.PatientModel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AccountController {

    // --- FXML Fields for Personal Information ---
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker birthdayPicker;
    @FXML private Label userIdLabel;
    @FXML private Label accountTypeLabel;
    @FXML private Label memberSinceLabel;

    // --- FXML Fields for Security ---
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // --- Navigation Buttons ---
    @FXML private Button homeButton;
    @FXML private Button accountButton;
    @FXML private Button recordsButton;
    @FXML private Button casesButton;
    @FXML private Button calendarButton;
    @FXML private Button chatButton;
    @FXML private Button settingsButton;

    // --- Action Buttons ---
    @FXML private Button saveProfileButton;
    @FXML private Button changePasswordButton;
    @FXML private Button cancelButton;

    private RecordDAO recordDAO;
    private int currentPatientId;

    @FXML
    public void initialize() {
        System.out.println("Patient Account Controller Initialized");

        // Initialize database connection
        Connection conn = DatabaseConnection.getConnection();
        recordDAO = new RecordDAO(conn);

        // Get current patient ID from session
        currentPatientId = SessionManager.getCurrentUserId();

        if (currentPatientId <= 0) {
            System.err.println("⚠ No patient session found.");
            showAlert(Alert.AlertType.ERROR, "Session Error", "No active session found. Please log in again.");
            return;
        }

        // Load patient information
        loadPatientInfo();
    }

    /**
     * Load patient information from database
     */
    private void loadPatientInfo() {
        PatientModel patient = recordDAO.getPatientById(currentPatientId);

        if (patient != null) {
            // Populate personal information fields
            if (nameField != null) {
                nameField.setText(patient.getName());
            }

            if (emailField != null) {
                emailField.setText(patient.getEmail());
            }

            if (phoneField != null) {
                phoneField.setText(patient.getPhoneNumber());
            }

            if (birthdayPicker != null && patient.getBirthday() != null && !patient.getBirthday().isEmpty()) {
                try {
                    LocalDate birthday = LocalDate.parse(patient.getBirthday());
                    birthdayPicker.setValue(birthday);
                } catch (Exception e) {
                    System.err.println("Error parsing birthday: " + e.getMessage());
                }
            }

            // Populate info labels
            if (userIdLabel != null) {
                userIdLabel.setText("Patient ID: " + patient.getPatientId());
            }

            if (accountTypeLabel != null) {
                accountTypeLabel.setText("Account Type: Patient");
            }

            if (memberSinceLabel != null && patient.getCreatedAt() != null) {
                try {
                    // Parse and format the creation date
                    String createdAt = patient.getCreatedAt();
                    if (createdAt.contains(" ")) {
                        createdAt = createdAt.split(" ")[0]; // Get just the date part
                    }
                    LocalDate memberSince = LocalDate.parse(createdAt);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
                    memberSinceLabel.setText("Member Since: " + memberSince.format(formatter));
                } catch (Exception e) {
                    memberSinceLabel.setText("Member Since: " + patient.getCreatedAt());
                }
            }

            // Update session with current user info
            SessionManager.setCurrentUserName(patient.getName());
            SessionManager.setCurrentUserEmail(patient.getEmail());

        } else {
            System.err.println("⚠ Failed to load patient information.");
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load your profile information.");
        }
    }

    /**
     * Handle save profile changes
     */
    @FXML
    public void handleSaveChanges(ActionEvent event) {
        if (!validateProfileForm()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all required fields correctly.");
            return;
        }

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String birthday = birthdayPicker.getValue().toString();

        // Update patient information
        boolean success = recordDAO.updatePatient(currentPatientId, name, email, phone, birthday);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Profile information updated successfully!");

            // Update session with new info
            SessionManager.setCurrentUserName(name);
            SessionManager.setCurrentUserEmail(email);

            // Reload the info to reflect changes
            loadPatientInfo();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile. Please try again.");
        }
    }

    /**
     * Validate profile form fields
     */
    private boolean validateProfileForm() {
        // Name validation
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            return false;
        }

        // Email validation
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            return false;
        }
        if (!emailField.getText().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return false;
        }

        // Phone validation
        if (phoneField.getText() == null || phoneField.getText().trim().isEmpty()) {
            return false;
        }

        // Birthday validation
        if (birthdayPicker.getValue() == null) {
            return false;
        }

        return true;
    }

    /**
     * Handle password change
     */
    @FXML
    public void handlePasswordChange(ActionEvent event) {
        String currentPass = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        // Validation
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All password fields are required.");
            return;
        }

        if (newPass.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Error", "New password must be at least 6 characters long.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match.");
            return;
        }

        boolean success = LoginService.changePassword(currentPatientId, currentPass, newPass);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully!");
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password. Please check your current password.");
        }
    }

    /**
     * Handle cancel button - reload original data
     */
    @FXML
    public void handleCancel(ActionEvent event) {
        loadPatientInfo();
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    /**
     * Handle navigation buttons
     */
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
            default -> {
                System.err.println("Unknown navigation button: " + id);
                yield null;
            }
        };

        if (targetFxml != null) {
            navigateTo(event, targetFxml);
        }
    }

    /**
     * Navigate to another screen
     */
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
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load " + fxmlFile);
        }
    }

    /**
     * Handle logout
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Logout");
        confirmAlert.setHeaderText("Are you sure you want to log out?");
        confirmAlert.setContentText("You will need to log in again to access your account.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SessionManager.clearSession();
                navigateTo(event, "Login.fxml");
            }
        });
    }

    /**
     * View medical records
     */
    @FXML
    public void handleViewRecords(ActionEvent event) {
        navigateTo(event, "PatientRecords.fxml");
    }

    /**
     * View medical cases
     */
    @FXML
    public void handleViewCases(ActionEvent event) {
        navigateTo(event, "PatientCases.fxml");
    }

    /**
     * View assigned doctors
     */
    @FXML
    public void handleViewDoctors(ActionEvent event) {
        navigateTo(event, "PatientDoctors.fxml");
    }

    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Format phone number for display (optional enhancement)
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "";
        }
        // Simple formatting for Philippine numbers (09XX-XXX-XXXX)
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 11 && digits.startsWith("09")) {
            return String.format("%s-%s-%s",
                    digits.substring(0, 4),
                    digits.substring(4, 7),
                    digits.substring(7));
        }
        return phone;
    }

    /**
     * Calculate age from birthday
     */
    private int calculateAge(String birthday) {
        if (birthday == null || birthday.isEmpty()) {
            return 0;
        }
        try {
            LocalDate birthDate = LocalDate.parse(birthday);
            LocalDate currentDate = LocalDate.now();
            return currentDate.getYear() - birthDate.getYear();
        } catch (Exception e) {
            return 0;
        }
    }
}