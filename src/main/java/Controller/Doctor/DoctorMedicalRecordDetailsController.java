package Controller.Doctor;

import CommonModel.MedicalRecordModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import MVP.Login;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Controller for displaying detailed medical record information
 * in a structured table format
 */
public class DoctorMedicalRecordDetailsController {

    // Patient Details
    @FXML private Label recordIdLabel;
    @FXML private Label patientNameLabel;
    @FXML private Label patientAgeLabel;

    // Subjective Section
    @FXML private Label subjectiveLabel;
    @FXML private Label chiefComplaintLabel;
    @FXML private Label hpiLabel;

    // OPQRST Assessment
    @FXML private Label onsetLabel;
    @FXML private Label locationLabel;
    @FXML private Label durationLabel;
    @FXML private Label characterLabel;
    @FXML private Label aggravatingFactorsLabel;
    @FXML private Label radiationLabel;
    @FXML private Label severityLabel;

    // Medical History
    @FXML private Label pmiLabel;
    @FXML private Label medicationLabel;
    @FXML private Label allergiesLabel;
    @FXML private Label fhLabel;
    @FXML private Label shLabel;
    @FXML private Label rosLabel;

    // Buttons
    @FXML private Button backButton;

    private MedicalRecordModel currentRecord;

    @FXML
    public void initialize() {
        System.out.println("Medical Record Details Controller Initialized");
    }

    /**
     * Load medical record data into the view
     * Call this method after the scene is loaded
     */
    public void loadMedicalRecord(MedicalRecordModel record) {
        if (record == null) {
            System.err.println("No medical record provided");
            return;
        }

        this.currentRecord = record;

        // Patient Details
        recordIdLabel.setText("ID: " + record.getRecordId());
        patientNameLabel.setText("Name: " + (record.getPatientName() != null ? record.getPatientName() : "N/A"));
        patientAgeLabel.setText("Age: " + record.getAge());

        // Subjective
        if (record.getSubjective() != null && !record.getSubjective().trim().isEmpty()) {
            subjectiveLabel.setText(record.getSubjective());
        } else {
            subjectiveLabel.setText("No information provided");
        }

        // Chief Complaint
        if (record.getChiefComplaint() != null && !record.getChiefComplaint().trim().isEmpty()) {
            chiefComplaintLabel.setText("CC: " + record.getChiefComplaint());
        } else {
            chiefComplaintLabel.setText("CC: No information provided");
        }

        // History of Present Illness
        if (record.getHistoryPresentIllness() != null && !record.getHistoryPresentIllness().trim().isEmpty()) {
            hpiLabel.setText(record.getHistoryPresentIllness());
        } else {
            hpiLabel.setText("No information provided");
        }

        // OPQRST Assessment
        onsetLabel.setText("Onset: " + (record.getOnset() != null && !record.getOnset().trim().isEmpty() ? record.getOnset() : "N/A"));
        locationLabel.setText("Location: " + (record.getLocation() != null && !record.getLocation().trim().isEmpty() ? record.getLocation() : "N/A"));
        durationLabel.setText("Duration: " + (record.getDuration() != null && !record.getDuration().trim().isEmpty() ? record.getDuration() : "N/A"));
        characterLabel.setText("Character: " + (record.getCharacteristics() != null && !record.getCharacteristics().trim().isEmpty() ? record.getCharacteristics() : "N/A"));

        if (record.getAggravatingFactors() != null && !record.getAggravatingFactors().trim().isEmpty()) {
            aggravatingFactorsLabel.setText(record.getAggravatingFactors());
        } else {
            aggravatingFactorsLabel.setText("N/A");
        }

        if (record.getRadiation() != null && !record.getRadiation().trim().isEmpty()) {
            radiationLabel.setText(record.getRadiation());
        } else {
            radiationLabel.setText("N/A");
        }

        if (record.getSeverity() != null && !record.getSeverity().trim().isEmpty()) {
            severityLabel.setText(record.getSeverity());
        } else {
            severityLabel.setText("N/A");
        }

        // Medical History
        pmiLabel.setText("PMI: " + (record.getPastMedicalIllness() != null && !record.getPastMedicalIllness().trim().isEmpty() ? record.getPastMedicalIllness() : "None"));
        medicationLabel.setText("Medication: " + (record.getMedications() != null && !record.getMedications().trim().isEmpty() ? record.getMedications() : "None"));
        allergiesLabel.setText("Allergies: " + (record.getAllergies() != null && !record.getAllergies().trim().isEmpty() ? record.getAllergies() : "NKDA"));
        fhLabel.setText("FH: " + (record.getFamilyHistory() != null && !record.getFamilyHistory().trim().isEmpty() ? record.getFamilyHistory() : "Non-contributory"));
        shLabel.setText("SH: " + (record.getSocialHistory() != null && !record.getSocialHistory().trim().isEmpty() ? record.getSocialHistory() : "N/A"));
        rosLabel.setText("ROS: " + (record.getReviewOfSystems() != null && !record.getReviewOfSystems().trim().isEmpty() ? record.getReviewOfSystems() : "N/A"));
    }

    /**
     * Handle back button click
     */
    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/History.fxml"));
            Scene scene = new Scene(loader.load(), 1366, 768);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to navigate back to medical records list");
        }
    }

    /**
     * Handle navigation between different views
     */
    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        String targetFxml = "";

        switch (buttonId) {
            case "accountButton":
                targetFxml = "/MVP/DoctorAccount.fxml";
                break;
            case "casesButton":
                targetFxml = "/MVP/History.fxml";
                break;
            case "calendarButton":
                targetFxml = "/MVP/Calendar.fxml";
                break;
            case "notificationButton":
                targetFxml = "/MVP/Chat.fxml";
                break;
            case "settingsButton":
                targetFxml = "/MVP/DoctorSettings.fxml";
                break;
            default:
                System.out.println("Unknown navigation target: " + buttonId);
                return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(targetFxml));
            Scene scene = new Scene(loader.load(), 1366, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to navigate to " + targetFxml);
        }
    }
}