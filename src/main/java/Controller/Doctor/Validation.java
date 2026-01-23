package Controller.Doctor;

import DAO.RecordDAO;
import DAO.DatabaseConnection;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Validation extends Application {

    @FXML private TextField licenseNumberInput;
    @FXML private TextField registrationYearInput;

    // Fields to hold data passed from SignupController
    private String name, email, password, phone, specialization;

    // Setter to receive data from SignupController
    public void setDoctorData(String name, String email, String password, String phone, String specialization) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.specialization = specialization;
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MVP/Validation.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 812, 717);
        stage.setTitle("MEDMORY - Doctor Verification");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleVerification(ActionEvent event) {
        String license = licenseNumberInput.getText().trim();
        String year = registrationYearInput.getText().trim();

        // 1. Validate UI Fields
        if (license.isEmpty() || year.isEmpty()) {
            showError("Please enter your license number and registration year.");
            return;
        }

        // 2. CRITICAL FIX: Validate that Signup data was actually passed
        if (this.name == null || this.name.isEmpty()) {
            showError("Missing registration data (Name/Email). Please go back and fill the first form.");
            return;
        }

        if (this.specialization == null || this.specialization.trim().isEmpty()) {
            showError("Missing specialization. Please go back and select your specialization.");
            return;
        }

        if (license.matches("\\d{7}")) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                RecordDAO dao = new RecordDAO(conn);

                // Call the registerDoctor method in RecordDAO
                boolean success = dao.registerDoctor(this.name, this.email, this.password, this.phone, this.specialization, license, year);

                if (success) {
                    showSuccess("Account created successfully! Redirecting to Login...");
                    navigateToLogin(event);
                } else {
                    showError("Database Error: Could not save the account.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Database Error: " + e.getMessage());
            }
        } else {
            showError("Invalid License Number. Please enter a valid 7-digit PRC ID.");
        }
    }

    private void navigateToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 768));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Signup.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 812, 717));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.show();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Verification Successful");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}