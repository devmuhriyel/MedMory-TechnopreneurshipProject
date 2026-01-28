package Controller.Doctor;

import Controller.LoginService;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Validation Controller - Handles doctor-specific signup completion
 * This is the second step for doctor registration where they provide
 * their specialization, license, and other professional details
 */
public class Validation {

    @FXML private TextField licenseField;
    @FXML private TextField yearField;
    @FXML private ComboBox<String> specializationCombo;
    @FXML private TextArea biographyArea;
    @FXML private Button completeRegistrationButton;
    @FXML private Button backButton;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private AnchorPane mainCard;

    // Data passed from SignupController
    private String doctorName;
    private String doctorEmail;
    private String doctorPassword;
    private String doctorPhone;
    private String doctorBirthday;
    private String doctorAddress;

    /**
     * Set doctor data from the previous signup form
     */
    public void setDoctorData(String name, String email, String password, String phone, String birthday, String address) {
        this.doctorName = name;
        this.doctorEmail = email;
        this.doctorPassword = password;
        this.doctorPhone = phone;
        this.doctorBirthday = birthday;
        this.doctorAddress = address;

        // Update labels to show user info
        if (nameLabel != null) {
            nameLabel.setText("Name: " + name);
        }
        if (emailLabel != null) {
            emailLabel.setText("Email: " + email);
        }
    }

    @FXML
    private void initialize() {
        setupSpecializationCombo();
        setupBiographyPlaceholder();
        Platform.runLater(this::startPageAnimations);
    }

    /**
     * Setup the specialization dropdown with common medical specializations
     */
    private void setupSpecializationCombo() {
        if (specializationCombo == null) return;

        specializationCombo.getItems().addAll(
                "General Medicine",
                "Cardiology",
                "Dermatology",
                "Emergency Medicine",
                "Endocrinology",
                "Family Medicine",
                "Gastroenterology",
                "Hematology",
                "Infectious Disease",
                "Internal Medicine",
                "Nephrology",
                "Neurology",
                "Obstetrics & Gynecology",
                "Oncology",
                "Ophthalmology",
                "Orthopedics",
                "Otolaryngology (ENT)",
                "Pediatrics",
                "Psychiatry",
                "Pulmonology",
                "Radiology",
                "Rheumatology",
                "Surgery",
                "Urology",
                "Other"
        );
    }

    /**
     * Setup biography text area with default text
     */
    private void setupBiographyPlaceholder() {
        if (biographyArea != null) {
            biographyArea.setPromptText("Tell us about your medical background, experience, and areas of expertise...");
        }
    }

    /**
     * Handle the complete registration button
     */
    @FXML
    private void handleCompleteRegistration(ActionEvent event) {
        if (!validateDoctorForm()) {
            showErrorAnimation();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Form Incomplete");
            alert.setHeaderText("Missing Professional Information");
            alert.setContentText("Please fill in all required fields.");
            alert.show();
            return;
        }

        showLoadingAnimation();

        // Prepare doctor data
        String specialization = specializationCombo.getValue();
        String license = licenseField.getText().trim();
        String year = yearField.getText().trim();
        String biography = biographyArea.getText().trim();

        // Create biography text with license info
        String fullBiography = biography;
        if (!license.isEmpty() && !year.isEmpty()) {
            fullBiography = "PRC License: " + license + " | Registered in: " + year + "\n\n" + biography;
        }

        String finalFullBiography = fullBiography;
        Timeline registrationProcess = new Timeline(new KeyFrame(Duration.millis(2000), e -> {
            completeRegistrationButton.getStyleClass().remove("loading");

            // Register doctor using LoginService
            boolean isSaved = LoginService.registerDoctor(
                    doctorName,
                    doctorEmail,
                    doctorPassword,
                    doctorPhone,
                    doctorBirthday,
                    specialization,
                    finalFullBiography,
                    doctorAddress
            );

            if (isSaved) {
                showSuccessMessage("Doctor account created successfully!");
                PauseTransition delay = new PauseTransition(Duration.millis(1500));
                delay.setOnFinished(ev -> {
                    try {
                        navigateToLogin();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                delay.play();
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Registration Failed");
                    alert.setHeaderText("Database Error");
                    alert.setContentText("Failed to create doctor account. Please try again or contact support.");
                    alert.show();
                });
                // Reset button
                completeRegistrationButton.setText("Complete Registration");
                completeRegistrationButton.setStyle("");
                completeRegistrationButton.setDisable(false);
            }
        }));
        registrationProcess.play();
    }

    /**
     * Validate the doctor-specific form fields
     */
    private boolean validateDoctorForm() {
        boolean isValid = true;

        // License field validation
        if (licenseField == null || licenseField.getText() == null || licenseField.getText().trim().isEmpty()) {
            System.out.println("DEBUG: License field is empty");
            if (licenseField != null) highlightField(licenseField);
            isValid = false;
        } else {
            System.out.println("DEBUG: License = " + licenseField.getText().trim());
        }

        // Year field validation
        if (yearField == null || yearField.getText() == null || yearField.getText().trim().isEmpty()) {
            System.out.println("DEBUG: Year field is empty");
            if (yearField != null) highlightField(yearField);
            isValid = false;
        } else {
            String year = yearField.getText().trim();
            System.out.println("DEBUG: Year = " + year);
            if (!year.matches("\\d{4}")) {
                System.out.println("DEBUG: Year format invalid");
                highlightField(yearField);
                isValid = false;
            } else {
                int yearInt = Integer.parseInt(year);
                int currentYear = java.time.Year.now().getValue();
                System.out.println("DEBUG: Year parsed = " + yearInt + ", current = " + currentYear);
                if (yearInt < 1950 || yearInt > currentYear) {
                    System.out.println("DEBUG: Year out of range");
                    highlightField(yearField);
                    isValid = false;
                }
            }
        }

        // Specialization validation
        if (specializationCombo == null || specializationCombo.getValue() == null) {
            System.out.println("DEBUG: Specialization not selected");
            if (specializationCombo != null) highlightField(specializationCombo);
            isValid = false;
        } else {
            System.out.println("DEBUG: Specialization = " + specializationCombo.getValue());
        }

        System.out.println("DEBUG: Validation result = " + isValid);
        return isValid;
    }
    /**
     * Highlight invalid field with red border
     */
    private void highlightField(Control field) {
        field.setStyle("-fx-border-color: #f44336; -fx-border-width: 2px; -fx-border-radius: 5px;");
        Timeline removeError = new Timeline(new KeyFrame(Duration.millis(3000), e -> {
            field.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-border-radius: 5px;");
        }));
        removeError.play();
    }

    /**
     * Handle back button - return to signup form
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Signup.fxml"));
            Parent signupPage = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(signupPage);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Signup.fxml");
        }
    }

    /**
     * Navigate to login page after successful registration
     */
    private void navigateToLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Login.fxml"));
        Parent loginPage = loader.load();
        Stage stage = (Stage) completeRegistrationButton.getScene().getWindow();
        stage.getScene().setRoot(loginPage);
        stage.setWidth(800);
        stage.setHeight(600);
        stage.centerOnScreen();
    }

    /**
     * Show loading animation on button
     */
    private void showLoadingAnimation() {
        completeRegistrationButton.getStyleClass().add("loading");
        completeRegistrationButton.setText("Registering...");
        completeRegistrationButton.setDisable(true);
    }

    /**
     * Show success message
     */
    private void showSuccessMessage(String message) {
        completeRegistrationButton.setText("Success!");
        completeRegistrationButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Doctor Registration Complete");
            alert.setContentText(message);
            alert.show();
        });
    }

    /**
     * Shake animation for errors
     */
    private void showErrorAnimation() {
        if (mainCard == null) return;
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), mainCard);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }

    /**
     * Start page entrance animations
     */
    private void startPageAnimations() {
        if (mainCard == null) return;

        FadeTransition cardFade = new FadeTransition(Duration.millis(800), mainCard);
        cardFade.setFromValue(0.0);
        cardFade.setToValue(1.0);

        TranslateTransition cardSlide = new TranslateTransition(Duration.millis(800), mainCard);
        cardSlide.setFromY(-20);
        cardSlide.setToY(0);

        new ParallelTransition(cardFade, cardSlide).play();
    }
}