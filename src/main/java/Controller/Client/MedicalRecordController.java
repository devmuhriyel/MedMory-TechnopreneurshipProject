package Controller.Client;

import CommonModel.MedicalRecordModel;
import DAO.DatabaseConnection;
import DAO.RecordDAO;
import MVP.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Patient Medical Record File Viewing
 * Handles viewing and downloading medical records as documents
 */
public class MedicalRecordController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private TableView<MedicalRecordModel> medicalFilesTable;
    @FXML private TableColumn<MedicalRecordModel, String> fileNameCol;
    @FXML private TableColumn<MedicalRecordModel, String> dateUploadedCol;
    @FXML private Button viewFileButton;
    @FXML private Button downloadButton;
    @FXML private Button refreshButton;

    private RecordDAO recordDAO;
    private ObservableList<MedicalRecordModel> allRecords;
    private ObservableList<MedicalRecordModel> filteredRecords;
    private int currentPatientId;

    @FXML
    public void initialize() {
        System.out.println("Medical Record Controller Initialized");

        // Initialize DAO
        Connection conn = DatabaseConnection.getConnection();
        recordDAO = new RecordDAO(conn);

        // Get current patient from session
        currentPatientId = SessionManager.getCurrentUserId();
        if (currentPatientId == 0) {
            currentPatientId = SessionManager.getCurrentPatientId();
        }

        // Setup table columns
        setupTableColumns();

        // Setup filter combo box
        setupFilterComboBox();

        // Setup search field listener
        setupSearchField();

        // Load medical records
        loadMedicalRecords();

        // Setup double-click to view
        setupTableDoubleClick();
    }

    /**
     * Setup table columns to display medical record information
     */
    private void setupTableColumns() {
        // File Name Column - Display record type or identifier
        fileNameCol.setCellValueFactory(cellData -> {
            MedicalRecordModel record = cellData.getValue();
            String fileName = String.format("Medical Record #%d - %s",
                    record.getRecordId(),
                    record.getPatientName() != null ? record.getPatientName() : "Patient");
            return new SimpleStringProperty(fileName);
        });

        // Date Uploaded Column
        dateUploadedCol.setCellValueFactory(cellData -> {
            MedicalRecordModel record = cellData.getValue();
            String date = "N/A";
            if (record.getCreatedAt() != null) {
                try {
                    date = record.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
                } catch (Exception e) {
                    date = record.getFormattedCreatedAt();
                }
            }
            return new SimpleStringProperty(date);
        });
    }

    /**
     * Setup filter combo box with categories
     */
    private void setupFilterComboBox() {
        ObservableList<String> categories = FXCollections.observableArrayList(
                "All Categories",
                "Recent (Last 30 days)",
                "Older"
        );
        filterType.setItems(categories);
        filterType.setValue("All Categories");

        // Add listener for filter changes
        filterType.setOnAction(event -> applyFilters());
    }

    /**
     * Setup search field with real-time filtering
     */
    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    /**
     * Setup double-click on table row to view record
     */
    private void setupTableDoubleClick() {
        medicalFilesTable.setRowFactory(tv -> {
            TableRow<MedicalRecordModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewFile();
                }
            });
            return row;
        });
    }

    /**
     * Load medical records for the current patient
     */
    private void loadMedicalRecords() {
        try {
            List<MedicalRecordModel> records = recordDAO.getMedicalRecordsByPatient(currentPatientId);
            allRecords = FXCollections.observableArrayList(records);
            filteredRecords = FXCollections.observableArrayList(records);
            medicalFilesTable.setItems(filteredRecords);

            System.out.println("Loaded " + records.size() + " medical records for patient " + currentPatientId);
        } catch (Exception e) {
            System.err.println("Error loading medical records: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load medical records.");
        }
    }

    /**
     * Apply search and filter to the records
     */
    private void applyFilters() {
        if (allRecords == null) return;

        String searchText = searchField.getText().toLowerCase().trim();
        String filterValue = filterType.getValue();

        List<MedicalRecordModel> filtered = allRecords.stream()
                .filter(record -> {
                    // Search filter
                    if (!searchText.isEmpty()) {
                        String recordText = String.format("Medical Record #%d - %s %s",
                                        record.getRecordId(),
                                        record.getPatientName() != null ? record.getPatientName() : "",
                                        record.getCreatedAt() != null ? record.getCreatedAt().toString() : "")
                                .toLowerCase();
                        if (!recordText.contains(searchText)) {
                            return false;
                        }
                    }

                    // Category filter
                    if (filterValue != null && !filterValue.equals("All Categories")) {
                        if (filterValue.equals("Recent (Last 30 days)")) {
                            if (record.getCreatedAt() == null ||
                                    record.getCreatedAt().isBefore(java.time.LocalDateTime.now().minusDays(30))) {
                                return false;
                            }
                        } else if (filterValue.equals("Older")) {
                            if (record.getCreatedAt() != null &&
                                    record.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusDays(30))) {
                                return false;
                            }
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        filteredRecords.setAll(filtered);
    }

    /**
     * Refresh the medical records list
     */
    @FXML
    public void handleRefresh() {
        loadMedicalRecords();
        searchField.clear();
        filterType.setValue("All Categories");
        showAlert(Alert.AlertType.INFORMATION, "Refreshed", "Medical records list has been refreshed.");
    }

    /**
     * View the selected medical record - UPDATED to navigate to detailed view
     */
    @FXML
    public void handleViewFile() {
        MedicalRecordModel selectedRecord = medicalFilesTable.getSelectionModel().getSelectedItem();

        if (selectedRecord == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a medical record to view.");
            return;
        }

        // Navigate to the detailed table view
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/MedicalRecordDetailsView.fxml"));
            Scene scene = new Scene(loader.load(), 1366, 768);

            // Get the controller and pass the record data
            MedicalRecordDetailsController detailsController = loader.getController();
            detailsController.loadMedicalRecord(selectedRecord);

            Stage stage = (Stage) viewFileButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load medical record details view.");
        }
    }

    /**
     * Download the selected medical record as a text file
     */
    @FXML
    public void handleDownload() {
        MedicalRecordModel selectedRecord = medicalFilesTable.getSelectionModel().getSelectedItem();

        if (selectedRecord == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a medical record to download.");
            return;
        }

        // Open file chooser to save the file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Medical Record");
        String patientName = selectedRecord.getPatientName() != null ?
                selectedRecord.getPatientName().replaceAll("\\s+", "_") : "Patient";
        fileChooser.setInitialFileName(String.format("MedicalRecord_%d_%s.txt",
                selectedRecord.getRecordId(),
                patientName));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());

        if (file != null) {
            try {
                String content = generateRecordContentForFile(selectedRecord);
                FileWriter writer = new FileWriter(file);
                writer.write(content);
                writer.close();

                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Medical record downloaded successfully to:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving file: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save medical record.");
            }
        }
    }

    /**
     * Generate formatted content for file download
     */
    private String generateRecordContentForFile(MedicalRecordModel record) {
        StringBuilder content = new StringBuilder();

        // Header
        content.append("═══════════════════════════════════════════════════════════════════════════════\n");
        content.append("                              MEDICAL RECORD                                    \n");
        content.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

        // Patient Details
        content.append("═══════════════════════════════════════════════════════════════════════════════\n");
        content.append("                            PATIENT DETAILS                                     \n");
        content.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

        content.append(String.format("ID: %-20s Name: %-35s Age: %d\n",
                record.getRecordId(),
                record.getPatientName() != null ? record.getPatientName() : "N/A",
                record.getAge()));

        if (record.getCreatedAt() != null) {
            try {
                content.append("Date Created: ").append(record.getCreatedAt().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"))).append("\n");
            } catch (Exception e) {
                content.append("Date Created: ").append(record.getFormattedCreatedAt()).append("\n");
            }
        }

        content.append("\n───────────────────────────────────────────────────────────────────────────────\n\n");

        // Subjective
        if (record.getSubjective() != null && !record.getSubjective().trim().isEmpty()) {
            content.append("Subj:  ").append(record.getSubjective()).append("\n\n");
            content.append("───────────────────────────────────────────────────────────────────────────────\n");
        }

        // Chief Complaint
        if (record.getChiefComplaint() != null && !record.getChiefComplaint().trim().isEmpty()) {
            content.append("CC: ").append(record.getChiefComplaint()).append("\n\n");
            content.append("───────────────────────────────────────────────────────────────────────────────\n");
        }

        // History of Present Illness
        if (record.getHistoryPresentIllness() != null && !record.getHistoryPresentIllness().trim().isEmpty()) {
            content.append("HPI:\n").append(record.getHistoryPresentIllness()).append("\n\n");
            content.append("───────────────────────────────────────────────────────────────────────────────\n");
        }

        // OPQRST Assessment (table format)
        if (record.hasOPQRSTData()) {
            content.append("\n");

            // Row 1: Onset | Location | Duration | Character
            String onset = record.getOnset() != null && !record.getOnset().trim().isEmpty() ? record.getOnset() : "";
            String location = record.getLocation() != null && !record.getLocation().trim().isEmpty() ? record.getLocation() : "";
            String duration = record.getDuration() != null && !record.getDuration().trim().isEmpty() ? record.getDuration() : "";
            String character = record.getCharacteristics() != null && !record.getCharacteristics().trim().isEmpty() ? record.getCharacteristics() : "";

            if (!onset.isEmpty() || !location.isEmpty() || !duration.isEmpty() || !character.isEmpty()) {
                content.append(String.format("%-20s | %-25s | %-20s | %-20s\n",
                        !onset.isEmpty() ? "Onset: " + onset : "",
                        !location.isEmpty() ? "Location: " + location : "",
                        !duration.isEmpty() ? "Duration: " + duration : "",
                        !character.isEmpty() ? "Character: " + character : ""));
            }

            // Row 2: Aggravating Factors | Radiation | Severity
            String factors = record.getAggravatingFactors() != null && !record.getAggravatingFactors().trim().isEmpty() ? record.getAggravatingFactors() : "";
            String radiation = record.getRadiation() != null && !record.getRadiation().trim().isEmpty() ? record.getRadiation() : "";
            String severity = record.getSeverity() != null && !record.getSeverity().trim().isEmpty() ? record.getSeverity() : "";

            if (!factors.isEmpty() || !radiation.isEmpty() || !severity.isEmpty()) {
                content.append(String.format("%-50s | %-25s | %-20s\n",
                        !factors.isEmpty() ? "Aggravating Factors: " + factors : "",
                        !radiation.isEmpty() ? "Radiation: " + radiation : "",
                        !severity.isEmpty() ? "Severity: " + severity : ""));
            }

            content.append("\n───────────────────────────────────────────────────────────────────────────────\n");
        }

        // Past Medical History, Medications, Allergies
        String pmi = record.getPastMedicalIllness() != null && !record.getPastMedicalIllness().trim().isEmpty() ? record.getPastMedicalIllness() : "";
        String meds = record.getMedications() != null && !record.getMedications().trim().isEmpty() ? record.getMedications() : "";
        String allergies = record.getAllergies() != null && !record.getAllergies().trim().isEmpty() ? record.getAllergies() : "";

        if (!pmi.isEmpty() || !meds.isEmpty() || !allergies.isEmpty()) {
            content.append(String.format("\n%-30s | %-40s | %-20s\n",
                    !pmi.isEmpty() ? "PMI: " + pmi : "",
                    !meds.isEmpty() ? "Medication: " + meds : "",
                    !allergies.isEmpty() ? "Allergies: " + allergies : ""));
            content.append("───────────────────────────────────────────────────────────────────────────────\n");
        }

        // Family History, Social History, Review of Systems
        String fh = record.getFamilyHistory() != null && !record.getFamilyHistory().trim().isEmpty() ? record.getFamilyHistory() : "";
        String sh = record.getSocialHistory() != null && !record.getSocialHistory().trim().isEmpty() ? record.getSocialHistory() : "";
        String ros = record.getReviewOfSystems() != null && !record.getReviewOfSystems().trim().isEmpty() ? record.getReviewOfSystems() : "";

        if (!fh.isEmpty() || !sh.isEmpty() || !ros.isEmpty()) {
            content.append(String.format("\n%-30s | %-40s | %-20s\n",
                    !fh.isEmpty() ? "FH: " + fh : "",
                    !sh.isEmpty() ? "SH: " + sh : "",
                    !ros.isEmpty() ? "ROS: " + ros : ""));
        }

        content.append("\n═══════════════════════════════════════════════════════════════════════════════\n");
        content.append("                              END OF RECORD                                     \n");
        content.append("═══════════════════════════════════════════════════════════════════════════════\n");

        return content.toString();
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
                // Already on this page
                return;
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
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to " + targetFxml);
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
}