package Controller.Client;

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

public class MedicalRecordController {

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
    private int patientID;

    @FXML
    public void initialize() {
        Connection conn = DatabaseConnection.getConnection();
        recordDAO = new RecordDAO(conn);

        // GET LOGGED-IN CLIENT
        int userID = SessionManager.getUserID();

        if (userID > 0) {
            loadRecord(userID);
        } else {
            System.out.println("⚠ No user session found!");
        }
    }

    /**
     * Optional: Allows the patient to submit/update their record info.
     * Note: This adds a new entry to the Blockchain.
     */
    @FXML
    public void handleSaveRecord() {
        try {
            MedicalRecord record = new MedicalRecord();

            record.setPatientID(patientID);
            record.setPatientName(patientNameField.getText());
            record.setAge(Integer.parseInt(ageField.getText()));

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

            // In a patient view, doctorID might be 0 or a default value
            record.setDoctorID(0);

            boolean success = recordDAO.insertRecord(record);

            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Your information has been updated and secured.");
                alert.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadRecord(int patientID) {
        this.patientID = patientID;

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

    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        String targetFxml = "";

        switch (buttonId) {
            case "homeButton": targetFxml = "ClientHomepage.fxml"; break;
            case "accountButton": targetFxml = "ClientAccount.fxml"; break;
            case "casesButton": targetFxml = "MedicalRecord.fxml"; break;
            case "calendarButton": targetFxml = "ClientCalendar.fxml"; break;
            case "settingsButton": targetFxml = "ClientSettings.fxml"; break;
            default: return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource(targetFxml));
            Scene scene = new Scene(fxmlLoader.load(), 1280, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void clearForm() {
        patientNameField.clear();
        ageField.clear();
        subjField.clear(); ccField.clear(); hpiField.clear();
        onsetField.clear(); locationField.clear(); durationField.clear();
        characField.clear(); factorsField.clear(); radiationField.clear();
        severityField.clear(); pmiField.clear(); medField.clear();
        allergiesField.clear(); fhField.clear(); shField.clear(); rosField.clear();
    }
}