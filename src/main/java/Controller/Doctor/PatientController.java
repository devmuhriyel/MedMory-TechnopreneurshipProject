package Controller.Doctor;

import CommonModel.DoctorCase;
import CommonModel.SessionManager;
import CommonModel.UserRow;
import DAO.DatabaseConnection;
import MVP.Doctor.DoctorService;
import MVP.Login;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class PatientController {

    @FXML private TableView<UserRow> userTable;
    @FXML private TableColumn<UserRow, String> nameColumn;
    @FXML private TableColumn<UserRow, String> detailColumn;

    private List<DoctorCase> allCases;


    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();

        String targetFxml;

        switch (id) {
            case "accountButton":
                targetFxml = "DoctorAccount.fxml";
                break;
            case "records":
            case "recordsButton":
                targetFxml = "ViewMedicalRecordList.fxml";
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
        int doctorID = SessionManager.getDoctorID();
        System.out.println("PatientController initialized. Doctor ID = " + doctorID);

        if (userTable != null) {
            setupPatientTable();
            loadPatients(doctorID);
        }
    }

    private void setupPatientTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        detailColumn.setCellValueFactory(new PropertyValueFactory<>("actionText"));
    }

    private void loadPatients(int doctorID) {
        allCases = DoctorService.getCasesHandledByDoctor(doctorID);

        if (allCases == null || allCases.isEmpty()) {
            System.out.println("No patients found for doctor ID: " + doctorID);
            return;
        }

        Set<Integer> addedPatients = new HashSet<>();
        userTable.getItems().clear();

        for (DoctorCase c : allCases) {
            if (!addedPatients.contains(c.getPatientID())) {
                addedPatients.add(c.getPatientID());
                UserRow row = new UserRow(c.getPatientID(), c.getPatientName());
                userTable.getItems().add(row);
                System.out.println("Added to table: " + c.getPatientID() + " - " + c.getPatientName());
            }
        }
    }

    /**
     * Opens the History/Edit window for the selected patient
     */
    public void goToHistory() {
        UserRow selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("Please select a patient.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("History.fxml"));
            Parent root = loader.load();

            // Pass the selected patient to the controller
            HistoryController controller = loader.getController();
            controller.setPatient(selected); // similar to EditUserController setUser()

            // Get current stage from any injected node (here we use userTable)
            Stage stage = (Stage) userTable.getScene().getWindow();

            // Replace the scene in the existing stage
            stage.setScene(new Scene(root, 1280, 768));
            stage.setTitle("Patient History - " + selected.getName());
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to open History.fxml");
        }
    }

}
