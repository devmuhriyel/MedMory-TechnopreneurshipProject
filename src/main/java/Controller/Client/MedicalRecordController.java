package Controller.Client;

import CommonModel.MedicalRecordModel;
import DAO.DatabaseConnection;
import DAO.RecordDAO;
import MVP.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MedicalRecordController {

    // FXML Injections matching your Table-based UI
    @FXML private TableView<MedicalRecordModel> medicalRecordsTable;
    @FXML private TableColumn<MedicalRecordModel, String> recordTypeCol;
    @FXML private TableColumn<MedicalRecordModel, String> doctorNameCol;
    @FXML private TableColumn<MedicalRecordModel, String> createdAtCol;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;

    private ObservableList<MedicalRecordModel> recordList = FXCollections.observableArrayList();
    private RecordDAO recordDAO;

    @FXML
    public void initialize() {
        // Setup table columns to match MedicalRecordModel properties
        recordTypeCol.setCellValueFactory(new PropertyValueFactory<>("recordType"));
        doctorNameCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        createdAtCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Initialize ComboBox options
        if (filterType != null) {
            filterType.setItems(FXCollections.observableArrayList(
                    "All Types",
                    "Consultation",
                    "Diagnosis",
                    "Treatment",
                    "Lab Results",
                    "Prescription"
            ));
            filterType.setValue("All Types");
        }

        // Initialize DAO
        Connection conn = DatabaseConnection.getConnection();
        recordDAO = new RecordDAO(conn);

        // Load data for the logged-in user
        loadMedicalRecords();
    }

    /**
     * Load all medical records for the current patient using RecordDAO
     */
    private void loadMedicalRecords() {
        recordList.clear();
        int patientID = SessionManager.getCurrentPatientId();

        try {
            List<MedicalRecordModel> records = recordDAO.getMedicalRecordsByPatient(patientID);
            recordList.addAll(records);
            medicalRecordsTable.setItems(recordList);

        } catch (Exception e) {
            showError("Error Loading Records", "Failed to load medical records: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Refresh the table data
     */
    @FXML
    private void handleRefresh() {
        loadMedicalRecords();
        showInfo("Refreshed", "Medical records have been refreshed.");
    }

    /**
     * View the selected medical record details
     */
    @FXML
    private void handleViewRecord() {
        MedicalRecordModel selected = medicalRecordsTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            showRecordDetails(selected);
        } else {
            showError("No Selection", "Please select a medical record to view.");
        }
    }

    /**
     * Display detailed information about a medical record
     */
    private void showRecordDetails(MedicalRecordModel record) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Medical Record Details");
        alert.setHeaderText("Record ID: " + record.getRecordId());

        String details = String.format(
                "Record Type: %s\n" +
                        "Doctor: %s\n" +
                        "Created: %s\n\n" +
                        "Notes:\n%s",
                record.getRecordType(),
                record.getDoctorName(),
                record.getCreatedAt(),
                record.getNotes()
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    /**
     * Filter records by type
     */
    @FXML
    private void handleFilter() {
        String selectedType = filterType.getValue();

        if (selectedType == null || selectedType.equals("All Types")) {
            loadMedicalRecords();
            return;
        }

        ObservableList<MedicalRecordModel> filteredList = FXCollections.observableArrayList();
        int patientID = SessionManager.getCurrentPatientId();

        try {
            List<MedicalRecordModel> allRecords = recordDAO.getMedicalRecordsByPatient(patientID);

            for (MedicalRecordModel record : allRecords) {
                if (record.getRecordType().equalsIgnoreCase(selectedType)) {
                    filteredList.add(record);
                }
            }

            medicalRecordsTable.setItems(filteredList);

        } catch (Exception e) {
            showError("Filter Error", "Failed to filter records: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Search records by keyword
     */
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            loadMedicalRecords();
            return;
        }

        ObservableList<MedicalRecordModel> searchResults = FXCollections.observableArrayList();
        int patientID = SessionManager.getCurrentPatientId();

        try {
            List<MedicalRecordModel> allRecords = recordDAO.getMedicalRecordsByPatient(patientID);

            for (MedicalRecordModel record : allRecords) {
                boolean matches = record.getRecordType().toLowerCase().contains(keyword) ||
                        record.getDoctorName().toLowerCase().contains(keyword) ||
                        record.getNotes().toLowerCase().contains(keyword);

                if (matches) {
                    searchResults.add(record);
                }
            }

            medicalRecordsTable.setItems(searchResults);

        } catch (Exception e) {
            showError("Search Error", "Failed to search records: " + e.getMessage());
            e.printStackTrace();
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
            case "homeButton":
                targetFxml = "/MVP/ClientHomepage.fxml";
                break;
            case "accountButton":
                targetFxml = "/MVP/ClientAccount.fxml";
                break;
            case "medicalRecordButton":
                targetFxml = "/MVP/MedicalRecord.fxml";
                break;
            case "calendarButton":
                targetFxml = "/MVP/ClientCalendar.fxml";
                break;
            case "chatsButton":
                targetFxml = "/MVP/ClientChat.fxml";
                break;
            case "settingsButton":
                targetFxml = "/MVP/ClientSettings.fxml";
                break;
            default:
                return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(targetFxml));
            Scene scene = new Scene(fxmlLoader.load(), 1366, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load: " + targetFxml);
            e.printStackTrace();
            showError("Navigation Error", "Failed to navigate to " + targetFxml);
        }
    }

    /**
     * Show error alert dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show information alert dialog
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}