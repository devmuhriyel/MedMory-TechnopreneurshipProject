package Controller.Doctor;

import DAO.DatabaseConnection;
import DAO.RecordDAO;
import CommonModel.MedicalRecord;
import MVP.Login;
import CommonModel.SessionManager;
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

public class DoctorMedicalRecordController {

    @FXML private TextField patientNameField;
    @FXML private TextField ageField;

    @FXML private TextArea subjField;
    @FXML private TextArea ccField;
    @FXML private TextArea hpiField;

    @FXML private TextField onsetField;
    @FXML private TextField locationField;
    @FXML private TextField durationField;
    @FXML private TextField characField;
    @FXML private TextField factorsField;
    @FXML private TextField radiationField;
    @FXML private TextField severityField;

    @FXML private TextArea pmiField;
    @FXML private TextArea medField;
    @FXML private TextArea allergiesField;
    @FXML private TextArea fhField;
    @FXML private TextArea shField;
    @FXML private TextArea rosField;

    private RecordDAO recordDAO;
    private int currentPatientID;
    private int currentRecordID; // For edit mode
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        Connection conn = DatabaseConnection.getConnection();
        recordDAO = new RecordDAO(conn);

        // Get the ID of the patient currently being viewed
        this.currentPatientID = SessionManager.getUserID();

        if (currentPatientID > 0) {
            loadRecord(currentPatientID);
        } else {
            System.out.println("⚠ No patient selected or session found!");
        }
    }

    /**
     * Gathers data from the UI fields and saves it to the database
     * while linking it to the blockchain.
     * Handles both CREATE and UPDATE operations.
     */
    @FXML
    public void handleSaveRecord() {
        try {
            // Validate required fields
            if (patientNameField.getText().trim().isEmpty()) {
                showSimpleAlert(Alert.AlertType.WARNING, "Validation Error", "Patient name is required.");
                return;
            }
            
            if (ageField.getText().trim().isEmpty()) {
                showSimpleAlert(Alert.AlertType.WARNING, "Validation Error", "Age is required.");
                return;
            }

            // 1. Create a MedicalRecord object
            MedicalRecord record = new MedicalRecord();

            // 2. Map UI fields to the object
            if (isEditMode) {
                record.setIdrecords(currentRecordID);
            }
            
            record.setPatientID(currentPatientID > 0 ? currentPatientID : 0); // Use 0 if not set
            record.setPatientName(patientNameField.getText().trim());
            record.setAge(Integer.parseInt(ageField.getText().trim()));
            record.setDoctorID(SessionManager.getDoctorID());

            record.setSubj(subjField.getText());
            record.setCc(ccField.getText());
            record.setHpi(hpiField.getText());
            record.setOnset(onsetField.getText());
            record.setLocation(locationField.getText());
            record.setDuration(durationField.getText());
            record.setCharacter(characField.getText());
            record.setFactors(factorsField.getText());
            record.setRadiation(radiationField.getText());
            record.setSeverity(severityField.getText());

            record.setPmi(pmiField.getText());
            record.setMed(medField.getText());
            record.setAllergies(allergiesField.getText());
            record.setFh(fhField.getText());
            record.setSh(shField.getText());
            record.setRos(rosField.getText());

            // 3. Call RecordDAO to insert or update the record with Blockchain hashing
            boolean success;
            if (isEditMode) {
                success = recordDAO.updateRecord(record);
                if (success) {
                    showSimpleAlert(Alert.AlertType.INFORMATION, "Success", "Medical record successfully updated and blockchain re-validated.");
                    // Navigate back to list
                    navigateToRecordList();
                } else {
                    showSimpleAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update the record in the database.");
                }
            } else {
                success = recordDAO.insertRecord(record);
                if (success) {
                    showSimpleAlert(Alert.AlertType.INFORMATION, "Success", "Medical record successfully saved and secured in the blockchain.");
                    // Navigate back to list
                    navigateToRecordList();
                } else {
                    showSimpleAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save the record to the database.");
                }
            }

        } catch (NumberFormatException e) {
            showSimpleAlert(Alert.AlertType.WARNING, "Input Error", "Please ensure the Age field contains a valid number.");
        } catch (SQLException e) {
            e.printStackTrace();
            showSimpleAlert(Alert.AlertType.ERROR, "Blockchain Error", "A database error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Loads a record for editing
     */
    public void loadRecordForEdit(MedicalRecord record) {
        isEditMode = true;
        currentRecordID = record.getIdrecords();
        currentPatientID = record.getPatientID();
        
        // Populate fields
        patientNameField.setText(record.getPatientName());
        ageField.setText(String.valueOf(record.getAge()));
        subjField.setText(record.getSubj());
        ccField.setText(record.getCc());
        hpiField.setText(record.getHpi());
        onsetField.setText(record.getOnset());
        locationField.setText(record.getLocation());
        durationField.setText(record.getDuration());
        characField.setText(record.getCharacter());
        factorsField.setText(record.getFactors());
        radiationField.setText(record.getRadiation());
        severityField.setText(record.getSeverity());
        pmiField.setText(record.getPmi());
        medField.setText(record.getMed());
        allergiesField.setText(record.getAllergies());
        fhField.setText(record.getFh());
        shField.setText(record.getSh());
        rosField.setText(record.getRos());
    }
    
    private void navigateToRecordList() {
        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("ViewMedicalRecordList.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 768);
            Stage stage = (Stage) patientNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadRecord(int patientID) {
        this.currentPatientID = patientID;

        try {
            MedicalRecord r = recordDAO.getRecordByPatientID(patientID);

            if (r == null) {
                clearForm();
                return;
            }

            patientNameField.setText(r.getPatientName());
            ageField.setText(String.valueOf(r.getAge()));
            subjField.setText(r.getSubj());
            ccField.setText(r.getCc());
            hpiField.setText(r.getHpi());
            onsetField.setText(r.getOnset());
            locationField.setText(r.getLocation());
            durationField.setText(r.getDuration());
            characField.setText(r.getCharacter());
            factorsField.setText(r.getFactors());
            radiationField.setText(r.getRadiation());
            severityField.setText(r.getSeverity());
            pmiField.setText(r.getPmi());
            medField.setText(r.getMed());
            allergiesField.setText(r.getAllergies());
            fhField.setText(r.getFh());
            shField.setText(r.getSh());
            rosField.setText(r.getRos());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSimpleAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        String targetFxml = "";

        switch (buttonId) {
            case "accountButton": targetFxml = "DoctorAccount.fxml"; break;
            case "records": targetFxml = "ViewMedicalRecordList.fxml"; break;
            case "casesButton": targetFxml = "History.fxml"; break;
            case "calendarButton": targetFxml = "Calendar.fxml"; break;
            case "notificationsButton": targetFxml = "Chat.fxml"; break;
            case "settingsButton": targetFxml = "DoctorSettings.fxml"; break;
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
        }
    }

    @FXML
    public void clearForm(ActionEvent event) {
        clearForm();
    }
    
    public void clearForm() {
        patientNameField.clear();
        ageField.clear();
        subjField.clear();
        ccField.clear();
        hpiField.clear();
        onsetField.clear();
        locationField.clear();
        durationField.clear();
        characField.clear();
        factorsField.clear();
        radiationField.clear();
        severityField.clear();
        pmiField.clear();
        medField.clear();
        allergiesField.clear();
        fhField.clear();
        shField.clear();
        rosField.clear();
        currentPatientID = 0; // Reset patient ID
    }
}