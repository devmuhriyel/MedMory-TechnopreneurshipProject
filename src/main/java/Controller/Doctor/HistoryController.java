package Controller.Doctor;

import CommonModel.MedicalCaseModel;
import CommonModel.MedicalRecordModel;
import CommonModel.PatientAttachmentModel;
import CommonModel.PatientModel;
import Controller.Doctor.DoctorMedicalRecordDetailsController;
import DAO.DatabaseConnection;
import DAO.RecordDAO;
import java.nio.file.Files;
import java.nio.file.Path;
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
import javafx.scene.control.Separator;
import javafx.scene.control.ScrollPane;
import java.time.format.DateTimeFormatter;
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
                // Read file into byte array
                byte[] fileData = Files.readAllBytes(file.toPath());

                // Determine file type (MIME type)
                String fileType = Files.probeContentType(file.toPath());
                if (fileType == null) {
                    // Fallback to generic binary type if MIME type cannot be determined
                    fileType = "application/octet-stream";
                }

                int doctorId = SessionManager.getCurrentUserId();
                boolean success = recordDAO.uploadPatientAttachment(
                        selectedCase.getPatientId(),
                        doctorId,
                        file.getName(),
                        fileType,
                        fileData
                );

                if (success) {
                    loadAttachmentsForCase(selectedCase.getPatientId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "File uploaded successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Upload Failed", "Failed to upload file.");
                }

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Upload Failed", "Failed to read file: " + e.getMessage());
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
            // Retrieve the full attachment with file data from database
            PatientAttachmentModel fullAttachment = recordDAO.getPatientAttachmentById(attachment.getAttachmentId());

            if (fullAttachment == null || fullAttachment.getFileData() == null) {
                showAlert(Alert.AlertType.WARNING, "File Not Found",
                        "The file data could not be retrieved from the database.");
                return;
            }

            // Create a temporary file to open
            String fileName = fullAttachment.getFileName();
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = fileName.substring(dotIndex);
            }

            // Create temp file with original extension
            Path tempFile = Files.createTempFile("attachment_", extension);
            Files.write(tempFile, fullAttachment.getFileData());

            // Mark for deletion on exit
            tempFile.toFile().deleteOnExit();

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile.toFile());
            } else {
                showAlert(Alert.AlertType.WARNING, "Not Supported",
                        "Desktop operations are not supported on this system.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Open Failed", "Failed to open file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to retrieve file: " + e.getMessage());
        }
    }

    // =================== CASE MANAGEMENT ===================

    /**
     * Add or update diagnosis for selected case
     */
    @FXML
    private void handleUpdateCase() {
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
    private void handleAddCase() {
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


    /**Create medical record with inline dialog
     */
    @FXML
    private void handleCreateMedicalRecordInline() {
        if (selectedCase == null) {
            showAlert(Alert.AlertType.WARNING, "No Case Selected", "Please select a case first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Medical Record");
        dialog.setHeaderText("Create medical record for: " + selectedCase.getPatientName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Create form fields
        TextField patientNameField = new TextField(selectedCase.getPatientName());
        patientNameField.setEditable(false);
        patientNameField.setDisable(true);

        TextField ageField = new TextField();
        ageField.setPromptText("Age");

        TextArea subjField = new TextArea();
        subjField.setPromptText("Subjective");
        subjField.setPrefRowCount(2);

        TextArea ccField = new TextArea();
        ccField.setPromptText("Chief Complaint");
        ccField.setPrefRowCount(2);

        TextArea hpiField = new TextArea();
        hpiField.setPromptText("History of Present Illness");
        hpiField.setPrefRowCount(3);

        // OPQRST Fields
        TextField onsetField = new TextField();
        onsetField.setPromptText("Onset");

        TextField locationField = new TextField();
        locationField.setPromptText("Location");

        TextField durationField = new TextField();
        durationField.setPromptText("Duration");

        TextField characterField = new TextField();
        characterField.setPromptText("Characteristics");

        TextField factorsField = new TextField();
        factorsField.setPromptText("Aggravating Factors");

        TextField radiationField = new TextField();
        radiationField.setPromptText("Radiation");

        TextField severityField = new TextField();
        severityField.setPromptText("Severity (e.g., 5/10)");

        // Medical History Fields
        TextArea pmiField = new TextArea();
        pmiField.setPromptText("Past Medical Illness");
        pmiField.setPrefRowCount(2);

        TextArea medicationsField = new TextArea();
        medicationsField.setPromptText("Current Medications");
        medicationsField.setPrefRowCount(2);

        TextArea allergiesField = new TextArea();
        allergiesField.setPromptText("Allergies");
        allergiesField.setPrefRowCount(1);

        TextArea fhField = new TextArea();
        fhField.setPromptText("Family History");
        fhField.setPrefRowCount(2);

        TextArea shField = new TextArea();
        shField.setPromptText("Social History");
        shField.setPrefRowCount(2);

        TextArea rosField = new TextArea();
        rosField.setPromptText("Review of Systems");
        rosField.setPrefRowCount(2);

        // Create scrollable content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);

        VBox content = new VBox(10,
                new Label("Patient Information:"),
                new Label("Patient Name:"), patientNameField,
                new Label("Age:"), ageField,
                new Separator(),

                new Label("Subjective:"),
                new Label("Subjective:"), subjField,
                new Label("Chief Complaint:"), ccField,
                new Label("History of Present Illness:"), hpiField,
                new Separator(),

                new Label("OPQRST Assessment:"),
                new Label("Onset:"), onsetField,
                new Label("Location:"), locationField,
                new Label("Duration:"), durationField,
                new Label("Characteristics:"), characterField,
                new Label("Aggravating Factors:"), factorsField,
                new Label("Radiation:"), radiationField,
                new Label("Severity:"), severityField,
                new Separator(),

                new Label("Medical History:"),
                new Label("Past Medical Illness:"), pmiField,
                new Label("Current Medications:"), medicationsField,
                new Label("Allergies:"), allergiesField,
                new Label("Family History:"), fhField,
                new Label("Social History:"), shField,
                new Label("Review of Systems:"), rosField
        );

        content.setStyle("-fx-padding: 10;");
        scrollPane.setContent(content);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(700);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    // Parse age
                    int age = 0;
                    try {
                        age = Integer.parseInt(ageField.getText().trim());
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.WARNING, "Validation", "Please enter a valid age.");
                        return;
                    }

                    // Validate required fields
                    if (subjField.getText().trim().isEmpty() && ccField.getText().trim().isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, "Validation", "Please enter at least a subjective or chief complaint.");
                        return;
                    }

                    // Create medical record model
                    MedicalRecordModel record = new MedicalRecordModel();
                    record.setPatientId(selectedCase.getPatientId());
                    record.setPatientName(selectedCase.getPatientName());
                    record.setAge(age);
                    record.setSubjective(subjField.getText().trim());
                    record.setChiefComplaint(ccField.getText().trim());
                    record.setHistoryPresentIllness(hpiField.getText().trim());
                    record.setOnset(onsetField.getText().trim());
                    record.setLocation(locationField.getText().trim());
                    record.setDuration(durationField.getText().trim());
                    record.setCharacteristics(characterField.getText().trim());
                    record.setAggravatingFactors(factorsField.getText().trim());
                    record.setRadiation(radiationField.getText().trim());
                    record.setSeverity(severityField.getText().trim());
                    record.setPastMedicalIllness(pmiField.getText().trim());
                    record.setMedications(medicationsField.getText().trim());
                    record.setAllergies(allergiesField.getText().trim());
                    record.setFamilyHistory(fhField.getText().trim());
                    record.setSocialHistory(shField.getText().trim());
                    record.setReviewOfSystems(rosField.getText().trim());
                    record.setDoctorId(SessionManager.getCurrentUserId());

                    // Save to database
                    boolean success = recordDAO.createMedicalRecordComplete(record);

                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Medical record created successfully.");

                        // Create audit log
                        recordDAO.createAuditLog(
                                SessionManager.getCurrentUserId(),
                                "CREATE_RECORD",
                                selectedCase.getPatientId(),
                                "medical_records",
                                0
                        );
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Creation Failed", "Failed to create medical record.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Error creating medical record: " + e.getMessage());
                }
            }
        });
    }

    /**
     * View medical records for the selected patient
     */
    @FXML
    private void handleViewMedicalRecords() {
        if (selectedCase == null) {
            showAlert(Alert.AlertType.WARNING, "No Case Selected", "Please select a case first.");
            return;
        }

        try {
            List<MedicalRecordModel> records = recordDAO.getMedicalRecordsByPatient(selectedCase.getPatientId());

            if (records.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Records",
                        "No medical records found for " + selectedCase.getPatientName());
                return;
            }

            // Create a dialog to display records
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Medical Records");
            dialog.setHeaderText("Medical Records for: " + selectedCase.getPatientName());
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Create table to display records
            TableView<MedicalRecordModel> recordsTable = new TableView<>();
            recordsTable.setPrefHeight(400);

            TableColumn<MedicalRecordModel, Integer> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("recordId"));
            idCol.setPrefWidth(50);

            TableColumn<MedicalRecordModel, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(cellData -> {
                MedicalRecordModel rec = cellData.getValue();
                if (rec.getCreatedAt() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            rec.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("N/A");
            });
            dateCol.setPrefWidth(120);

            TableColumn<MedicalRecordModel, String> subjCol = new TableColumn<>("Subjective");
            subjCol.setCellValueFactory(new PropertyValueFactory<>("subjective"));
            subjCol.setPrefWidth(300);

            TableColumn<MedicalRecordModel, String> ccCol = new TableColumn<>("Chief Complaint");
            ccCol.setCellValueFactory(new PropertyValueFactory<>("chiefComplaint"));
            ccCol.setPrefWidth(200);

            TableColumn<MedicalRecordModel, String> doctorCol = new TableColumn<>("Doctor");
            doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
            doctorCol.setPrefWidth(150);

            recordsTable.getColumns().addAll(idCol, dateCol, subjCol, ccCol, doctorCol);
            recordsTable.getItems().addAll(records);

            // Add double-click to view details
            recordsTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    MedicalRecordModel selectedRecord = recordsTable.getSelectionModel().getSelectedItem();
                    if (selectedRecord != null) {
                        openMedicalRecordDetails(selectedRecord);
                    }
                }
            });

            VBox content = new VBox(10,
                    new Label("Double-click a record to view full details"),
                    recordsTable
            );
            content.setStyle("-fx-padding: 10;");

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setPrefWidth(850);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load medical records: " + e.getMessage());
        }
    }

    /**
     * Show detailed view of a medical record
     */
    private void openMedicalRecordDetails(MedicalRecordModel record) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/MVP/DoctorMedicalRecordDetailsView.fxml")
            );

            Scene scene = new Scene(loader.load(), 1366, 768);

            // ✅ Get controller AFTER load
            DoctorMedicalRecordDetailsController controller = loader.getController();

            // ✅ Use YOUR method name
            controller.loadMedicalRecord(record);

            Stage stage = new Stage();
            stage.setTitle("Medical Record Details");
            stage.setScene(scene);


            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Failed to open medical record details."
            );
        }
    }

    /**
     * Add a new patient (admin function)
     */
    //This will be replaced with another form of adding existing patients
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
            return model.getFileName();
        }

        public String getFileType() {
            return model.getFileType();
        }

        public String getFileSize() {
            return model.getFormattedFileSize();
        }

        public long getFileSizeBytes() {
            return model.getFileSize();
        }

        public String getFileExtension() {
            return model.getFileExtension();
        }

        public String getUploadedAt() {
            return model.getUploadedAt();
        }

        public String getDoctorName() {
            return model.getDoctorName();
        }

        public PatientAttachmentModel getModel() {
            return model;
        }
    }
}