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

/**
 * Controller for Doctor Medical Record Management
 * Handles creating and editing medical records for patients
 */
public class DoctorMedicalRecordController {

    // Patient Information Fields
    @FXML private TextField patientNameField;
    @FXML private TextField ageField;

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
            if (currentPatientID <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "No patient selected.");
                return;
            }

            // Validate that at least some information is entered
            if (!hasAnyData()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter medical information.");
                return;
            }

            int doctorID = SessionManager.getCurrentUserId();
            if (doctorID <= 0) {
                showAlert(Alert.AlertType.ERROR, "Session Error", "No doctor session found.");
                return;
            }

            // Create a MedicalRecordModel with all the field data
            MedicalRecordModel record = buildRecordFromFields();
            record.setPatientId(currentPatientID);
            record.setDoctorId(doctorID);

            boolean success;

            if (isEditMode && currentRecordID > 0) {
                record.setRecordId(currentRecordID);
                success = recordDAO.updateMedicalRecordComplete(record);
                showAlert(
                        success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        success ? "Success" : "Error",
                        success ? "Medical record updated." : "Failed to update record."
                );
            } else {
                success = recordDAO.createMedicalRecordComplete(record);
                showAlert(
                        success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        success ? "Success" : "Error",
                        success ? "Medical record created." : "Failed to create record."
                );
            }

            if (success) navigateToRecordList();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    /**
     * Check if any data has been entered
     */
    private boolean hasAnyData() {
        return (subjField != null && !subjField.getText().trim().isEmpty()) ||
                (ccField != null && !ccField.getText().trim().isEmpty()) ||
                (hpiField != null && !hpiField.getText().trim().isEmpty()) ||
                hasOPQRSTData() ||
                (pmiField != null && !pmiField.getText().trim().isEmpty()) ||
                (medField != null && !medField.getText().trim().isEmpty()) ||
                (allergiesField != null && !allergiesField.getText().trim().isEmpty()) ||
                (fhField != null && !fhField.getText().trim().isEmpty()) ||
                (shField != null && !shField.getText().trim().isEmpty()) ||
                (rosField != null && !rosField.getText().trim().isEmpty()) ||
                (notesArea != null && !notesArea.getText().trim().isEmpty());
    }

    /**
     * Build a complete MedicalRecordModel from all form fields
     */
    private MedicalRecordModel buildRecordFromFields() {
        MedicalRecordModel record = new MedicalRecordModel();

        // Patient information
        record.setPatientName(getFieldText(patientNameField));
        try {
            record.setAge(Integer.parseInt(getFieldText(ageField)));
        } catch (NumberFormatException e) {
            record.setAge(0);
        }

        // Subjective section
        record.setSubjective(getFieldText(subjField));
        record.setChiefComplaint(getFieldText(ccField));
        record.setHistoryPresentIllness(getFieldText(hpiField));

        // OPQRST Assessment
        record.setOnset(getFieldText(onsetField));
        record.setLocation(getFieldText(locationField));
        record.setDuration(getFieldText(durationField));
        record.setCharacteristics(getFieldText(characField));
        record.setAggravatingFactors(getFieldText(factorsField));
        record.setRadiation(getFieldText(radiationField));
        record.setSeverity(getFieldText(severityField));

        // Medical History
        record.setPastMedicalIllness(getFieldText(pmiField));
        record.setMedications(getFieldText(medField));
        record.setAllergies(getFieldText(allergiesField));
        record.setFamilyHistory(getFieldText(fhField));
        record.setSocialHistory(getFieldText(shField));
        record.setReviewOfSystems(getFieldText(rosField));

        return record;
    }

    /**
     * Helper method to get text from a TextField safely
     */
    private String getFieldText(TextField field) {
        if (field == null) return "";
        return field.getText() != null ? field.getText().trim() : "";
    }

    /**
     * Helper method to get text from a TextArea safely
     */
    private String getFieldText(TextArea field) {
        if (field == null) return "";
        return field.getText() != null ? field.getText().trim() : "";
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
     * Load a medical record for editing
     */
    public void loadRecordForEdit(MedicalRecordModel record) {
        if (record == null) return;

        isEditMode = true;
        currentRecordID = record.getRecordId();
        currentPatientID = record.getPatientId();

        loadPatientInfo(currentPatientID);

        // Load all fields from the record
        if (record.getPatientName() != null && patientNameField != null) {
            patientNameField.setText(record.getPatientName());
        }
        if (ageField != null) {
            ageField.setText(String.valueOf(record.getAge()));
        }

        // Subjective section
        if (subjField != null && record.getSubjective() != null) {
            subjField.setText(record.getSubjective());
        }
        if (ccField != null && record.getChiefComplaint() != null) {
            ccField.setText(record.getChiefComplaint());
        }
        if (hpiField != null && record.getHistoryPresentIllness() != null) {
            hpiField.setText(record.getHistoryPresentIllness());
        }

        // OPQRST Assessment
        if (onsetField != null && record.getOnset() != null) {
            onsetField.setText(record.getOnset());
        }
        if (locationField != null && record.getLocation() != null) {
            locationField.setText(record.getLocation());
        }
        if (durationField != null && record.getDuration() != null) {
            durationField.setText(record.getDuration());
        }
        if (characField != null && record.getCharacteristics() != null) {
            characField.setText(record.getCharacteristics());
        }
        if (factorsField != null && record.getAggravatingFactors() != null) {
            factorsField.setText(record.getAggravatingFactors());
        }
        if (radiationField != null && record.getRadiation() != null) {
            radiationField.setText(record.getRadiation());
        }
        if (severityField != null && record.getSeverity() != null) {
            severityField.setText(record.getSeverity());
        }

        // Medical History
        if (pmiField != null && record.getPastMedicalIllness() != null) {
            pmiField.setText(record.getPastMedicalIllness());
        }
        if (medField != null && record.getMedications() != null) {
            medField.setText(record.getMedications());
        }
        if (allergiesField != null && record.getAllergies() != null) {
            allergiesField.setText(record.getAllergies());
        }
        if (fhField != null && record.getFamilyHistory() != null) {
            fhField.setText(record.getFamilyHistory());
        }
        if (shField != null && record.getSocialHistory() != null) {
            shField.setText(record.getSocialHistory());
        }
        if (rosField != null && record.getReviewOfSystems() != null) {
            rosField.setText(record.getReviewOfSystems());
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