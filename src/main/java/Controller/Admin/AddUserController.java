package Controller.Admin;

import Controller.LoginService;
import MVP.Login;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * AddUserController - Handles adding new users (patients or doctors)
 */
public class AddUserController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneNumberField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private DatePicker birthdayPicker;
    @FXML private ComboBox<String> roleCombo;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> specializationCombo;
    @FXML private TextArea biographyArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private ManageUsersController manageUsersController;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[DEBUG] AddUserController initialized");
        setupRoleCombo();
        setupSpecializationCombo();
        setupRoleChangeListener();
    }

    /**
     * Set reference to parent controller
     */
    public void setManageUsersController(ManageUsersController controller) {
        this.manageUsersController = controller;
    }

    /**
     * Setup role dropdown
     */
    private void setupRoleCombo() {
        if (roleCombo != null) {
            roleCombo.getItems().addAll("PATIENT", "DOCTOR");
            roleCombo.setValue("PATIENT");
            System.out.println("[DEBUG] Role combo initialized with default: PATIENT");
        } else {
            System.out.println("[WARNING] roleCombo is NULL");
        }
    }

    /**
     * Setup specialization dropdown
     */
    private void setupSpecializationCombo() {
        if (specializationCombo != null) {
            specializationCombo.getItems().addAll(
                    "General Medicine", "Cardiology", "Dermatology", "Emergency Medicine",
                    "Endocrinology", "Family Medicine", "Gastroenterology", "Hematology",
                    "Internal Medicine", "Neurology", "Obstetrics & Gynecology", "Oncology",
                    "Ophthalmology", "Orthopedics", "Pediatrics", "Psychiatry", "Surgery", "Other"
            );
            specializationCombo.setVisible(false);
            specializationCombo.setManaged(false);
        }
        if (biographyArea != null) {
            biographyArea.setVisible(false);
            biographyArea.setManaged(false);
        }
    }


    /**
     * Show/hide doctor-specific fields based on role selection
     */
    private void setupRoleChangeListener() {
        if (roleCombo != null) {
            roleCombo.setOnAction(e -> {
                boolean isDoctor = "DOCTOR".equals(roleCombo.getValue());
                System.out.println("[DEBUG] Role changed to: " + roleCombo.getValue() + ", isDoctor: " + isDoctor);

                if (specializationCombo != null) {
                    specializationCombo.setVisible(isDoctor);
                    specializationCombo.setManaged(isDoctor);
                }
                if (biographyArea != null) {
                    biographyArea.setVisible(isDoctor);
                    biographyArea.setManaged(isDoctor);
                }
                if (addressField != null && !isDoctor) {
                    addressField.setPromptText("Address");
                } else if (addressField != null) {
                    addressField.setPromptText("Hospital/Clinic Address");
                }
            });
        }
    }

    /**
     * Handle save button
     */
    @FXML
    private void handleSave(ActionEvent event) {
        System.out.println("\n[DEBUG] ========== SAVE BUTTON CLICKED ==========");

        // Print all field values
        System.out.println("[DEBUG] Field Values:");
        System.out.println("  - Name: '" + (nameField != null && nameField.getText() != null ? nameField.getText() : "NULL") + "'");
        System.out.println("  - Email: '" + (emailField != null && emailField.getText() != null ? emailField.getText() : "NULL") + "'");
        System.out.println("  - Phone: '" + (phoneNumberField != null && phoneNumberField.getText() != null ? phoneNumberField.getText() : "NULL") + "'");
        System.out.println("  - Password Length: " + (passwordField != null && passwordField.getText() != null ? passwordField.getText().length() : "NULL"));
        System.out.println("  - Confirm Password Length: " + (confirmPasswordField != null && confirmPasswordField.getText() != null ? confirmPasswordField.getText().length() : "NULL"));
        System.out.println("  - Passwords Match: " + (passwordField != null && confirmPasswordField != null && passwordField.getText() != null && passwordField.getText().equals(confirmPasswordField.getText())));
        System.out.println("  - Birthday: " + (birthdayPicker != null && birthdayPicker.getValue() != null ? birthdayPicker.getValue() : "NULL/NOT PROVIDED"));
        System.out.println("  - Role: '" + (roleCombo != null && roleCombo.getValue() != null ? roleCombo.getValue() : "NULL") + "'");
        System.out.println("  - Address: '" + (addressField != null && addressField.getText() != null ? addressField.getText() : "NULL") + "'");
        System.out.println("  - Specialization: '" + (specializationCombo != null && specializationCombo.getValue() != null ? specializationCombo.getValue() : "NULL") + "'");
        System.out.println("  - Biography: '" + (biographyArea != null && biographyArea.getText() != null ? biographyArea.getText().substring(0, Math.min(50, biographyArea.getText().length())) : "NULL") + "'");

        // Check for null fields
        System.out.println("[DEBUG] Null Field Check:");
        System.out.println("  - nameField is " + (nameField == null ? "NULL" : "OK"));
        System.out.println("  - emailField is " + (emailField == null ? "NULL" : "OK"));
        System.out.println("  - phoneNumberField is " + (phoneNumberField == null ? "NULL" : "OK"));
        System.out.println("  - passwordField is " + (passwordField == null ? "NULL" : "OK"));
        System.out.println("  - confirmPasswordField is " + (confirmPasswordField == null ? "NULL" : "OK"));
        System.out.println("  - birthdayPicker is " + (birthdayPicker == null ? "NULL (OPTIONAL)" : "OK"));
        System.out.println("  - roleCombo is " + (roleCombo == null ? "NULL" : "OK"));
        System.out.println("  - addressField is " + (addressField == null ? "NULL (OPTIONAL)" : "OK"));
        System.out.println("  - specializationCombo is " + (specializationCombo == null ? "NULL (OPTIONAL)" : "OK"));
        System.out.println("  - biographyArea is " + (biographyArea == null ? "NULL (OPTIONAL)" : "OK"));

        if (!validateForm()) {
            System.out.println("[DEBUG] ❌ Validation FAILED");
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all required fields correctly.");
            return;
        }

        System.out.println("[DEBUG] ✓ Validation PASSED");

        // Check if email already exists
        String email = emailField.getText().trim();
        System.out.println("[DEBUG] Checking if email exists: " + email);

        if (LoginService.emailExists(email)) {
            System.out.println("[DEBUG] ❌ Email already exists");
            showAlert(Alert.AlertType.ERROR, "Email Exists", "This email is already registered in the system.");
            return;
        }

        System.out.println("[DEBUG] ✓ Email is unique");

        String role = roleCombo.getValue();
        boolean success;

        if ("PATIENT".equals(role)) {
            System.out.println("[DEBUG] Creating PATIENT...");
            success = createPatient();
        } else {
            System.out.println("[DEBUG] Creating DOCTOR...");
            success = createDoctor();
        }

        if (success) {
            System.out.println("[DEBUG] ✓ User created successfully!");
            showAlert(Alert.AlertType.INFORMATION, "Success", "User created successfully!");
            if (manageUsersController != null) {
                manageUsersController.refreshTable();
            }
            handleCancel(event);
        } else {
            System.out.println("[DEBUG] ❌ User creation FAILED");
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create user. Please try again.");
        }

        System.out.println("[DEBUG] ========== END SAVE ==========\n");
    }

    /**
     * Create a new patient
     */
    private boolean createPatient() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String phone = phoneNumberField.getText().trim();
        String birthday = (birthdayPicker != null && birthdayPicker.getValue() != null) ? birthdayPicker.getValue().toString() : "";

        System.out.println("[DEBUG] registerPatient called with:");
        System.out.println("  - Name: " + name);
        System.out.println("  - Email: " + email);
        System.out.println("  - Password length: " + password.length());
        System.out.println("  - Phone: " + phone);
        System.out.println("  - Birthday: " + (birthday.isEmpty() ? "(not provided)" : birthday));

        return LoginService.registerPatient(name, email, password, phone, birthday);
    }

    /**
     * Create a new doctor
     */
    private boolean createDoctor() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String phone = phoneNumberField.getText().trim();
        String birthday = (birthdayPicker != null && birthdayPicker.getValue() != null) ? birthdayPicker.getValue().toString() : "";
        String specialization = (specializationCombo != null && specializationCombo.getValue() != null)
                ? specializationCombo.getValue()
                : "General Medicine";
        String biography = (biographyArea != null && biographyArea.getText() != null && !biographyArea.getText().trim().isEmpty())
                ? biographyArea.getText().trim()
                : "No biography provided.";
        String address = (addressField != null && addressField.getText() != null && !addressField.getText().trim().isEmpty())
                ? addressField.getText().trim()
                : "";

        System.out.println("[DEBUG] registerDoctor called with:");
        System.out.println("  - Name: " + name);
        System.out.println("  - Email: " + email);
        System.out.println("  - Password length: " + password.length());
        System.out.println("  - Phone: " + phone);
        System.out.println("  - Birthday: " + (birthday.isEmpty() ? "(not provided)" : birthday));
        System.out.println("  - Specialization: " + specialization);
        System.out.println("  - Biography: " + biography.substring(0, Math.min(50, biography.length())));
        System.out.println("  - Address: " + (address.isEmpty() ? "(not provided)" : address));

        return LoginService.registerDoctor(name, email, password, phone, birthday, specialization, biography, address);
    }

    /**
     * Validate form fields
     */
    private boolean validateForm() {
        System.out.println("\n[DEBUG] === Starting Validation ===");

        // Name validation
        System.out.println("[DEBUG] Validating Name...");
        if (nameField == null) {
            System.out.println("  ❌ nameField is NULL");
            return false;
        }
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            System.out.println("  ❌ Name is empty or null");
            return false;
        }
        System.out.println("  ✓ Name: '" + nameField.getText().trim() + "'");

        // Email validation
        System.out.println("[DEBUG] Validating Email...");
        if (emailField == null) {
            System.out.println("  ❌ emailField is NULL");
            return false;
        }
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            System.out.println("  ❌ Email is empty or null");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            System.out.println("  ❌ Email format invalid: '" + emailField.getText().trim() + "'");
            return false;
        }
        System.out.println("  ✓ Email: '" + emailField.getText().trim() + "'");

        // Phone validation
        System.out.println("[DEBUG] Validating Phone...");
        if (phoneNumberField == null) {
            System.out.println("  ❌ phoneNumberField is NULL");
            return false;
        }
        if (phoneNumberField.getText() == null || phoneNumberField.getText().trim().isEmpty()) {
            System.out.println("  ❌ Phone is empty or null");
            return false;
        }
        System.out.println("  ✓ Phone: '" + phoneNumberField.getText().trim() + "'");

        // Password validation
        System.out.println("[DEBUG] Validating Password...");
        if (passwordField == null) {
            System.out.println("  ❌ passwordField is NULL");
            return false;
        }
        if (passwordField.getText() == null || passwordField.getText().length() < 6) {
            System.out.println("  ❌ Password is null or < 6 characters (length: " +
                    (passwordField.getText() != null ? passwordField.getText().length() : "NULL") + ")");
            return false;
        }
        if (confirmPasswordField == null) {
            System.out.println("  ❌ confirmPasswordField is NULL");
            return false;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            System.out.println("  ❌ Passwords don't match");
            System.out.println("    Password: '" + passwordField.getText() + "'");
            System.out.println("    Confirm: '" + confirmPasswordField.getText() + "'");
            return false;
        }
        System.out.println("  ✓ Password length: " + passwordField.getText().length());

        // Birthday validation (OPTIONAL)
        System.out.println("[DEBUG] Validating Birthday...");
        if (birthdayPicker != null && birthdayPicker.getValue() != null) {
            LocalDate birthday = birthdayPicker.getValue();
            LocalDate minDate = LocalDate.now().minusYears(13);
            if (birthday.isAfter(minDate)) {
                System.out.println("  ❌ User must be at least 13 years old");
                System.out.println("    Birthday: " + birthday);
                System.out.println("    Min date: " + minDate);
                return false;
            }
            System.out.println("  ✓ Birthday: " + birthday);
        } else {
            System.out.println("  ✓ Birthday not provided (optional)");
        }

        // Role-specific validation (OPTIONAL for doctors)
        System.out.println("[DEBUG] Validating Role-specific fields...");
        if (roleCombo == null) {
            System.out.println("  ❌ roleCombo is NULL");
            return false;
        }
        String role = roleCombo.getValue();
        System.out.println("  Role: " + role);

        if ("DOCTOR".equals(role)) {
            System.out.println("  [DEBUG] Validating Doctor-specific fields (optional)...");

            // Specialization is optional
            if (specializationCombo != null && specializationCombo.getValue() != null) {
                System.out.println("    ✓ Specialization: '" + specializationCombo.getValue() + "'");
            } else {
                System.out.println("    ✓ Specialization not provided (optional)");
            }

            // Address is optional
            if (addressField != null && addressField.getText() != null && !addressField.getText().trim().isEmpty()) {
                System.out.println("    ✓ Address: '" + addressField.getText().trim() + "'");
            } else {
                System.out.println("    ✓ Address not provided (optional)");
            }
        } else {
            System.out.println("  ✓ Patient role - no additional fields required");
        }

        System.out.println("[DEBUG] === Validation PASSED ===\n");
        return true;
    }

    /**
     * Handle cancel button
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("ManageUsers.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @FXML
    private void handleNavigation(ActionEvent event) {
        System.out.println("[DEBUG] Navigation clicked inside AddUser");

        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();

        String targetFxml = switch (id) {
            case "backButton", "dashboardButton" -> "ManageUsers.fxml";
            case "usersButton" -> "ManageUsers.fxml";
            default -> null;
        };

        if (targetFxml != null) {
            try {
                FXMLLoader loader = new FXMLLoader(Login.class.getResource(targetFxml));
                Scene scene = new Scene(loader.load(), 1280, 768);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}