package Controller.Doctor;

import CommonModel.DoctorCase;
import CommonModel.UserRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.*;

public class HistoryController {

    @FXML private TableView<DoctorCase> casesTable;
    @FXML private TableColumn<DoctorCase, String> caseDateCol;
    @FXML private TableColumn<DoctorCase, String> treatmentCol;
    @FXML private TableColumn<DoctorCase, String> patientCol;
    @FXML private TableColumn<DoctorCase, String> diagnosisCol;
    @FXML private TableColumn<DoctorCase, String> completionDateCol;

    @FXML private TextField searchField;
    @FXML private Button returnButton;

    private UserRow selectedPatient;

    // Database credentials (adjust as needed)
    private final String URL = "jdbc:mysql://localhost:3306/medical";
    private final String USER = "root";
    private final String PASS = ""; // your DB password

    private final ObservableList<DoctorCase> casesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Bind columns to DoctorCase properties
        caseDateCol.setCellValueFactory(new PropertyValueFactory<>("caseDate"));
        treatmentCol.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        diagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        completionDateCol.setCellValueFactory(new PropertyValueFactory<>("dateCompleted"));

        // Optional: filter by search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCases(newVal));
    }

    public void setPatient(UserRow patient) {
        this.selectedPatient = patient;
        loadCasesForPatient(patient.getId());
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
                dc.setDiagnosis(rs.getString("diagnosis"));
                dc.setTreatment(rs.getString("treatment"));
                dc.setDateCompleted(rs.getString("date_completed"));
                dc.setCaseDate(rs.getString("date_completed")); // or leave blank if you have another field
                casesList.add(dc);
            }

            casesTable.setItems(casesList);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to load patient cases.");
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
