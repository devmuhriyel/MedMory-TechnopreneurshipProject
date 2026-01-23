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
        // --- Cases Table ---
        caseDateCol.setCellValueFactory(new PropertyValueFactory<>("caseDate"));
        treatmentCol.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        diagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        completionDateCol.setCellValueFactory(new PropertyValueFactory<>("dateCompleted"));
        casesTable.setItems(casesList);

        // --- Files Table ---
        fileNameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        filesTable.setItems(attachmentsList);

        // Double-click to open file
        filesTable.setRowFactory(tv -> {
            TableRow<Attachment> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openFileFromDB(row.getItem());
                }
            });
            return row;
        });

        // Load attachments whenever a case is selected
        casesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldCase, newCase) -> {
            if (newCase != null) {
                attachmentsList.setAll(newCase.getAttachments());
      }
        });

        // Search filter for cases
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCases(newVal));

    }

    public void setPatient(UserRow patient) {
        this.selectedPatient = patient;
        loadCasesForPatient(patient.getId());
        loadAttachmentsForPatient(patient.getId()); // only load files here
    }



    private void loadCasesForPatient(int patientID) {
        casesList.clear();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String sql = "SELECT c.case_id, c.patientID, u.name AS patientName, c.diagnosis, c.treatment, c.date_completed " +
                    "FROM cases c JOIN users u ON c.patientID = u.idusers " +
                    "WHERE c.patientID = ? ORDER BY c.date_completed DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, patientID);

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
        }
    }

    private void filterCases(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            casesTable.setItems(casesList);
            return;
        }

        ObservableList<DoctorCase> filtered = FXCollections.observableArrayList();
        for (DoctorCase dc : casesList) {
            if (dc.getDiagnosis().toLowerCase().contains(keyword.toLowerCase()) ||
                    dc.getTreatment().toLowerCase().contains(keyword.toLowerCase())) {
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
                attachmentsList.add(new Attachment(file)); // add to table immediately
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to upload file.");
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
            String sql = "DELETE FROM patient_attachments WHERE patient_id = ? AND file_name = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, selectedPatient.getId());
                ps.setString(2, selected.getFileName());
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    attachmentsList.remove(selected);
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Not Found", "File was not found in the database.");
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
            }
        } catch (IllegalArgumentException iae) {
            showAlert(Alert.AlertType.WARNING, "Date Format", "Please use YYYY-MM-DD for Date Completed.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add record: " + e.getMessage());
        }
    }



    private void saveFileToDB(File file) throws Exception {
        String URL = "jdbc:mysql://localhost:3306/medical";
        String USER = "root";
        String PASS = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String sql = "INSERT INTO patient_attachments (patient_id, file_name, file_data) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, selectedPatient.getId()); // assign to selected patient
            ps.setString(2, file.getName());
            ps.setBytes(3, java.nio.file.Files.readAllBytes(file.toPath()));

            ps.executeUpdate();
            System.out.println("File saved to DB for patient: " + selectedPatient.getName());
        }
    }

    private void loadAttachmentsForPatient(int patientID) {
        attachmentsList.clear();

        String URL = "jdbc:mysql://localhost:3306/medical";
        String USER = "root";
        String PASS = "";

        String sql = "SELECT file_name, file_data FROM patient_attachments WHERE patient_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Create a temporary File object for display
                String fileName = rs.getString("file_name");
                // Save the file to a temp folder so it can be opened later
                File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
                java.nio.file.Files.write(tempFile.toPath(), rs.getBytes("file_data"));

                attachmentsList.add(new Attachment(tempFile));
            }

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
            // Patient list screen isn't present in resources; return to Doctor Account for now
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/DoctorAccount.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) returnButton.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 768);
            stage.setScene(scene);
            stage.setTitle("Doctor Account");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
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
