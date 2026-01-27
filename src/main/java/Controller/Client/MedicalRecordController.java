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

    @FXML private TableView<MedicalFile> medicalFilesTable;
    @FXML private TableColumn<MedicalFile, String> fileNameCol;
    @FXML private TableColumn<MedicalFile, String> dateUploadedCol;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;

    private ObservableList<MedicalFile> fileList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Setup the Table Columns to match the MedicalFile class properties
        fileNameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        dateUploadedCol.setCellValueFactory(new PropertyValueFactory<>("uploadDate"));

        // 2. Add options to the filter ComboBox
        if (filterType != null) {
            filterType.setItems(FXCollections.observableArrayList("All Files", "Images", "PDF Documents", "Lab Results"));
        }

        // 3. Load only the files belonging to the currently logged-in user
        loadUserSpecificFiles();
    }

    private void loadUserSpecificFiles() {
        fileList.clear();

        // Use the SessionManager to get the ID of the person who just logged in
        int currentPatientID = SessionManager.getUserID();

        // SECURITY: The WHERE clause ensures Patient A cannot see Patient B's files
        String sql = "SELECT file_name, uploaded_at FROM patient_attachments WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentPatientID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                fileList.add(new MedicalFile(
                        rs.getString("file_name"),
                        rs.getString("uploaded_at")
                ));
            }
            medicalFilesTable.setItems(fileList);

        } catch (SQLException e) {
            System.err.println("Database error while fetching attachments.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadUserSpecificFiles();
    }

    @FXML
    private void handleViewFile() {
        MedicalFile selected = medicalFilesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Logic to fetch the BLOB (file_data) from the DB would go here
            System.out.println("Viewing: " + selected.getFileName());
        }
    }

    @FXML
    private void handleDownload() {
        MedicalFile selected = medicalFilesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Downloading: " + selected.getFileName());
        }
    }

    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        String targetFxml = "";

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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(targetFxml));
            Scene scene = new Scene(fxmlLoader.load(), 1366, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner class required for the TableView to display data properly
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