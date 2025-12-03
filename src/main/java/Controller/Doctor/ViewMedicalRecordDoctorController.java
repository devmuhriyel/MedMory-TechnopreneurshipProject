package Controller.Doctor;

import CommonModel.MedicalRecord;
import MVP.Login;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class ViewMedicalRecordDoctorController {

    @FXML private TextField patientNameField, ageField, onsetField, locationField, durationField,
            characField, factorsField, radiationField, severityField;

    @FXML private TextArea subjField, ccField, hpiField, pmiField, medField,
            allergiesField, fhField, shField, rosField;

    private MedicalRecord record;

    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        String targetFxml = "";

        switch (buttonId) {
            case "accountButton":
                targetFxml = "DoctorAccount.fxml";
                break;
            case "casesButton":
                targetFxml = "History.fxml"; // Stay in current view
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
            Scene scene = new Scene(fxmlLoader.load(), 1280, 768); // Windowed size
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load scene: " + targetFxml);
        }
    }

    public void setRecord(MedicalRecord r) {
        record = r;
        patientNameField.setText(r.patientName);
        ageField.setText(String.valueOf(r.age));
        subjField.setText(r.subj);
        ccField.setText(r.cc);
        hpiField.setText(r.hpi);
        onsetField.setText(r.onset);
        locationField.setText(r.location);
        durationField.setText(r.duration);
        characField.setText(r.charac);
        factorsField.setText(r.factors);
        radiationField.setText(r.radiation);
        severityField.setText(r.severity);
        pmiField.setText(r.pmi);
        medField.setText(r.med);
        allergiesField.setText(r.allergies);
        fhField.setText(r.fh);
        shField.setText(r.sh);
        rosField.setText(r.ros);

        disableEditing();
    }

    private void disableEditing() {
        patientNameField.setEditable(false);
        ageField.setEditable(false);
        subjField.setEditable(false);
        ccField.setEditable(false);
        hpiField.setEditable(false);
        onsetField.setEditable(false);
        locationField.setEditable(false);
        durationField.setEditable(false);
        characField.setEditable(false);
        factorsField.setEditable(false);
        radiationField.setEditable(false);
        severityField.setEditable(false);
        pmiField.setEditable(false);
        medField.setEditable(false);
        allergiesField.setEditable(false);
        fhField.setEditable(false);
        shField.setEditable(false);
        rosField.setEditable(false);
    }

    @FXML
    private void handleBack() throws IOException {
        FXMLLoader loader = new FXMLLoader(Login.class.getResource("ViewMedicalRecordList.fxml"));
        Stage stage = (Stage) ageField.getScene().getWindow();
        stage.setScene(new Scene(loader.load(), 1280, 768));
    }
}
