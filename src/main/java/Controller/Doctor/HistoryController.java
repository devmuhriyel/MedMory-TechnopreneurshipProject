package Controller.Doctor;

import CommonModel.Attachment;
import CommonModel.DoctorCase;
import CommonModel.UserRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
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
    @FXML private Button returnButton;

    private UserRow selectedPatient;

    private final ObservableList<DoctorCase> casesList = FXCollections.observableArrayList();
    private final ObservableList<Attachment> attachmentsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // --- Cases table setup ---
        caseDateCol.setCellValueFactory(new PropertyValueFactory<>("caseDate"));
        treatmentCol.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        diagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        completionDateCol.setCellValueFactory(new PropertyValueFactory<>("dateCompleted"));

        casesTable.setItems(casesList);

        // --- Files table setup ---
        fileNameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        filesTable.setItems(attachmentsList);

        // Open file on double-click
        filesTable.setRowFactory(tv -> {
            TableRow<Attachment> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openFile(row.getItem().getFile());
                }
            });
            return row;
        });

        // Update attachments when a case is selected
        casesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldCase, newCase) -> {
            if (newCase != null) {
                attachmentsList.setAll(newCase.getAttachments());
            } else {
                attachmentsList.clear();
            }
        });

        // Search filter for cases
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCases(newVal));
    }

    public void setPatient(UserRow patient) {
        this.selectedPatient = patient;
        loadCasesForPatient(patient.getId());
    }

    private void loadCasesForPatient(int patientID) {
        casesList.clear();

        String URL = "jdbc:mysql://localhost:3306/medical";
        String USER = "root";
        String PASS = "";

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
        DoctorCase selectedCase = casesTable.getSelectionModel().getSelectedItem();
        if (selectedCase == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Upload File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF & Images", "*.pdf", "*.png", "*.jpg", "*.jpeg")
        );

        File file = chooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (file != null) {
            Attachment att = new Attachment(file);
            selectedCase.getAttachments().add(att);
            attachmentsList.setAll(selectedCase.getAttachments());
        }
    }

    private void openFile(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleReturn() {
        try {
            // Load the Patients FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Patient.fxml"));
            Parent root = loader.load();

            // Get the current stage (window)
            Stage stage = (Stage) returnButton.getScene().getWindow();

            // Replace the scene with the Patients scene
            Scene scene = new Scene(root, 1280, 768);
            stage.setScene(scene);
            stage.setTitle("Patients");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to return to Patients tab.");
        }
    }


}
