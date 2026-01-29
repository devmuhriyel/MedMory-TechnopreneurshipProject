package Controller;

import MVP.Login;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class SignupController {

    @FXML private TextField firstNameField, lastNameField, emailField, phoneField, locationField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private ComboBox<String> birthMonthCombo;
    @FXML private ComboBox<Integer> birthDayCombo;
    @FXML private ComboBox<Integer> birthYearCombo;
    @FXML private RadioButton patientRadio, doctorRadio;
    @FXML private CheckBox consentCheck1, consentCheck2, consentCheck3;
    @FXML private Button createAccountButton, cancelButton;
    @FXML private Hyperlink doctorSignupLink;
    @FXML private AnchorPane mainCard;
    @FXML private GridPane formGrid;
    @FXML private VBox consentContainer;
    @FXML private HBox logoContainer;

    private final ToggleGroup accountTypeGroup = new ToggleGroup();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();

        String targetFxml;

        switch (id) {
            case "accountButton":
                targetFxml = "DoctorAccount.fxml";
                break;
            case "records":
            case "recordsButton":
                targetFxml = "ViewMedicalRecordList.fxml";
                break;
            case "casesButton":
                targetFxml = "Patient.fxml";
                break;
            case "calendarButton":
                targetFxml = "Calendar.fxml";
                break;
            case "chatButton":
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
    private void initialize() {
        if (patientRadio != null) patientRadio.setToggleGroup(accountTypeGroup);
        if (doctorRadio != null) doctorRadio.setToggleGroup(accountTypeGroup);
        if (patientRadio != null) patientRadio.setSelected(true);

        setupBirthdayDropdowns();

        if (cancelButton != null) {
            cancelButton.setVisible(true);
            cancelButton.setOpacity(1.0);
            cancelButton.setManaged(true);
        }
        if (createAccountButton != null) {
            createAccountButton.setVisible(true);
            createAccountButton.setOpacity(1.0);
            createAccountButton.setManaged(true);
        }

        Platform.runLater(this::startPageAnimations);
    }

    /**
     * Navigates to the Doctor Validation screen after validating the form
     * and passing the user data to the next controller.
     */
    @FXML
    private void handleDoctorSignupLink(ActionEvent event) {
        if (doctorRadio != null) doctorRadio.setSelected(true);

        if (!validateForm(true)) {
            showErrorAnimation();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Form Incomplete");
            alert.setHeaderText("Missing Information");
            alert.setContentText("Please correct the highlighted fields before signing up as a doctor.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Validation.fxml"));
            Parent root = loader.load();

            Controller.Doctor.Validation validationController = loader.getController();
            validationController.setDoctorData(
                    firstNameField.getText().trim() + " " + lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    passwordField.getText(),
                    phoneField.getText().trim(),
                    formatBirthday(),
                    locationField.getText().trim()
            );

            Stage stage = (Stage) ((Hyperlink) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 812, 717);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Validation.fxml. Check your file paths.");
        }
    }

    @FXML
    private void handleCreateAccount() {
        if (!validateForm(false)) {
            showErrorAnimation();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Form Incomplete");
            alert.setHeaderText(null);
            alert.setContentText("Please correct the highlighted fields and accept the terms and conditions.");
            alert.show();
            return;
        }

        // Check if email already exists
        if (LoginService.emailExists(emailField.getText().trim())) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Registration Failed");
                alert.setHeaderText("Email Already Exists");
                alert.setContentText("An account with this email already exists. Please use a different email or try logging in.");
                alert.show();
            });
            highlightField(emailField);
            return;
        }

        showLoadingAnimation();

        // Data processing for Patient registration
        String fullName = firstNameField.getText().trim() + " " + lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String phone = phoneField.getText().trim();
        String birthday = formatBirthday(); // YYYY-MM-DD format
        String location = locationField.getText().trim();

        Timeline createProcess = new Timeline(new KeyFrame(Duration.millis(2000), event -> {
            createAccountButton.getStyleClass().remove("loading");

            // Register patient using new LoginService method
            boolean isSaved = LoginService.registerPatient(fullName, email, password, phone, birthday, location);

            if (isSaved) {
                showSuccessMessage("Account created successfully!");
                PauseTransition delay = new PauseTransition(Duration.millis(1500));
                delay.setOnFinished(e -> {
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
                    alert.setContentText("Failed to create account. Please try again or contact support.");
                    alert.show();
                });
                // Reset button
                createAccountButton.setText("Create Account");
                createAccountButton.setStyle("");
            }
        }));
        createProcess.play();
    }

    /**
     * Format birthday from ComboBoxes to YYYY-MM-DD format
     */
    private String formatBirthday() {
        if (!isBirthdaySelected()) {
            return null;
        }

        String month = String.format("%02d", birthMonthCombo.getItems().indexOf(birthMonthCombo.getValue()) + 1);
        String day = String.format("%02d", birthDayCombo.getValue());
        String year = String.valueOf(birthYearCombo.getValue());

        return year + "-" + month + "-" + day; // YYYY-MM-DD format
    }

    private boolean validateForm(boolean forDoctorFlow) {
        boolean isValid = true;

        // First name validation
        if (firstNameField == null || firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            if (firstNameField != null) highlightField(firstNameField);
            isValid = false;
        }

        // Last name validation
        if (lastNameField == null || lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            if (lastNameField != null) highlightField(lastNameField);
            isValid = false;
        }

        // Email validation
        if (emailField == null || emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            if (emailField != null) highlightField(emailField);
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            highlightField(emailField);
            isValid = false;
        }

        // Phone validation
        if (phoneField == null || phoneField.getText() == null || phoneField.getText().trim().isEmpty()) {
            if (phoneField != null) highlightField(phoneField);
            isValid = false;
        } else {
            String digits = phoneField.getText().replaceAll("\\D", "");
            if (digits.length() < 10 || digits.length() > 15) {
                highlightField(phoneField);
                isValid = false;
            }
        }

        // Location validation (required for address field)
        if (locationField == null || locationField.getText() == null || locationField.getText().trim().isEmpty()) {
            if (locationField != null) highlightField(locationField);
            isValid = false;
        }

        // Birthday validation
        if (!isBirthdaySelected()) {
            if (birthMonthCombo != null) highlightField(birthMonthCombo);
            if (birthDayCombo != null) highlightField(birthDayCombo);
            if (birthYearCombo != null) highlightField(birthYearCombo);
            isValid = false;
        } else {
            // Validate age (must be at least 13 years old for patients, 21 for doctors)
            int minAge = forDoctorFlow ? 21 : 13;
            if (!isValidAge(minAge)) {
                highlightField(birthYearCombo);
                isValid = false;
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Invalid Age");
                    alert.setHeaderText("Age Requirement Not Met");
                    alert.setContentText(forDoctorFlow ?
                            "Doctors must be at least 21 years old." :
                            "You must be at least 13 years old to register.");
                    alert.show();
                });
            }
        }

        // Password validation: minimum 6 characters
        if (passwordField == null || passwordField.getText() == null || passwordField.getText().trim().isEmpty()) {
            if (passwordField != null) highlightField(passwordField);
            isValid = false;
        } else if (passwordField.getText().length() < 6) {
            highlightField(passwordField);
            isValid = false;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Password");
                alert.setHeaderText("Password Too Short");
                alert.setContentText("Password must be at least 6 characters long.");
                alert.show();
            });
        }

        // Confirm password validation
        if (confirmPasswordField == null || confirmPasswordField.getText() == null || confirmPasswordField.getText().trim().isEmpty()) {
            if (confirmPasswordField != null) highlightField(confirmPasswordField);
            isValid = false;
        } else if (passwordField != null && !passwordField.getText().equals(confirmPasswordField.getText())) {
            highlightField(passwordField);
            highlightField(confirmPasswordField);
            isValid = false;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Password Mismatch");
                alert.setHeaderText("Passwords Don't Match");
                alert.setContentText("Please ensure both password fields match.");
                alert.show();
            });
        }

        // Consent checkboxes validation
        if (!consentCheck1.isSelected() || !consentCheck2.isSelected() || !consentCheck3.isSelected()) {
            animateConsentCheckboxes();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validate if the selected birthday meets the minimum age requirement
     */
    private boolean isValidAge(int minAge) {
        if (!isBirthdaySelected()) {
            return false;
        }

        int year = birthYearCombo.getValue();
        int month = birthMonthCombo.getItems().indexOf(birthMonthCombo.getValue()) + 1;
        int day = birthDayCombo.getValue();

        LocalDate birthdate = LocalDate.of(year, month, day);
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.minusYears(minAge);

        return birthdate.isBefore(minDate) || birthdate.isEqual(minDate);
    }

    private boolean isBirthdaySelected() {
        return birthMonthCombo != null && birthDayCombo != null && birthYearCombo != null
                && birthMonthCombo.getValue() != null
                && birthDayCombo.getValue() != null
                && birthYearCombo.getValue() != null;
    }

    private void setupBirthdayDropdowns() {
        if (birthMonthCombo == null || birthDayCombo == null || birthYearCombo == null) return;

        birthMonthCombo.getItems().setAll(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        );

        birthDayCombo.getItems().clear();
        for (int d = 1; d <= 31; d++) birthDayCombo.getItems().add(d);

        int currentYear = LocalDate.now().getYear();
        birthYearCombo.getItems().clear();
        for (int y = currentYear; y >= 1900; y--) birthYearCombo.getItems().add(y);
    }

    private void highlightField(Control field) {
        field.setStyle("-fx-border-color: #f44336; -fx-border-width: 2px; -fx-border-radius: 5px;");
        Timeline removeError = new Timeline(new KeyFrame(Duration.millis(3000), e -> {
            field.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-border-radius: 5px;");
        }));
        removeError.play();
    }

    // --- Navigation and Animations ---

    private void navigateToLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Login.fxml"));
        Parent loginPage = loader.load();
        Stage stage = (Stage) createAccountButton.getScene().getWindow();
        stage.getScene().setRoot(loginPage);
        stage.setWidth(800);
        stage.setHeight(600);
        stage.centerOnScreen();
    }

    @FXML
    private void handleCancel() {
        try { navigateToLogin(); } catch (Exception e) { e.printStackTrace(); }
    }

    private void showLoadingAnimation() {
        createAccountButton.getStyleClass().add("loading");
        createAccountButton.setText("Registering...");
        createAccountButton.setDisable(true);
    }

    private void showSuccessMessage(String message) {
        createAccountButton.setText("Success!");
        createAccountButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Registration Complete");
            alert.setContentText(message);
            alert.show();
        });
    }

    private void showErrorAnimation() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), mainCard);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void animateConsentCheckboxes() {
        for (CheckBox cb : new CheckBox[]{consentCheck1, consentCheck2, consentCheck3}) {
            if (!cb.isSelected()) {
                ScaleTransition pulse = new ScaleTransition(Duration.millis(200), cb);
                pulse.setToX(1.2);
                pulse.setToY(1.2);
                pulse.setCycleCount(2);
                pulse.setAutoReverse(true);
                pulse.play();
            }
        }
    }

    private void startPageAnimations() {
        if (mainCard == null) return;
        FadeTransition cardFade = new FadeTransition(Duration.millis(800), mainCard);
        cardFade.setFromValue(0.0);
        cardFade.setToValue(1.0);
        TranslateTransition cardSlide = new TranslateTransition(Duration.millis(800), mainCard);
        cardSlide.setFromY(-20);
        cardSlide.setToY(0);
        new ParallelTransition(cardFade, cardSlide).play();

        Timeline pageAnimation = new Timeline();
        pageAnimation.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(1000), e -> animateWelcomeSection()),
                new KeyFrame(Duration.millis(1200), e -> animateLogo()),
                new KeyFrame(Duration.millis(1400), e -> animateFormFields()),
                new KeyFrame(Duration.millis(1600), e -> animateConsentSection())
        );
        pageAnimation.play();
    }

    private void animateWelcomeSection() {
        VBox welcomeSection = (VBox) mainCard.lookup(".welcome-section");
        if (welcomeSection != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(500), welcomeSection);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            TranslateTransition slide = new TranslateTransition(Duration.millis(500), welcomeSection);
            slide.setFromX(-20);
            slide.setToX(0);
            new ParallelTransition(fade, slide).play();
        }
    }

    private void animateLogo() {
        if (logoContainer != null) {
            for (int i = 0; i < logoContainer.getChildren().size(); i++) {
                var child = logoContainer.getChildren().get(i);
                FadeTransition fade = new FadeTransition(Duration.millis(300), child);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                TranslateTransition slide = new TranslateTransition(Duration.millis(300), child);
                slide.setFromY(-10);
                slide.setToY(0);
                ParallelTransition pt = new ParallelTransition(fade, slide);
                pt.setDelay(Duration.millis(i * 100));
                pt.play();
            }
        }
    }

    private void animateFormFields() {
        if (formGrid != null) {
            for (int i = 0; i < formGrid.getChildren().size(); i++) {
                var child = formGrid.getChildren().get(i);
                FadeTransition fade = new FadeTransition(Duration.millis(300), child);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                TranslateTransition slide = new TranslateTransition(Duration.millis(300), child);
                slide.setFromY(20);
                slide.setToY(0);
                ParallelTransition fieldAnim = new ParallelTransition(fade, slide);
                fieldAnim.setDelay(Duration.millis(i * 50));
                fieldAnim.play();
            }
        }
    }

    private void animateConsentSection() {
        if (consentContainer != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(500), consentContainer);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            TranslateTransition slide = new TranslateTransition(Duration.millis(500), consentContainer);
            slide.setFromY(30);
            slide.setToY(0);
            new ParallelTransition(fade, slide).play();
        }
    }
}