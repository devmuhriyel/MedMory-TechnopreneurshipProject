package Controller.Doctor;

import CommonModel.MedicalRecordModel;
import CommonModel.PatientModel;
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
import java.sql.SQLException;

/**
 * Controller for Doctor Medical Record Management
 * Handles creating and editing medical records for patients
 */
public class DoctorMedicalRecordController {

    // Patient Information Fields
    @FXML private TextField patientNameField;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> recordTypeCombo;

    // Subjective Section
    @FXML private TextArea subjField;
    @FXML private TextArea ccField;  // Chief Complaint
    @FXML private TextArea hpiField; // History of Present Illness

    // OPQRST Assessment Fields
    @FXML private TextField onsetField;
    @FXML private TextField locationField;
    @FXML private TextField durationField;
    @FXML private TextField characField;     // Character
    @FXML private TextField factorsField;     // Aggravating/Alleviating Factors
    @FXML private TextField radiationField;
    @FXML private TextField severityField;

    // History Fields
    @FXML private TextArea pmiField;        // Past Medical History
    @FXML private TextArea medField;        // Medications
    @FXML private TextArea allergiesField;
    @FXML private TextArea fhField;         // Family History
    @FXML private TextArea shField;         // Social History
    @FXML private TextArea rosField;        // Review of Systems

    // Main Notes Field (combines all information)
    @FXML private TextArea notesArea;

    private RecordDAO recordDAO;
    private int currentPatientID;
    private int currentRecordID;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        System.out.println("Doctor Medical Record Controller Initialized");

        // Initialize DAO
        Connection conn = DatabaseConnection.getConnection();
        recordDAO = new RecordDAO(conn);

        // Initialize record type combo box
        if (recordTypeCombo != null) {
            recordTypeCombo.getItems().addAll(
                    "Consultation",
                    "Diagnosis",
                    "Treatment",
                    "Lab Results",
                    "Prescription",
                    "Follow-up",
                    "Emergency"
            );
            recordTypeCombo.setValue("Consultation");
        }

        // Get the current patient from session (if set)
        this.currentPatientID = SessionManager.getCurrentPatientId();

        if (currentPatientID > 0) {
            loadPatientInfo(currentPatientID);
        } else {
            System.out.println("⚠ No patient selected or session found!");
        }
    }

    /**
     * Load patient information
     */
    private void loadPatientInfo(int patientID) {
        try {
            PatientModel patient = recordDAO.getPatientById(patientID);

            if (patient != null) {
                patientNameField.setText(patient.getName());
                // Calculate age from birthday if needed
                if (ageField != null && patient.getBirthday() != null) {
                    int age = calculateAge(patient.getBirthday());
                    ageField.setText(String.valueOf(age));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading patient info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Calculate age from birthday string (YYYY-MM-DD format)
     */
    private int calculateAge(String birthday) {
        try {
            String[] parts = birthday.split("-");
            int birthYear = Integer.parseInt(parts[0]);
            int currentYear = java.time.Year.now().getValue();
            return currentYear - birthYear;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Saves the medical record to the database
     * Handles both CREATE and UPDATE operations
     */
    @FXML
    public void handleSaveRecord() {
        try {
            // Validate required fields
            if (currentPatientID <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "No patient selected.");
                return;
            }

            String recordType = recordTypeCombo != null ? recordTypeCombo.getValue() : "Consultation";
            if (recordType == null || recordType.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a record type.");
                return;
            }

            // Build comprehensive notes from all fields
            String notes = buildNotesFromFields();

            if (notes.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter medical information.");
                return;
            }

            int doctorID = SessionManager.getCurrentUserId();
            if (doctorID <= 0) {
                showAlert(Alert.AlertType.ERROR, "Session Error", "No doctor session found. Please log in again.");
                return;
            }

            boolean success;

            if (isEditMode && currentRecordID > 0) {
                // Update existing record
                success = recordDAO.updateMedicalRecord(currentRecordID, notes, doctorID);

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Medical record successfully updated.");
                    navigateToRecordList();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update the medical record.");
                }
            } else {
                // Create new record
                success = recordDAO.createMedicalRecord(currentPatientID, doctorID, recordType, notes);

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Medical record successfully created.");
                    navigateToRecordList();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create the medical record.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    /**
     * Build comprehensive notes from all form fields
     */
    private String buildNotesFromFields() {
        StringBuilder notes = new StringBuilder();

        // If there's a main notes area, use that
        if (notesArea != null && !notesArea.getText().trim().isEmpty()) {
            return notesArea.getText().trim();
        }

        // Otherwise, build from individual fields
        appendSection(notes, "SUBJECTIVE", subjField);
        appendSection(notes, "Chief Complaint", ccField);
        appendSection(notes, "History of Present Illness", hpiField);

        // OPQRST Assessment
        if (hasOPQRSTData()) {
            notes.append("\n=== OPQRST ASSESSMENT ===\n");
            appendField(notes, "Onset", onsetField);
            appendField(notes, "Location", locationField);
            appendField(notes, "Duration", durationField);
            appendField(notes, "Character", characField);
            appendField(notes, "Aggravating/Alleviating Factors", factorsField);
            appendField(notes, "Radiation", radiationField);
            appendField(notes, "Severity", severityField);
        }

        // Medical History
        appendSection(notes, "Past Medical History", pmiField);
        appendSection(notes, "Current Medications", medField);
        appendSection(notes, "Allergies", allergiesField);
        appendSection(notes, "Family History", fhField);
        appendSection(notes, "Social History", shField);
        appendSection(notes, "Review of Systems", rosField);

        return notes.toString().trim();
    }

    /**
     * Check if any OPQRST fields have data
     */
    private boolean hasOPQRSTData() {
        return (onsetField != null && !onsetField.getText().trim().isEmpty()) ||
                (locationField != null && !locationField.getText().trim().isEmpty()) ||
                (durationField != null && !durationField.getText().trim().isEmpty()) ||
                (characField != null && !characField.getText().trim().isEmpty()) ||
                (factorsField != null && !factorsField.getText().trim().isEmpty()) ||
                (radiationField != null && !radiationField.getText().trim().isEmpty()) ||
                (severityField != null && !severityField.getText().trim().isEmpty());
    }

    /**
     * Append a section to notes if it has content
     */
    private void appendSection(StringBuilder notes, String title, TextArea field) {
        if (field != null && !field.getText().trim().isEmpty()) {
            notes.append("\n=== ").append(title.toUpperCase()).append(" ===\n");
            notes.append(field.getText().trim()).append("\n");
        }
    }

    /**
     * Append a field to notes if it has content
     */
    private void appendField(StringBuilder notes, String label, TextField field) {
        if (field != null && !field.getText().trim().isEmpty()) {
            notes.append(label).append(": ").append(field.getText().trim()).append("\n");
        }
    }

    /**
     * Load a medical record for editing
     */
    public void loadRecordForEdit(MedicalRecordModel record) {
        if (record == null) {
            System.err.println("Cannot load null record");
            return;
        }

        isEditMode = true;
        currentRecordID = record.getRecordId();
        currentPatientID = record.getPatientId();

        // Load patient info
        loadPatientInfo(currentPatientID);

        // Set record type
        if (recordTypeCombo != null && record.getRecordType() != null) {
            recordTypeCombo.setValue(record.getRecordType());
        }

        // Set notes - either in main notes area or parse into individual fields
        if (notesArea != null) {
            notesArea.setText(record.getNotes());
        } else {
            // Parse notes into individual fields if they exist
            parseNotesIntoFields(record.getNotes());
        }
    }

    /**
     * Parse notes string into individual fields
     * This is a simple parser - you may want to enhance it
     */
    private void parseNotesIntoFields(String notes) {
        if (notes == null || notes.trim().isEmpty()) {
            return;
        }

        // For now, just put everything in the subjective field
        // You can enhance this to parse structured notes
        if (subjField != null) {
            subjField.setText(notes);
        }
    }

    /**
     * Load the latest record for a patient
     */
    public void loadRecord(int patientID) {
        this.currentPatientID = patientID;
        loadPatientInfo(patientID);

        // Load the most recent record if exists
        try {
            var records = recordDAO.getMedicalRecordsByPatient(patientID);
            if (!records.isEmpty()) {
                MedicalRecordModel latestRecord = records.get(0); // Most recent first
                loadRecordForEdit(latestRecord);
            } else {
                clearForm();
            }
        } catch (Exception e) {
            e.printStackTrace();
            clearForm();
        }
    }

    /**
     * Navigate back to the record list
     */
    private void navigateToRecordList() {
        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("ViewMedicalRecordList.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 768);
            Stage stage = (Stage) patientNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to record list.");
        }
    }

    /**
     * Handle navigation between different views
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

        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource(targetFxml));
            Scene scene = new Scene(loader.load(), 1280, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to " + targetFxml);
        }
    }

    /**
     * Clear the form
     */
    @FXML
    public void clearForm(ActionEvent event) {
        clearForm();
    }

    /**
     * Clear all form fields
     */
    public void clearForm() {
        isEditMode = false;
        currentRecordID = 0;

        if (patientNameField != null) patientNameField.clear();
        if (ageField != null) ageField.clear();
        if (recordTypeCombo != null) recordTypeCombo.setValue("Consultation");

        if (subjField != null) subjField.clear();
        if (ccField != null) ccField.clear();
        if (hpiField != null) hpiField.clear();

        if (onsetField != null) onsetField.clear();
        if (locationField != null) locationField.clear();
        if (durationField != null) durationField.clear();
        if (characField != null) characField.clear();
        if (factorsField != null) factorsField.clear();
        if (radiationField != null) radiationField.clear();
        if (severityField != null) severityField.clear();

        if (pmiField != null) pmiField.clear();
        if (medField != null) medField.clear();
        if (allergiesField != null) allergiesField.clear();
        if (fhField != null) fhField.clear();
        if (shField != null) shField.clear();
        if (rosField != null) rosField.clear();

        if (notesArea != null) notesArea.clear();
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
     * Cancel and return to previous screen
     */
    @FXML
    public void handleCancel(ActionEvent event) {
        navigateToRecordList();
    }
}