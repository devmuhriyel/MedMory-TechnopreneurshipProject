package Controller.Doctor;

import CommonModel.Attachment;
import CommonModel.DoctorCase;
import CommonModel.SessionManager;
import CommonModel.UserRow;
import MVP.Login;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class HistoryController {

    @FXML private TableView<DoctorCase> casesTable;
    @FXML private TableColumn<DoctorCase, String> caseDateCol;
    @FXML private TableColumn<DoctorCase, String> treatmentCol;
    @FXML private TableColumn<DoctorCase, String> patientCol;
    @FXML private TableColumn<DoctorCase, String> diagnosisCol;
    @FXML private TableColumn<DoctorCase, String> completionDateCol;

    @FXML private TableView<Attachment> filesTable;
    @FXML private TableColumn<Attachment, String> fileNameCol;

    @FXML private TextField searchField;
    @FXML private Button uploadButton;
    @FXML private Button removeFileButton;
    @FXML private Button returnButton;

    private UserRow selectedPatient;

    private final ObservableList<DoctorCase> casesList = FXCollections.observableArrayList();
    private final ObservableList<Attachment> attachmentsList = FXCollections.observableArrayList();
    // Map to track original file names for temp files
    private final Map<Attachment, String> attachmentOriginalNames = new HashMap<>();

    private final String URL = "jdbc:mysql://localhost:3306/medical";
    private final String USER = "root";
    private final String PASS = "";


    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();

        String targetFxml;

        switch (id) {
            case "accountButton":
                targetFxml = "DoctorAccount.fxml";
                break;
            case "casesButton":
                targetFxml = "History.fxml";
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

        caseDateCol.setCellValueFactory(new PropertyValueFactory<>("caseDate"));
        treatmentCol.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        diagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        completionDateCol.setCellValueFactory(new PropertyValueFactory<>("dateCompleted"));

        casesTable.setItems(casesList);

        fileNameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        filesTable.setItems(attachmentsList);

        // ✅ FIX: Clear files when clicking on a case row
        casesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                DoctorCase selectedCase = casesTable.getSelectionModel().getSelectedItem();
                if (selectedCase != null) {
                    // IMPORTANT: Clear previous patient's files first
                    attachmentsList.clear();
                    attachmentOriginalNames.clear();

                    // Create patient using ID (not name!)
                    UserRow patient = new UserRow(
                            selectedCase.getPatientID(),
                            selectedCase.getPatientName()
                    );
                    setPatient(patient);
                }
            }
        });

        int doctorId = SessionManager.getDoctorID();
        if (doctorId > 0) {
            loadAllCasesForDoctor(doctorId);
        }

        searchField.textProperty().addListener(
                (obs, oldVal, newVal) -> filterCases(newVal)
        );

        // ✅ FIX: Add double-click to open files
        filesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Attachment selected = filesTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openFileFromDB(selected);
                }
            }
        });
    }


    public void setPatient(UserRow patient) {
        this.selectedPatient = patient;

        // ✅ Clear everything first to prevent mixing data
        casesList.clear();
        attachmentsList.clear();
        attachmentOriginalNames.clear();

        // ✅ Load data ONLY for this specific patient ID
        loadCasesForPatient(patient.getId());
        loadAttachmentsForPatient(patient.getId());

        // Clear search field when patient is selected
        searchField.clear();

        // ✅ Debug: Print to verify correct patient
        System.out.println("Selected Patient - ID: " + patient.getId() + ", Name: " + patient.getName());
    }



    private void loadAllCasesForDoctor(int doctorId) {
        casesList.clear();

        String sql = """
        SELECT c.case_id, c.patientID, u.name AS patientName,
               c.diagnosis, c.treatment, c.date_completed
        FROM cases c
        JOIN users u ON c.patientID = u.idusers
        WHERE c.doctorID = ?
        ORDER BY c.date_completed DESC
    """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DoctorCase dc = new DoctorCase();
                dc.setCaseID(rs.getInt("case_id"));
                dc.setPatientID(rs.getInt("patientID"));
                dc.setPatientName(rs.getString("patientName"));
                dc.setCaseDate(rs.getString("date_completed"));
                dc.setDiagnosis(rs.getString("diagnosis"));
                dc.setTreatment(rs.getString("treatment"));
                casesList.add(dc);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    private void loadCasesForPatient(int patientID) {
        casesList.clear();
        int doctorId = SessionManager.getDoctorID();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String sql = "SELECT c.case_id, c.patientID, u.name AS patientName, c.diagnosis, c.treatment, c.date_completed " +
                    "FROM cases c JOIN users u ON c.patientID = u.idusers " +
                    "WHERE c.patientID = ? AND c.doctorID = ? ORDER BY c.date_completed DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientID);
            stmt.setInt(2, doctorId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DoctorCase dc = new DoctorCase();
                dc.setCaseID(rs.getInt("case_id"));
                dc.setPatientID(rs.getInt("patientID"));
                dc.setPatientName(rs.getString("patientName"));
                dc.setCaseDate(rs.getString("date_completed"));
                dc.setDiagnosis(rs.getString("diagnosis"));
                dc.setTreatment(rs.getString("treatment"));
                casesList.add(dc);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load cases: " + e.getMessage());
        }
    }

    private void filterCases(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            casesTable.setItems(casesList);
            return;
        }

        ObservableList<DoctorCase> filtered = FXCollections.observableArrayList();
        String lowerKeyword = keyword.toLowerCase();
        for (DoctorCase dc : casesList) {
            String diagnosis = dc.getDiagnosis() != null ? dc.getDiagnosis().toLowerCase() : "";
            String treatment = dc.getTreatment() != null ? dc.getTreatment().toLowerCase() : "";
            if (diagnosis.contains(lowerKeyword) || treatment.contains(lowerKeyword)) {
                filtered.add(dc);
            }
        }
        casesTable.setItems(filtered);
    }

    @FXML
    private void handleUploadFile() {
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "No Patient Selected", "Please add/select a patient first.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Upload File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF & Images", "*.pdf", "*.png", "*.jpg", "*.jpeg")
        );

        File file = chooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (file != null) {
            try {
                saveFileToDB(file); // save file to database
                loadAttachmentsForPatient(selectedPatient.getId()); // reload attachments from DB
                showAlert(Alert.AlertType.INFORMATION, "Success", "File uploaded successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Upload Failed", "Failed to upload file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRemoveFile() {
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "No Patient Selected", "Please add/select a patient first.");
            return;
        }

        Attachment selected = filesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No File Selected", "Please select a file to remove.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Remove");
        confirm.setHeaderText("Remove selected file?");
        confirm.setContentText(selected.getFileName());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            // Get the original file name from the map
            String originalFileName = attachmentOriginalNames.get(selected);
            if (originalFileName == null) {
                // Fallback: try to extract from temp file name
                String tempFileName = selected.getFileName();
                if (tempFileName.startsWith("patient_")) {
                    String[] parts = tempFileName.split("_", 4);
                    if (parts.length >= 4) {
                        originalFileName = parts[3];
                    }
                } else {
                    originalFileName = tempFileName;
                }
            }
            
            // Use patient_id AND file_name to ensure we delete the correct file for this specific patient
            String sql = "DELETE FROM patient_attachments WHERE patient_id = ? AND file_name = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int patientId = selectedPatient.getId(); // Use patient ID, not name - ensures correct patient
                ps.setInt(1, patientId);
                ps.setString(2, originalFileName);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    loadAttachmentsForPatient(patientId); // reload from DB using patient ID
                    showAlert(Alert.AlertType.INFORMATION, "Success", "File removed successfully.");
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Not Found", "File was not found in the database for this patient.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to remove file: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddPatient() {
        Dialog<UserRow> dialog = new Dialog<>();
        dialog.setTitle("Add Patient");
        dialog.setHeaderText("Create a patient account");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Patient name");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Temporary password");

        VBox content = new VBox(10, new Label("Name"), nameField, new Label("Password"), passField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            String pass = passField.getText() == null ? "" : passField.getText().trim();
            if (name.isEmpty() || pass.length() < 6) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Name is required and password must be at least 6 characters.");
                return null;
            }

            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                String sql = "INSERT INTO users (name, password) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, pass);
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        ResultSet keys = ps.getGeneratedKeys();
                        if (keys.next()) {
                            int id = keys.getInt(1);
                            return new UserRow(id, name);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create patient: " + e.getMessage());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(patient -> {
            this.selectedPatient = patient;
            loadCasesForPatient(patient.getId());
            loadAttachmentsForPatient(patient.getId());
            showAlert(Alert.AlertType.INFORMATION, "Patient Added", "Patient created and selected: " + patient.getName());
        });
    }

    @FXML
    private void handleAddRecord() {
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "No Patient Selected", "Please add/select a patient first.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Record");
        dialog.setHeaderText("Add a case record for: " + selectedPatient.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField diagnosisField = new TextField();
        diagnosisField.setPromptText("Diagnosis");
        TextField treatmentField = new TextField();
        treatmentField.setPromptText("Treatment");
        TextField dateCompletedField = new TextField();
        dateCompletedField.setPromptText("Date Completed (YYYY-MM-DD) - optional");

        VBox content = new VBox(10,
                new Label("Diagnosis"), diagnosisField,
                new Label("Treatment"), treatmentField,
                new Label("Date Completed"), dateCompletedField
        );
        dialog.getDialogPane().setContent(content);

        ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
        if (result != ButtonType.OK) return;

        String diagnosis = diagnosisField.getText() == null ? "" : diagnosisField.getText().trim();
        String treatment = treatmentField.getText() == null ? "" : treatmentField.getText().trim();
        String dateCompleted = dateCompletedField.getText() == null ? "" : dateCompletedField.getText().trim();

        if (diagnosis.isEmpty() || treatment.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Diagnosis and Treatment are required.");
            return;
        }

        int doctorId = SessionManager.getDoctorID();
        if (doctorId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Session Error", "No doctor session found. Please log in again.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String sql = "INSERT INTO cases (patientID, doctorID, diagnosis, treatment, date_completed, case_date) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, selectedPatient.getId());
                ps.setInt(2, doctorId);
                ps.setString(3, diagnosis);
                ps.setString(4, treatment);
                if (dateCompleted.isEmpty()) {
                    ps.setNull(5, Types.DATE);
                    ps.setNull(6, Types.DATE);
                } else {
                    ps.setDate(5, Date.valueOf(dateCompleted));
                    ps.setDate(6, Date.valueOf(dateCompleted));
                }

                ps.executeUpdate();
                loadCasesForPatient(selectedPatient.getId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Record added successfully.");
            }
        } catch (IllegalArgumentException iae) {
            showAlert(Alert.AlertType.WARNING, "Date Format", "Please use YYYY-MM-DD for Date Completed.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add record: " + e.getMessage());
        }
    }



    private void saveFileToDB(File file) throws Exception {
        if (selectedPatient == null) {
            throw new IllegalStateException("No patient selected. Cannot upload file without a selected patient.");
        }

        String URL = "jdbc:mysql://localhost:3306/medical";
        String USER = "root";
        String PASS = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            // ✅ Use patient_id to link file to specific patient
            String sql = "INSERT INTO patient_attachments (patient_id, file_name, file_data) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            int patientId = selectedPatient.getId();
            ps.setInt(1, patientId);
            ps.setString(2, file.getName());
            ps.setBytes(3, java.nio.file.Files.readAllBytes(file.toPath()));

            ps.executeUpdate();

            // ✅ Debug: Confirm file was saved
            System.out.println("File '" + file.getName() + "' saved for patient ID: " + patientId +
                    " (Name: " + selectedPatient.getName() + ")");
        }
    }

    private void loadAttachmentsForPatient(int patientID) {
        attachmentsList.clear();
        attachmentOriginalNames.clear();

        String URL = "jdbc:mysql://localhost:3306/medical";
        String USER = "root";
        String PASS = "";

        // ✅ Use patient_id to ensure we only get files for THIS specific patient
        String sql = "SELECT file_name, file_data FROM patient_attachments WHERE patient_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientID);
            ResultSet rs = ps.executeQuery();

            // ✅ Debug: Check how many files are loaded
            int fileCount = 0;

            while (rs.next()) {
                String originalFileName = rs.getString("file_name");
                String uniqueTempFileName = "patient_" + patientID + "_" + System.currentTimeMillis() + "_" + originalFileName;
                File tempFile = new File(System.getProperty("java.io.tmpdir"), uniqueTempFileName);
                java.nio.file.Files.write(tempFile.toPath(), rs.getBytes("file_data"));

                Attachment attachment = new Attachment(tempFile);
                attachmentsList.add(attachment);
                attachmentOriginalNames.put(attachment, originalFileName);
                fileCount++;
            }

            // ✅ Debug: Print loaded files
            System.out.println("Loaded " + fileCount + " file(s) for patient ID: " + patientID);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load attachments for patient ID: " + patientID);
        }
    }

    private void openFileFromDB(Attachment attachment) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(attachment.getFile());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleReturn() {
        try {
            // ✅ FIX: Go back to DoctorAccount (patient list), not settings
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("History.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 768);

            Stage stage = (Stage) returnButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Doctor Account");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Failed to return to patient list: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
