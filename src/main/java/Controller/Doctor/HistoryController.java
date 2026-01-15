package Controller.Doctor;

import CommonModel.*;
import javafx.scene.Parent;
import DAO.DatabaseConnection;
import DAO.DoctorService;
import DAO.RecordDAO;
import MVP.Login;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HistoryController {

    @FXML private TableView<UserRow> userTable;
    @FXML
    private Button accountButton;
    @FXML
    private Button recordsButton;
    @FXML
    private Button calendarButton;
    @FXML
    private Button notificationsButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button createRecordButton;
    @FXML
    private TextField searchField;
    @FXML
    private VBox recordsListVBox;
    @FXML
    private VBox casesListVBox;
    @FXML
    private VBox filterOptionsVBox;

    private UserRow selectedPatient;
    private List<MedicalRecordSummary> allRecords;
    private List<DoctorCase> allCases;
    private RecordDAO recordDAO;
    private String currentFilter = null;

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
        recordDAO = new RecordDAO(DatabaseConnection.getConnection());
        int doctorID = SessionManager.getDoctorID();

        System.out.println("=== DEBUG: Doctor ID from SessionManager: " + doctorID);

        // Check if this is the Cases view (History.fxml) or Records view (ViewMedicalRecordList.fxml)
        if (casesListVBox != null) {
            // This is the Cases view - load cases
            loadCases(doctorID);

            // Setup search field listener for cases
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filterCases(newValue);
                });
            }
        } else if (recordsListVBox != null) {
            // This is the Records view - load records
            allRecords = RecordDAO.getDoctorMedicalRecords(doctorID);

            // Setup search field listener for records
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filterRecords(newValue);
                });
            }

            displayRecords(allRecords);
        }
    }

    private void loadCases(int doctorID) {
        allCases = DoctorService.getCasesHandledByDoctor(doctorID);
        displayCases(allCases);
    }

    private void displayCases(List<DoctorCase> cases) {
        if (casesListVBox == null) return;

        casesListVBox.getChildren().clear();

        if (cases.isEmpty()) {
            Label empty = new Label("No cases found.");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            casesListVBox.getChildren().add(empty);
            return;
        }

        for (DoctorCase doctorCase : cases) {
            addCaseItem(doctorCase);
        }
    }

    private void filterCases(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            displayCases(allCases);
        } else {
            List<DoctorCase> filtered = allCases.stream()
                    .filter(c ->
                            (c.patientName != null && c.patientName.toLowerCase().contains(searchTerm.toLowerCase())) ||
                                    (c.diagnosis != null && c.diagnosis.toLowerCase().contains(searchTerm.toLowerCase())) ||
                                    (c.treatment != null && c.treatment.toLowerCase().contains(searchTerm.toLowerCase())) ||
                                    (c.dateCompleted != null && c.dateCompleted.toLowerCase().contains(searchTerm.toLowerCase()))
                    )
                    .toList();
            displayCases(filtered);
        }
    }

    private void addCaseItem(DoctorCase doctorCase) {
        HBox row = new HBox(20);
        row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;"
                + " -fx-border-color: #cccccc; -fx-border-radius: 8;");
        row.setPrefHeight(60);

        // Format case date (Date column) - e.g., "February 13 2023"
        String formattedCaseDate = formatCaseDate(doctorCase.caseDate);
        Label dateLabel = new Label(formattedCaseDate);
        dateLabel.setPrefWidth(150);
        dateLabel.setStyle("-fx-font-size: 14px;");

        Label treatmentLabel = new Label(doctorCase.treatment != null ? doctorCase.treatment : "N/A");
        treatmentLabel.setPrefWidth(150);
        treatmentLabel.setStyle("-fx-font-size: 14px;");

        Label patientLabel = new Label(doctorCase.patientName != null ? doctorCase.patientName : "N/A");
        patientLabel.setPrefWidth(200);
        patientLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label diagnosisLabel = new Label(doctorCase.diagnosis != null ? doctorCase.diagnosis : "N/A");
        diagnosisLabel.setPrefWidth(250);
        diagnosisLabel.setStyle("-fx-font-size: 14px;");
        diagnosisLabel.setWrapText(true);

        // Format completion date - e.g., "14-2-2023"
        String completionDate = formatCompletionDate(doctorCase.dateCompleted);
        Label completionLabel = new Label(completionDate);
        completionLabel.setPrefWidth(150);
        completionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        row.getChildren().addAll(dateLabel, treatmentLabel, patientLabel, diagnosisLabel, completionLabel);
        casesListVBox.getChildren().add(row);
    }

    private String formatCaseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "N/A";
        }
        try {
            // Format: "February 13 2023" from "2023-02-13" or similar
            if (dateStr.contains("-") && dateStr.length() >= 10) {
                String[] parts = dateStr.substring(0, 10).split("-");
                if (parts.length == 3) {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);

                    String[] monthNames = {"", "January", "February", "March", "April", "May", "June",
                            "July", "August", "September", "October", "November", "December"};

                    if (month >= 1 && month <= 12) {
                        return monthNames[month] + " " + day + " " + year;
                    }
                }
            }
            return dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String formatCompletionDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "N/A";
        }
        try {
            // Format: "14-2-2023" from "2023-02-14" or similar
            if (dateStr.contains("-") && dateStr.length() >= 10) {
                String[] parts = dateStr.substring(0, 10).split("-");
                if (parts.length == 3) {
                    return parts[2] + "-" + Integer.parseInt(parts[1]) + "-" + parts[0];
                }
            }
            return dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void displayRecords(List<MedicalRecordSummary> records) {
        recordsListVBox.getChildren().clear();

        if (records.isEmpty()) {
            Label empty = new Label("No medical records found.");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            recordsListVBox.getChildren().add(empty);
            return;
        }

        for (MedicalRecordSummary summary : records) {
            addRecordItem(summary);
        }
    }

    private void filterRecords(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            displayRecords(allRecords);
        } else {
            int doctorID = SessionManager.getDoctorID();
            List<MedicalRecordSummary> filtered = RecordDAO.searchDoctorMedicalRecords(doctorID, searchTerm.trim());
            displayRecords(filtered);
        }
    }

    private void addRecordItem(MedicalRecordSummary summary) {
        HBox row = new HBox(15);
        row.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8;"
                + " -fx-border-color: #cccccc; -fx-border-radius: 8;");
        row.setPrefHeight(70);

        Label name = new Label(summary.patientName);
        name.setPrefWidth(200);
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label diagnosis = new Label(summary.diagnosis != null ? summary.diagnosis : "N/A");
        diagnosis.setPrefWidth(300);
        diagnosis.setWrapText(true);

        Label dateLabel = new Label(summary.recordDate != null ? summary.recordDate : "N/A");
        dateLabel.setPrefWidth(120);
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        // View Button
        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15 5 15; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> openRecord(summary.recordID));

        // Edit Button
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #00BF63; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15 5 15; -fx-cursor: hand;");
        editBtn.setOnAction(e -> editRecord(summary.recordID));

        // Delete Button
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e57373; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15 5 15; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteRecord(summary.recordID));

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(viewBtn, editBtn, deleteBtn);

        row.getChildren().addAll(name, diagnosis, dateLabel, buttonBox);
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

    /**
     * Handles the "Create New Record" button click.
     * Navigates to the DoctorMedicalRecord.fxml interface for creating a new record.
     */
    @FXML
    public void handleCreateRecord(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("DoctorMedicalRecord.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 768);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load DoctorMedicalRecord.fxml");
        }
    }

    /**
     * Opens record in edit mode
     */
    private void editRecord(int recordID) {
        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("DoctorMedicalRecord.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 768);

            RecordDAO dao = new RecordDAO(DatabaseConnection.getConnection());
            MedicalRecord record = dao.getRecordByID(recordID);

            if (record == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Record not found!");
                return;
            }

            DoctorMedicalRecordController controller = loader.getController();
            controller.loadRecordForEdit(record);

            Stage stage = (Stage) recordsListVBox.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load record for editing: " + e.getMessage());
        }
    }

    /**
     * Deletes a medical record with confirmation
     */
    private void deleteRecord(int recordID) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Record");
        confirmAlert.setHeaderText("Confirm Deletion");
        confirmAlert.setContentText("Are you sure you want to delete this medical record? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean deleted = recordDAO.deleteRecord(recordID);

                    if (deleted) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Medical record deleted successfully.");
                        // Refresh the list
                        int doctorID = SessionManager.getDoctorID();
                        allRecords = RecordDAO.getDoctorMedicalRecords(doctorID);
                        String searchTerm = searchField != null ? searchField.getText() : "";
                        if (searchTerm == null || searchTerm.trim().isEmpty()) {
                            displayRecords(allRecords);
                        } else {
                            filterRecords(searchTerm);
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete the medical record.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Database error: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles filter button clicks
     */
    @FXML
    public void handleFilterClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String filterText = clickedButton.getText();

        // Toggle filter selection
        if (currentFilter != null && currentFilter.equals(filterText)) {
            // Deselect if clicking the same filter
            currentFilter = null;
            clickedButton.setStyle("-fx-background-color: white; -fx-text-fill: #333; -fx-alignment: center-left; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-width: 1; -fx-cursor: hand;");
            // Reset to show all records
            if (recordsListVBox != null) {
                displayRecords(allRecords);
            }
        } else {
            // Select new filter
            currentFilter = filterText;

            // Reset all filter buttons
            if (filterOptionsVBox != null) {
                for (var child : filterOptionsVBox.getChildren()) {
                    if (child instanceof Button) {
                        Button btn = (Button) child;
                        btn.setStyle("-fx-background-color: white; -fx-text-fill: #333; -fx-alignment: center-left; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-width: 1; -fx-cursor: hand;");
                    }
                }
            }

            // Highlight selected filter
            clickedButton.setStyle("-fx-background-color: #b0e0ff; -fx-text-fill: #0050c0; -fx-alignment: center-left; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #2090ff; -fx-border-width: 2; -fx-cursor: hand;");

            // Apply filter to records
            if (recordsListVBox != null && allRecords != null) {
                filterRecordsByType(filterText);
            }
        }
    }

    private void filterRecordsByType(String filterType) {
        // Filter records based on diagnosis or other criteria matching the filter type
        List<MedicalRecordSummary> filtered = allRecords.stream()
                .filter(record -> {
                    String diagnosis = record.diagnosis != null ? record.diagnosis.toLowerCase() : "";
                    // Match filter keywords
                    if (filterType.contains("Constant mild")) {
                        return diagnosis.contains("constant") || diagnosis.contains("mild");
                    } else if (filterType.contains("Mild daily")) {
                        return diagnosis.contains("mild") || diagnosis.contains("daily");
                    } else if (filterType.contains("Daily episodes")) {
                        return diagnosis.contains("daily") || diagnosis.contains("episode");
                    } else if (filterType.contains("Continuous mild")) {
                        return diagnosis.contains("continuous") || diagnosis.contains("mild");
                    } else if (filterType.contains("Short intermitten")) {
                        return diagnosis.contains("short") || diagnosis.contains("intermittent");
                    }
                    return false;
                })
                .toList();
        displayRecords(filtered);
    }

    private void loadPatientHistory(int patientID) {
        if (userTable == null) return;

        userTable.getItems().clear();

        // Get all cases handled by this patient for this doctor
        int doctorID = SessionManager.getDoctorID();
        List<DoctorCase> cases = DoctorService.getCasesHandledByDoctor(doctorID);

        if (cases == null || cases.isEmpty()) return;

        for (DoctorCase c : cases) {
            if (c.patientID == patientID) {
                userTable.getItems().add(new UserRow(c.patientID, c.patientName));
            }
        }
    }

    public void setPatient(UserRow patient) {
        this.selectedPatient = patient;
        System.out.println("Selected patient: " + patient.getName() + " (ID: " + patient.getId() + ")");

        // Load patient history
        if (patient != null) {
            loadPatientHistory(patient.getId());
        }
    }


    @FXML
    private void handleReturn(ActionEvent event) {
        try {
            // Absolute path from classpath root
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Patient.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 768));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load Patients.fxml");
        }
    }

    }

