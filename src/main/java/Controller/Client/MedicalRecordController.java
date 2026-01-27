package Controller.Client;

import DAO.DatabaseConnection;
import MVP.Login;
import CommonModel.SessionManager;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MedicalRecordController {

    // FXML Injections matching your new Table-based UI
    @FXML private TableView<MedicalFile> medicalFilesTable;
    @FXML private TableColumn<MedicalFile, String> fileNameCol;
    @FXML private TableColumn<MedicalFile, String> dateUploadedCol;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;

    private ObservableList<MedicalFile> fileList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup table columns to match MedicalFile properties
        fileNameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        dateUploadedCol.setCellValueFactory(new PropertyValueFactory<>("uploadDate"));

        // Initialize ComboBox options
        if (filterType != null) {
            filterType.setItems(FXCollections.observableArrayList("All Categories", "Reports", "Prescriptions", "Lab Results"));
        }

        // Load data for the logged-in user
        loadMedicalFiles();
    }

    private void loadMedicalFiles() {
        fileList.clear();
        int patientID = SessionManager.getUserID(); // Retrieve current session ID

        String query = "SELECT file_name, uploaded_at FROM patient_attachments WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, patientID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                fileList.add(new MedicalFile(
                        rs.getString("file_name"),
                        rs.getString("uploaded_at")
                ));
            }
            medicalFilesTable.setItems(fileList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadMedicalFiles();
    }

    @FXML
    private void handleViewFile() {
        MedicalFile selected = medicalFilesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Opening file: " + selected.getFileName());
            // Logic to open/stream blob data from database goes here
        } else {
            showError("Please select a file to view.");
        }
    }

    @FXML
    private void handleDownload() {
        MedicalFile selected = medicalFilesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Downloading file: " + selected.getFileName());
        }
    }

    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        String targetFxml = "";

        // Updated mapping to match your navigation requirements
        switch (buttonId) {
            case "homeButton": targetFxml = "/MVP/ClientHomepage.fxml"; break;
            case "accountButton": targetFxml = "/MVP/ClientAccount.fxml"; break;
            case "medicalRecordButton": targetFxml = "/MVP/MedicalRecord.fxml"; break;
            case "calendarButton": targetFxml = "/MVP/ClientCalendar.fxml"; break;
            case "chatsButton": targetFxml = "/MVP/ClientChat.fxml"; break;
            case "settingsButton": targetFxml = "/MVP/ClientSettings.fxml"; break;
            default: return;
        }

        try {
            // Using absolute resource path with leading '/'
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(targetFxml));
            Scene scene = new Scene(fxmlLoader.load(), 1366, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load: " + targetFxml);
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Selection Required");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for TableView data binding
    public static class MedicalFile {
        private final String fileName;
        private final String uploadDate;

        public MedicalFile(String fileName, String uploadDate) {
            this.fileName = fileName;
            this.uploadDate = uploadDate;
        }

        public String getFileName() { return fileName; }
        public String getUploadDate() { return uploadDate; }
    }
}