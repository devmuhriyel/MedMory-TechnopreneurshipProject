package Controller.Doctor;

import CommonModel.MedicalCaseModel;
import CommonModel.PatientAttachmentModel;
import CommonModel.PatientModel;
import DAO.DatabaseConnection;
import DAO.RecordDAO;
import MVP.Login;
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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Doctor Case History and File Management
 * Handles viewing medical cases and managing patient attachments
 */
public class HistoryController {

    // Cases Table
    @FXML private TableView<MedicalCaseModel> casesTable;
    @FXML private TableColumn<MedicalCaseModel, String> caseDateCol;
    @FXML private TableColumn<MedicalCaseModel, String> treatmentCol;
    @FXML private TableColumn<MedicalCaseModel, String> patientCol;
    @FXML private TableColumn<MedicalCaseModel, String> diagnosisCol;
    @FXML private TableColumn<MedicalCaseModel, String> statusCol;

    // Attachments Table
    @FXML private TableView<AttachmentDisplay> filesTable;
    @FXML private TableColumn<AttachmentDisplay, String> fileNameCol;

    // UI Controls
    @FXML private TextField searchField;
    @FXML private Button uploadButton;
    @FXML private Button removeFileButton;
    @FXML private Button returnButton;

    private MedicalCaseModel selectedCase;
    private final ObservableList<MedicalCaseModel> casesList = FXCollections.observableArrayList();
    private final ObservableList<AttachmentDisplay> attachmentsList = FXCollections.observableArrayList();

    private RecordDAO recordDAO;

    @FXML
    public void initialize() {
        System.out.println("History Controller Initialized");

        // Initialize DAO
        Connection conn = DatabaseConnection.getConnection();
        recordDAO = new RecordDAO(conn);

        // Setup cases table columns
        caseDateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        treatmentCol.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        diagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        casesTable.setItems(casesList);

        // Setup files table columns
        fileNameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        filesTable.setItems(attachmentsList);

        // Initially disable upload/remove until a case is selected
        uploadButton.setDisable(true);
        removeFileButton.setDisable(true);

        // Select a case → load files for that case only
        casesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                selectedCase = casesTable.getSelectionModel().getSelectedItem();
                if (selectedCase != null) {
                    loadAttachmentsForCase(selectedCase.getPatientId());
                    uploadButton.setDisable(false);
                    removeFileButton.setDisable(false);
                }
            }
        });

        // Double-click to open file
        filesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                AttachmentDisplay selected = filesTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openFile(selected);
                }
            }
        });

        // Load cases for doctor
        int doctorId = SessionManager.getCurrentUserId();
        if (doctorId > 0) {
            loadAllCasesForDoctor(doctorId);
        } else {
            showAlert(Alert.AlertType.WARNING, "Session Error", "No active doctor session found.");
        }

        // Setup search filter
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCases(newVal));
        }
    }

    // =================== CASE LOADING ===================

    /**
     * Load all medical cases for the current doctor
     */
    private void loadAllCasesForDoctor(int doctorId) {
        casesList.clear();

        try {
            List<MedicalCaseModel> cases = recordDAO.getMedicalCasesByDoctor(doctorId);
            casesList.addAll(cases);

            if (cases.isEmpty()) {
                System.out.println("No cases found for doctor ID: " + doctorId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load cases: " + e.getMessage());
        }
    }

    /**
     * Filter cases based on search keyword
     */
    private void filterCases(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            casesTable.setItems(casesList);
            return;
        }

        ObservableList<MedicalCaseModel> filtered = FXCollections.observableArrayList();
        String lowerKeyword = keyword.toLowerCase().trim();

        for (MedicalCaseModel caseModel : casesList) {
            String diagnosis = caseModel.getDiagnosis() != null ? caseModel.getDiagnosis().toLowerCase() : "";
            String treatment = caseModel.getTreatment() != null ? caseModel.getTreatment().toLowerCase() : "";
            String patientName = caseModel.getPatientName() != null ? caseModel.getPatientName().toLowerCase() : "";
            String status = caseModel.getStatus() != null ? caseModel.getStatus().toLowerCase() : "";
            String createdAt = caseModel.getCreatedAt() != null ? caseModel.getCreatedAt().toLowerCase() : "";

            if (diagnosis.contains(lowerKeyword) ||
                    treatment.contains(lowerKeyword) ||
                    patientName.contains(lowerKeyword) ||
                    status.contains(lowerKeyword) ||
                    createdAt.contains(lowerKeyword)) {
                filtered.add(caseModel);
            }
        }

        casesTable.setItems(filtered);
    }

    // =================== FILE HANDLING ===================

    /**
     * Handle file upload for selected case
     */
    @FXML
    private void handleUploadFile() {
        if (selectedCase == null) {
            showAlert(Alert.AlertType.WARNING, "No Case Selected", "Please select a case first.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Upload File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Document Files", "*.doc", "*.docx")
        );

        File file = chooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (file != null) {
            try {
                // Save file path to database
                // Note: In production, you might want to copy files to a secure location
                int doctorId = SessionManager.getCurrentUserId();
                boolean success = recordDAO.uploadPatientAttachment(
                        selectedCase.getPatientId(),
                        doctorId,
                        file.getAbsolutePath()
                );

                if (success) {
                    loadAttachmentsForCase(selectedCase.getPatientId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "File uploaded successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Upload Failed", "Failed to upload file.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Upload Failed", "Failed to upload file: " + e.getMessage());
            }
        }
    }

    /**
     * Handle file removal
     */
    @FXML
    private void handleRemoveFile() {
        if (selectedCase == null) {
            showAlert(Alert.AlertType.WARNING, "No Case Selected", "Please select a case first.");
            return;
        }

        AttachmentDisplay selected = filesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No File Selected", "Please select a file to remove.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Remove");
        confirm.setHeaderText("Remove selected file?");
        confirm.setContentText(selected.getFileName());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            boolean success = recordDAO.deletePatientAttachment(selected.getAttachmentId());

            if (success) {
                attachmentsList.remove(selected);
                showAlert(Alert.AlertType.INFORMATION, "Success", "File removed successfully.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Not Found", "File not found in database.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to remove file: " + e.getMessage());
        }
    }

    /**
     * Load attachments for a specific patient
     */
    private void loadAttachmentsForCase(int patientId) {
        attachmentsList.clear();

        try {
            List<PatientAttachmentModel> attachments = recordDAO.getPatientAttachments(patientId);

            for (PatientAttachmentModel attachment : attachments) {
                attachmentsList.add(new AttachmentDisplay(attachment));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load attachments: " + e.getMessage());
        }
    }

    /**
     * Open a file from the database
     */
    private void openFile(AttachmentDisplay attachment) {
        try {
            File file = new File(attachment.getFilePath());

            if (!file.exists()) {
                showAlert(Alert.AlertType.WARNING, "File Not Found",
                        "The file no longer exists at the stored location:\n" + attachment.getFilePath());
                return;
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert(Alert.AlertType.WARNING, "Not Supported",
                        "Desktop operations are not supported on this system.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Open Failed", "Failed to open file: " + e.getMessage());
        }
    }

    // =================== CASE MANAGEMENT ===================

    /**
     * Add or update diagnosis for selected case
     */
    @FXML
    private void handleAddRecord() {
        if (selectedCase == null) {
            showAlert(Alert.AlertType.WARNING, "No Case Selected", "Please select a case first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Case");
        dialog.setHeaderText("Update case for: " + selectedCase.getPatientName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextArea diagnosisArea = new TextArea(selectedCase.getDiagnosis());
        diagnosisArea.setPromptText("Diagnosis");
        diagnosisArea.setPrefRowCount(3);

        TextArea treatmentArea = new TextArea(selectedCase.getTreatment());
        treatmentArea.setPromptText("Treatment");
        treatmentArea.setPrefRowCount(3);

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("OPEN", "CLOSED");
        statusCombo.setValue(selectedCase.getStatus());

        VBox content = new VBox(10,
                new Label("Diagnosis:"), diagnosisArea,
                new Label("Treatment:"), treatmentArea,
                new Label("Status:"), statusCombo
        );
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String diagnosis = diagnosisArea.getText().trim();
                String treatment = treatmentArea.getText().trim();
                String status = statusCombo.getValue();

                try {
                    boolean success = recordDAO.updateMedicalCase(
                            selectedCase.getCaseId(),
                            diagnosis,
                            treatment,
                            status
                    );

                    if (success) {
                        loadAllCasesForDoctor(SessionManager.getCurrentUserId());
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Case updated successfully.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update case.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
                }
            }
        });
    }

    /**
     * Create a new case
     */
    @FXML
    private void handleCreateCase() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Case");
        dialog.setHeaderText("Create a new medical case");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Get list of patients for selection
        ComboBox<PatientModel> patientCombo = new ComboBox<>();
        try {
            List<PatientModel> patients = recordDAO.getAllPatients();
            patientCombo.getItems().addAll(patients);
            patientCombo.setConverter(new javafx.util.StringConverter<PatientModel>() {
                @Override
                public String toString(PatientModel patient) {
                    return patient != null ? patient.getName() : "";
                }
                @Override
                public PatientModel fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load patients: " + e.getMessage());
            return;
        }

        TextArea diagnosisArea = new TextArea();
        diagnosisArea.setPromptText("Diagnosis");
        diagnosisArea.setPrefRowCount(3);

        TextArea treatmentArea = new TextArea();
        treatmentArea.setPromptText("Treatment");
        treatmentArea.setPrefRowCount(3);

        VBox content = new VBox(10,
                new Label("Patient:"), patientCombo,
                new Label("Diagnosis:"), diagnosisArea,
                new Label("Treatment:"), treatmentArea
        );
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                PatientModel patient = patientCombo.getValue();
                String diagnosis = diagnosisArea.getText().trim();
                String treatment = treatmentArea.getText().trim();

                if (patient == null) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Please select a patient.");
                    return;
                }

                if (diagnosis.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Diagnosis is required.");
                    return;
                }

                try {
                    int doctorId = SessionManager.getCurrentUserId();
                    boolean success = recordDAO.createMedicalCase(
                            patient.getPatientId(),
                            doctorId,
                            diagnosis,
                            treatment,
                            "OPEN"
                    );

                    if (success) {
                        loadAllCasesForDoctor(doctorId);
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Case created successfully.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Creation Failed", "Failed to create case.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
                }
            }
        });
    }

    /**
     * Add a new patient (admin function)
     */
    @FXML
    private void handleAddPatient() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Patient");
        dialog.setHeaderText("Create new patient");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        DatePicker birthdayPicker = new DatePicker();
        birthdayPicker.setPromptText("Birthday");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Temporary password (min 6 chars)");

        VBox content = new VBox(10,
                new Label("Name:"), nameField,
                new Label("Email:"), emailField,
                new Label("Phone:"), phoneField,
                new Label("Birthday:"), birthdayPicker,
                new Label("Password:"), passField
        );
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String birthday = birthdayPicker.getValue() != null ? birthdayPicker.getValue().toString() : "";
                String pass = passField.getText().trim();

                if (name.isEmpty() || email.isEmpty() || pass.length() < 6) {
                    showAlert(Alert.AlertType.WARNING, "Validation",
                            "Name, email required and password must be at least 6 characters.");
                    return;
                }

                try {
                    boolean success = recordDAO.createPatient(name, email, pass, phone, birthday);

                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Patient created successfully.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Creation Failed", "Failed to create patient.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
                }
            }
        });
    }

    // =================== NAVIGATION ===================

    @FXML
    private void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();
        String targetFxml;

        switch (id) {
            case "accountButton" -> targetFxml = "DoctorAccount.fxml";
            case "casesButton" -> targetFxml = "History.fxml";
            case "calendarButton" -> targetFxml = "Calendar.fxml";
            case "notificationsButton" -> targetFxml = "Chat.fxml";
            case "settingsButton" -> targetFxml = "DoctorSettings.fxml";
            default -> {
                System.out.println("Unknown navigation button: " + id);
                return;
            }
        }

        navigateToScene(targetFxml, event);
    }

    @FXML
    private void handleReturn() {
        navigateToScene("DoctorAccount.fxml", null);
    }

    /**
     * Navigate to a different scene
     */
    private void navigateToScene(String fxmlFile, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource(fxmlFile));
            Scene scene = new Scene(loader.load(), 1280, 768);

            Stage stage;
            if (event != null) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else if (returnButton != null) {
                stage = (Stage) returnButton.getScene().getWindow();
            } else {
                stage = (Stage) casesTable.getScene().getWindow();
            }

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load: " + fxmlFile);
        }
    }

    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // =================== INNER CLASSES ===================

    /**
     * Display wrapper for PatientAttachmentModel
     */
    public static class AttachmentDisplay {
        private final PatientAttachmentModel model;

        public AttachmentDisplay(PatientAttachmentModel model) {
            this.model = model;
        }

        public int getAttachmentId() {
            return model.getAttachmentId();
        }

        public String getFileName() {
            // Extract filename from path if needed
            String path = model.getFilePath();
            if (path != null && path.contains(File.separator)) {
                return path.substring(path.lastIndexOf(File.separator) + 1);
            }
            return path;
        }

        public String getFilePath() {
            return model.getFilePath();
        }

        public String getUploadedAt() {
            return model.getUploadedAt();
        }

        public String getDoctorName() {
            return model.getDoctorName();
        }
    }
}