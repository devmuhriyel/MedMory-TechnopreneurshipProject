package Controller.Admin;

import DAO.RecordDAO;
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
 * EditUserController - Handles editing existing users
 */
public class EditUserController implements Initializable {

    @FXML private Label userIdLabel;
    @FXML private Label roleLabel;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker birthdayPicker;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> specializationCombo;
    @FXML private TextArea biographyArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private UserRow currentUser;
    private ManageUsersController manageUsersController;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[DEBUG] EditUserController initialize()");

        setupSpecializationCombo();

        // Check FXML injections
        if (nameField == null || emailField == null || phoneField == null) {
            System.err.println("[ERROR] One or more FXML fields are NULL (FXML mismatch?)");
        }
    }


    /**
     * Set the user to edit
     */
    public void setUser(UserRow user) {
        System.out.println("[DEBUG] setUser() called");

        if (user == null) {
            System.err.println("[ERROR] setUser() received NULL user");
            return;
        }

        this.currentUser = user;
        System.out.println("[DEBUG] Editing user ID=" + user.getId() + ", role=" + user.getRole());

        populateFields();
    }


    /**
     * Set reference to parent controller
     */
    public void setManageUsersController(ManageUsersController controller) {
        System.out.println("[DEBUG] Parent ManageUsersController injected");

        if (controller == null) {
            System.err.println("[WARN] manageUsersController is NULL");
        }

        this.manageUsersController = controller;
    }


    /**
     * Setup specialization dropdown
     */
    private void setupSpecializationCombo() {
        System.out.println("[DEBUG] Setting up specialization combo");

        if (specializationCombo == null) {
            System.err.println("[WARN] specializationCombo is NULL (probably patient edit)");
            return;
        }

        specializationCombo.getItems().addAll(
                "General Medicine", "Cardiology", "Dermatology", "Emergency Medicine",
                "Endocrinology", "Family Medicine", "Gastroenterology", "Hematology",
                "Internal Medicine", "Neurology", "Obstetrics & Gynecology", "Oncology",
                "Ophthalmology", "Orthopedics", "Pediatrics", "Psychiatry", "Surgery", "Other"
        );

        System.out.println("[DEBUG] specializationCombo populated");
    }
    /**
     * Populate form fields with user data
     */
    private void populateFields() {
        System.out.println("[DEBUG] Populating form fields");

        if (currentUser == null) {
            System.err.println("[ERROR] currentUser is NULL");
            return;
        }

        if (userIdLabel != null) {
            userIdLabel.setText(String.valueOf(currentUser.getId()));
        } else {
            System.err.println("[WARN] userIdLabel is NULL (FXML mismatch?)");
        }

        if (roleLabel != null) {
            roleLabel.setText("Role: " + currentUser.getRole());
        }

        if (nameField != null) {
            nameField.setText(currentUser.getName());
        }

        if (emailField != null) {
            emailField.setText(currentUser.getEmail());
        }

        if (phoneField != null) {
            phoneField.setText(currentUser.getPhone());
        }

        // Birthday
        if (birthdayPicker != null &&
                currentUser.getBirthday() != null &&
                !currentUser.getBirthday().isEmpty()) {
            try {
                birthdayPicker.setValue(LocalDate.parse(currentUser.getBirthday()));
            } catch (Exception e) {
                System.err.println("[WARN] Invalid birthday format: " + currentUser.getBirthday());
            }
        }

        boolean isDoctor = "DOCTOR".equals(currentUser.getRole());
        System.out.println("[DEBUG] isDoctor = " + isDoctor);

        if (addressField != null) {
            addressField.setVisible(isDoctor);
            addressField.setManaged(isDoctor);
            addressField.setText(currentUser.getAddress());
        }

        if (specializationCombo != null) {
            specializationCombo.setVisible(isDoctor);
            specializationCombo.setManaged(isDoctor);
        }

        if (biographyArea != null) {
            biographyArea.setVisible(isDoctor);
            biographyArea.setManaged(isDoctor);
        }

        if (isDoctor && manageUsersController != null) {
            loadDoctorDetails();
        }
    }

    /**
     * Load additional doctor details
     */
    private void loadDoctorDetails() {
        System.out.println("[DEBUG] Loading doctor-specific details");

        if (manageUsersController == null) {
            System.err.println("[ERROR] manageUsersController is NULL");
            return;
        }

        RecordDAO dao = manageUsersController.getRecordDAO();
        CommonModel.DoctorModel doctor = dao.getDoctorById(currentUser.getId());

        if (doctor == null) {
            System.err.println("[WARN] No doctor record found for ID=" + currentUser.getId());
            return;
        }

        System.out.println("[DEBUG] Doctor details loaded");

        specializationCombo.setValue(doctor.specialization);
        biographyArea.setText(doctor.biography);
        addressField.setText(doctor.address);
    }

    /**
     * Handle save button
     */
    @FXML
    private void handleSave(ActionEvent event) {
        System.out.println("[DEBUG] Save button clicked");

        if (!validateForm()) {
            System.err.println("[WARN] Form validation failed");
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all required fields correctly.");
            return;
        }

        boolean success;

        if ("PATIENT".equals(currentUser.getRole())) {
            System.out.println("[DEBUG] Updating PATIENT");
            success = updatePatient();
        } else if ("DOCTOR".equals(currentUser.getRole())) {
            System.out.println("[DEBUG] Updating DOCTOR");
            success = updateDoctor();
        } else {
            System.err.println("[ERROR] Unsupported role: " + currentUser.getRole());
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot edit admin users.");
            return;
        }

        System.out.println("[DEBUG] Update success = " + success);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Edited Successfully");
            manageUsersController.refreshTable();
            handleCancel(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user.");
        }
    }


    /**
     * Update patient information
     */
    private boolean updatePatient() {
        System.out.println("[DEBUG] updatePatient() called");

        return manageUsersController.getRecordDAO().updatePatient(
                currentUser.getId(),
                nameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                birthdayPicker.getValue().toString()
        );
    }

    /**
     * Update doctor information
     */
    private boolean updateDoctor() {
        System.out.println("[DEBUG] updateDoctor() called");

        return manageUsersController.getRecordDAO().updateDoctor(
                currentUser.getId(),
                nameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                specializationCombo.getValue(),
                biographyArea.getText().trim(),
                addressField.getText().trim()
        );
    }


    /**
     * Validate form fields
     */
    private boolean validateForm() {
        System.out.println("[DEBUG] Validating form");

        if (nameField.getText().isBlank()) return false;
        if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) return false;
        if (phoneField.getText().isBlank()) return false;
        if (birthdayPicker.getValue() == null) return false;

        if ("DOCTOR".equals(currentUser.getRole())) {
            if (specializationCombo.getValue() == null) return false;
            if (addressField.getText().isBlank()) return false;
        }

        return true;
    }

    /**
     * Handle cancel button
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        System.out.println("[DEBUG] Cancel clicked, returning to ManageUsers");

        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("ManageUsers.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to return to ManageUsers.fxml");
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
        System.out.println("[DEBUG] Navigation clicked inside EditUser");

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