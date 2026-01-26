package Controller.Doctor;

import CommonModel.Attachment;
import CommonModel.DoctorCase;
import CommonModel.SessionManager;
import MVP.Login;
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

    private DoctorCase selectedCase; // currently selected case
    private final ObservableList<DoctorCase> casesList = FXCollections.observableArrayList();
    private final ObservableList<Attachment> attachmentsList = FXCollections.observableArrayList();
    private final Map<Attachment, String> attachmentOriginalNames = new HashMap<>();

    private final String URL = "jdbc:mysql://localhost:3306/medical";
    private final String USER = "root";
    private final String PASS = "";

    @FXML
    public void initialize() {
        // Setup table columns
        caseDateCol.setCellValueFactory(new PropertyValueFactory<>("caseDate"));
        treatmentCol.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        diagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        completionDateCol.setCellValueFactory(new PropertyValueFactory<>("dateCompleted"));

        casesTable.setItems(casesList);

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
                    attachmentsList.clear();
                    attachmentOriginalNames.clear();
                    loadAttachmentsForCase(selectedCase.getCaseID());

                    // Enable buttons now that a case is selected
                    uploadButton.setDisable(false);
                    removeFileButton.setDisable(false);
                }
            }
        });

        // Double-click to open file
        filesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Attachment selected = filesTable.getSelectionModel().getSelectedItem();
                if (selected != null) openFileFromDB(selected);
            }
        });

        // Load cases for doctor
        int doctorId = SessionManager.getDoctorID();
        if (doctorId > 0) loadAllCasesForDoctor(doctorId);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCases(newVal));
    }

    // =================== CASE LOADING ===================

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
                String dateCompleted = rs.getString("date_completed");
                dc.setCaseDate(dateCompleted);
                dc.setDateCompleted(dateCompleted);
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
            String patientName = dc.getPatientName() != null ? dc.getPatientName().toLowerCase() : "";
            String caseDate = dc.getCaseDate() != null ? dc.getCaseDate().toLowerCase() : "";
            String dateCompleted = dc.getDateCompleted() != null ? dc.getDateCompleted().toLowerCase() : "";
            
            // Search across all relevant fields
            if (diagnosis.contains(lowerKeyword) || 
                treatment.contains(lowerKeyword) || 
                patientName.contains(lowerKeyword) ||
                caseDate.contains(lowerKeyword) ||
                dateCompleted.contains(lowerKeyword)) {
                filtered.add(dc);
            }
        }
        casesTable.setItems(filtered);
    }

    // =================== FILE HANDLING ===================

    @FXML
    private void handleUploadFile() {
        if (selectedCase == null) {
            showAlert(Alert.AlertType.WARNING, "No Case Selected", "Please select a case first.");
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
                saveFileToDB(file);
                loadAttachmentsForCase(selectedCase.getCaseID());
                showAlert(Alert.AlertType.INFORMATION, "Success", "File uploaded for this case.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Upload Failed", "Failed to upload file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRemoveFile() {
        if (selectedCase == null) {
            showAlert(Alert.AlertType.WARNING, "No Case Selected", "Please select a case first.");
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

            // Get the original file name
            String originalFileName = attachmentOriginalNames.get(selected);
            if (originalFileName == null) {
                originalFileName = selected.getFile().getName(); // fallback
            }

            // Delete from database using CASE ID
            String sql = "DELETE FROM patient_attachments WHERE case_id = ? AND file_name = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, selectedCase.getCaseID());
                ps.setString(2, originalFileName);
                int rows = ps.executeUpdate();

                if (rows > 0) {
                    // Remove from UI list
                    attachmentsList.remove(selected);
                    attachmentOriginalNames.remove(selected);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "File removed successfully from database.");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Not Found", "File not found in database.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to remove file: " + e.getMessage());
        }
    }

    private void saveFileToDB(File file) throws Exception {
        if (selectedCase == null) throw new IllegalStateException("No case selected.");

        String sql = "INSERT INTO patient_attachments (patient_id, case_id, file_name, file_data) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selectedCase.getPatientID());
            ps.setInt(2, selectedCase.getCaseID());
            ps.setString(3, file.getName());
            ps.setBytes(4, java.nio.file.Files.readAllBytes(file.toPath()));

            ps.executeUpdate();
        }
    }

    private void loadAttachmentsForCase(int caseID) {
        attachmentsList.clear();
        attachmentOriginalNames.clear();

        String sql = "SELECT file_name, file_data FROM patient_attachments WHERE case_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, caseID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String originalFileName = rs.getString("file_name");
                File tempFile = new File(
                        System.getProperty("java.io.tmpdir"),
                        "case_" + caseID + "_" + System.currentTimeMillis() + "_" + originalFileName
                );
                java.nio.file.Files.write(tempFile.toPath(), rs.getBytes("file_data"));

                Attachment attachment = new Attachment(tempFile);
                attachmentsList.add(attachment);
                attachmentOriginalNames.put(attachment, originalFileName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openFileFromDB(Attachment attachment) {
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(attachment.getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource(targetFxml));
            Scene scene = new Scene(loader.load(), 1280, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load: " + targetFxml);
        }
    }
    @FXML
    private void handleAddRecord() {
        if (selectedCase == null) {
            showAlert(Alert.AlertType.WARNING, "No Case Selected", "Please select a case first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Record");
        dialog.setHeaderText("Add record for case: " + selectedCase.getCaseID());
        dialog.setContentText("Enter diagnosis:");

        dialog.showAndWait().ifPresent(diagnosis -> {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                String sql = "UPDATE cases SET diagnosis = ? WHERE case_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, diagnosis);
                ps.setInt(2, selectedCase.getCaseID());
                ps.executeUpdate();

                loadAllCasesForDoctor(SessionManager.getDoctorID());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Record added.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
            }
        });
    }

    @FXML
    private void handleAddPatient() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Patient");
        dialog.setHeaderText("Create new patient");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Temporary password");

        VBox content = new VBox(10, new Label("Name"), nameField, new Label("Password"), passField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> btn);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String name = nameField.getText().trim();
                String pass = passField.getText().trim();
                if (name.isEmpty() || pass.length() < 6) {
                    showAlert(Alert.AlertType.WARNING, "Validation", "Name required and password >= 6 chars");
                    return;
                }
                try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                    String sql = "INSERT INTO users (name, password) VALUES (?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, name);
                    ps.setString(2, pass);
                    ps.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Patient created.");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
                }
            }
        });
    }


    @FXML
    private void handleReturn() {
        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("DoctorAccount.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 768);
            Stage stage = (Stage) returnButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load Doctor Account page.");
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
