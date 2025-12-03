package Controller.Doctor;

import CommonModel.MedicalRecord;
import CommonModel.MedicalRecordSummary;
import CommonModel.SessionManager;
import DAO.DatabaseConnection;
import DAO.RecordDAO;
import MVP.Login;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class HistoryController {

    @FXML private Button accountButton;
    @FXML private Button recordsButton;
    @FXML private Button calendarButton;
    @FXML private Button notificationsButton;
    @FXML private Button settingsButton;
    @FXML private VBox recordsListVBox;

    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();

        String targetFxml;

        switch (id) {
            case "accountButton": targetFxml = "DoctorAccount.fxml"; break;
            case "recordsButton": targetFxml = "ViewMedicalRecordList.fxml"; break;
            case "calendarButton": targetFxml = "Calendar.fxml"; break;
            case "notificationsButton": targetFxml = "Chat.fxml"; break;
            case "settingsButton": targetFxml = "DoctorSettings.fxml"; break;
            default:
                System.out.println("Unknown navigation button: " + id);
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
    public void initialize() {
        // FIXED: Use getDoctorID() instead of getUserID()
        int doctorID = SessionManager.getDoctorID();

        System.out.println("=== DEBUG: Doctor ID from SessionManager: " + doctorID);

        List<MedicalRecordSummary> records = RecordDAO.getDoctorMedicalRecords(doctorID);

        System.out.println("=== DEBUG: Number of records found: " + records.size());

        if (records.isEmpty()) {
            Label empty = new Label("No medical records found for your patients.");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            recordsListVBox.getChildren().add(empty);
            return;
        }

        for (MedicalRecordSummary summary : records) {
            addRecordItem(summary);
        }
    }

    private void addRecordItem(MedicalRecordSummary summary) {
        HBox row = new HBox(20);
        row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;"
                + " -fx-border-color: #cccccc; -fx-border-radius: 8;");
        row.setPrefHeight(60);

        Label name = new Label(summary.patientName);
        name.setPrefWidth(250);
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label diagnosis = new Label(summary.diagnosis);
        diagnosis.setPrefWidth(350);

        Button viewBtn = new Button(summary.recordDate);
        viewBtn.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-background-radius: 5;");
        viewBtn.setOnAction(e -> openRecord(summary.recordID));

        row.getChildren().addAll(name, diagnosis, viewBtn);
        recordsListVBox.getChildren().add(row);
    }

    private void openRecord(int recordID) {
        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("ViewMedicalRecordDoctor.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 768);

            RecordDAO dao = new RecordDAO(DatabaseConnection.getConnection());
            MedicalRecord fullRecord = dao.getRecordByID(recordID);

            if (fullRecord == null) {
                System.err.println("Record not found with ID: " + recordID);
                return;
            }

            ViewMedicalRecordDoctorController controller = loader.getController();
            controller.setRecord(fullRecord);

            Stage stage = (Stage) recordsListVBox.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.err.println("Error opening medical record: " + e.getMessage());
            e.printStackTrace();
        }
    }
}