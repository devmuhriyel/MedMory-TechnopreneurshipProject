package Controller;

import DAO.DatabaseConnection;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
            case "accountButton": targetFxml = "DoctorAccount.fxml"; break;
            case "records":
            case "recordsButton": targetFxml = "ViewMedicalRecordList.fxml"; break;
            case "casesButton": targetFxml = "Patient.fxml"; break;
            case "calendarButton": targetFxml = "Calendar.fxml"; break;
            case "chatButton": targetFxml = "Chat.fxml"; break;
            case "settingsButton": targetFxml = "DoctorSettings.fxml"; break;
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
                    firstNameField.getText() + " " + lastNameField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    phoneField.getText()
            );

            Stage stage = (Stage) ((Hyperlink) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 812, 717);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
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

        showLoadingAnimation();

        String fullName = firstNameField.getText().trim() + " " + lastNameField.getText().trim();
        String pass = passwordField.getText();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String loc = locationField.getText().trim();
        String type = (doctorRadio != null && doctorRadio.isSelected()) ? "Doctor" : "Patient";

        // FIX: Convert ComboBox values to YYYY-MM-DD format for MySQL DATE column
        int monthIndex = birthMonthCombo.getSelectionModel().getSelectedIndex() + 1;
        String bday = String.format("%d-%02d-%02d",
                birthYearCombo.getValue(),
                monthIndex,
                birthDayCombo.getValue());

        boolean isSaved = registerUserInDatabase(fullName, pass, phone, type, email, bday, loc);

        Timeline createProcess = new Timeline(new KeyFrame(Duration.millis(2000), event -> {
            createAccountButton.getStyleClass().remove("loading");

            if (isSaved) {
                showSuccessMessage("Account created successfully!");
                PauseTransition delay = new PauseTransition(Duration.millis(1500));
                delay.setOnFinished(e -> {
                    try { navigateToLogin(); } catch (Exception ex) { ex.printStackTrace(); }
                });
                delay.play();
            } else {
                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setContentText("Database error. Account registration failed.");
                    a.show();
                });
            }
        }));
        createProcess.play();
    }

    private boolean registerUserInDatabase(String name, String pass, String phone, String type, String email, String bday, String loc) {
        // Using 'contact_number' and 'user_type' to match your schema
        String sql = "INSERT INTO users (name, password, contact_number, user_type, email, birthday, location) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, pass);
            pstmt.setString(3, phone);
            pstmt.setString(4, type);
            pstmt.setString(5, email);
            pstmt.setString(6, bday); // Correct YYYY-MM-DD string
            pstmt.setString(7, loc);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean validateForm(boolean forDoctorFlow) {
        boolean isValid = true;

        TextField[] fields = { firstNameField, lastNameField, emailField, phoneField, locationField };
        for (TextField field : fields) {
            if (field == null || field.getText() == null || field.getText().trim().isEmpty()) {
                if (field != null) highlightField(field);
                isValid = false;
            }
        }

        if (!isBirthdaySelected()) {
            if (birthMonthCombo != null) highlightField(birthMonthCombo);
            if (birthDayCombo != null) highlightField(birthDayCombo);
            if (birthYearCombo != null) highlightField(birthYearCombo);
            isValid = false;
        }

        if (emailField != null && emailField.getText() != null && !emailField.getText().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
                highlightField(emailField);
                isValid = false;
            }
        }

        if (passwordField == null || passwordField.getText().length() < 6 || !passwordField.getText().equals(confirmPasswordField.getText())) {
            highlightField(confirmPasswordField);
            isValid = false;
        }

        if (!consentCheck1.isSelected() || !consentCheck2.isSelected() || !consentCheck3.isSelected()) {
            animateConsentCheckboxes();
            isValid = false;
        }

        return isValid;
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

    private void navigateToLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Login.fxml"));
        Parent loginPage = loader.load();
        Stage stage = (Stage) createAccountButton.getScene().getWindow();
        stage.getScene().setRoot(loginPage);
        stage.setWidth(800);
        stage.setHeight(600);
        stage.centerOnScreen();
    }

    @FXML private void handleCancel() { try { navigateToLogin(); } catch (Exception e) { e.printStackTrace(); } }
    private void showLoadingAnimation() { createAccountButton.getStyleClass().add("loading"); createAccountButton.setText("Registering..."); }
    private void showSuccessMessage(String m) {
        createAccountButton.setText("Success!");
        createAccountButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(m);
            alert.show();
        });
    }

    private void showErrorAnimation() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), mainCard);
        shake.setByX(10); shake.setCycleCount(4); shake.setAutoReverse(true); shake.play();
    }

    private void animateConsentCheckboxes() {
        for (CheckBox cb : new CheckBox[]{consentCheck1, consentCheck2, consentCheck3}) {
            if (!cb.isSelected()) {
                ScaleTransition pulse = new ScaleTransition(Duration.millis(200), cb);
                pulse.setToX(1.2); pulse.setToY(1.2); pulse.setCycleCount(2); pulse.setAutoReverse(true); pulse.play();
            }
        }
    }

    private void startPageAnimations() {
        if (mainCard == null) return;
        FadeTransition cardFade = new FadeTransition(Duration.millis(800), mainCard);
        cardFade.setFromValue(0.0); cardFade.setToValue(1.0);
        TranslateTransition cardSlide = new TranslateTransition(Duration.millis(800), mainCard);
        cardSlide.setFromY(-20); cardSlide.setToY(0);
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
            fade.setFromValue(0.0); fade.setToValue(1.0);
            TranslateTransition slide = new TranslateTransition(Duration.millis(500), welcomeSection);
            slide.setFromX(-20); slide.setToX(0);
            new ParallelTransition(fade, slide).play();
        }
    }

    private void animateLogo() {
        if (logoContainer != null) {
            for (Node child : logoContainer.getChildren()) {
                FadeTransition fade = new FadeTransition(Duration.millis(300), child);
                fade.setFromValue(0.0); fade.setToValue(1.0);
                fade.play();
            }
        }
    }

    private void animateFormFields() {
        if (formGrid != null) {
            for (Node child : formGrid.getChildren()) {
                FadeTransition fade = new FadeTransition(Duration.millis(300), child);
                fade.setFromValue(0.0); fade.setToValue(1.0);
                fade.play();
            }
        }
    }

    private void animateConsentSection() {
        if (consentContainer != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(500), consentContainer);
            fade.setFromValue(0.0); fade.setToValue(1.0);
            fade.play();
        }
    }
}