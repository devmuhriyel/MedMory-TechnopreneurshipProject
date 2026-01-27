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

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
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
        fileNameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        dateUploadedCol.setCellValueFactory(new PropertyValueFactory<>("uploadDate"));

        if (filterType != null) {
            filterType.setItems(FXCollections.observableArrayList("All Files", "Images", "PDF Documents", "Lab Results"));
        }

        loadUserSpecificFiles();
    }

    private void loadUserSpecificFiles() {
        fileList.clear();
        int currentPatientID = SessionManager.getUserID();

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
            e.printStackTrace();
        }
    }

    /**
     * COMPLETED: Logic to retrieve BLOB data and open the file.
     */
    @FXML
    private void handleViewFile() {
        MedicalFile selected = medicalFilesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a file to view.");
            return;
        }

        String sql = "SELECT file_data FROM patient_attachments WHERE file_name = ? AND patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, selected.getFileName());
            pstmt.setInt(2, SessionManager.getUserID());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                byte[] fileBytes = rs.getBytes("file_data");

                // Create a temporary file to open
                File tempFile = File.createTempFile("med_doc_", "_" + selected.getFileName());
                tempFile.deleteOnExit();

                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(fileBytes);
                }

                // Open the file using the Desktop API
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(tempFile);
                } else {
                    showError("Opening files is not supported on this system.");
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showError("Could not open file: " + e.getMessage());
        }
    }

    @FXML
    private void handleDownload() {
        MedicalFile selected = medicalFilesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Implementation would be similar to handleViewFile but using a FileChooser
            System.out.println("Downloading: " + selected.getFileName());
        }
    }

    @FXML
    private void handleRefresh() {
        loadUserSpecificFiles();
    }

    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        String targetFxml;

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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }

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