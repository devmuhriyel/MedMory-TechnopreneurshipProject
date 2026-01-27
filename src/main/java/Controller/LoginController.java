package Controller;

import DAO.DatabaseConnection;
import DAO.RecordDAO;
import MVP.SessionManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;
    @FXML private CheckBox rememberMeCheck;
    @FXML private Hyperlink forgotPasswordLink;

    @FXML private VBox mainCard;
    @FXML private VBox formContainer;
    @FXML private VBox logoContainer;

    private SequentialTransition pulseSequence;

    @FXML
    private void initialize() {
        Platform.runLater(this::startPageAnimations);

        // Add enter key handler for convenience
        if (passwordField != null) {
            passwordField.setOnAction(e -> handleLogin());
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

        ParallelTransition cardAnimation = new ParallelTransition(cardFade, cardSlide);
        cardAnimation.play();

        Timeline logoAnimation = new Timeline();
        logoAnimation.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(1000), event -> animateLogo()),
                new KeyFrame(Duration.millis(1200), event -> animateFormFields())
        );
        logoAnimation.play();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showErrorAnimation();
            Platform.runLater(() -> showErrorPopup("Please enter both username/email and password."));
            return;
        }

        showLoadingAnimation();

        Timeline loginProcess = new Timeline(
                new KeyFrame(Duration.millis(1500), event -> {
                    stopLoadingAnimation();

                    // Try email login first, then username login as fallback
                    LoginResult result;
                    if (username.contains("@")) {
                        // If input looks like email, use email login
                        result = LoginService.validateCredentials(username, password);
                    } else {
                        // Otherwise try username login
                        result = LoginService.validateCredentialsByUsername(username, password);
                    }

                    // Handle login result based on status
                    if (result.isSuccess()) {
                        handleSuccessfulLogin(result);
                    } else if (result.isInvalid()) {
                        showErrorAnimation();
                        Platform.runLater(() -> showErrorPopup("Invalid username/email or password."));
                    } else if (result.isError()) {
                        showErrorAnimation();
                        String errorMsg = result.getErrorMessage().isEmpty()
                                ? "System error occurred while logging in."
                                : result.getErrorMessage();
                        Platform.runLater(() -> showErrorPopup(errorMsg));
                    }
                })
        );
        loginProcess.play();
    }

    /**
     * Handle successful login and route to appropriate screen
     */
    private void handleSuccessfulLogin(LoginResult result) {
        // Set session based on user role
        if (result.isDoctor()) {
            SessionManager.setDoctorSession(
                    result.getUserId(),
                    -1,
                    result.getUserName(),
                    usernameField.getText().contains("@") ? usernameField.getText() : null
            );
            showSuccessMessage("Welcome, Dr. " + result.getUserName() + "!");
            loadFXML("MVP/DoctorAccount.fxml");

        } else if (result.isPatient()) {
            SessionManager.setPatientSession(
                    result.getUserId(),
                    result.getUserName(),
                    usernameField.getText().contains("@") ? usernameField.getText() : null
            );
            showSuccessMessage("Welcome, " + result.getUserName() + "!");
            loadFXML("MVP/ClientHomepage.fxml");

        } else if (result.isAdmin()) {
            SessionManager.setAdminSession(
                    result.getUserId(),
                    result.getUserName(),
                    usernameField.getText().contains("@") ? usernameField.getText() : null
            );
            showSuccessMessage("Welcome, Admin " + result.getUserName() + "!");
            loadFXML("MVP/AdminDashboard.fxml");
        }
    }

    /**
     * Stop the loading animation
     */
    private void stopLoadingAnimation() {
        if (pulseSequence != null) pulseSequence.stop();
        loginButton.getStyleClass().remove("loading");
        loginButton.setScaleX(1.0);
        loginButton.setScaleY(1.0);
        loginButton.setText("Log In");
        loginButton.setDisable(false);
    }

    /**
     * Show error popup dialog
     */
    private void showErrorPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText("Authentication Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Load FXML file and switch scene
     */
    private void loadFXML(String filePath) {
        try {
            // Ensure pathing is absolute from the resources root
            String path = filePath.startsWith("/") ? filePath : "/" + filePath;
            Parent root = FXMLLoader.load(getClass().getResource(path));

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);

            // Set appropriate window size based on destination
            if (filePath.contains("Doctor")) {
                stage.setWidth(1280);
                stage.setHeight(768);
            } else if (filePath.contains("Admin")) {
                stage.setWidth(1400);
                stage.setHeight(900);
            } else {
                stage.setWidth(1200);
                stage.setHeight(800);
            }

            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                showErrorPopup("Failed to load interface: " + filePath + "\n\nError: " + e.getMessage());
            });
        }
    }

    /**
     * Show loading animation on login button
     */
    private void showLoadingAnimation() {
        loginButton.getStyleClass().add("loading");
        loginButton.setText("Logging in...");
        loginButton.setDisable(true);

        ScaleTransition pulse1 = new ScaleTransition(Duration.millis(500), loginButton);
        pulse1.setToX(1.05);
        pulse1.setToY(1.05);

        ScaleTransition pulse2 = new ScaleTransition(Duration.millis(500), loginButton);
        pulse2.setToX(1.0);
        pulse2.setToY(1.0);

        pulseSequence = new SequentialTransition(pulse1, pulse2);
        pulseSequence.setCycleCount(Animation.INDEFINITE);
        pulseSequence.play();
    }

    /**
     * Handle signup button click
     */
    @FXML
    private void handleSignup() {
        ScaleTransition click = new ScaleTransition(Duration.millis(100), signupButton);
        click.setToX(0.95);
        click.setToY(0.95);

        ScaleTransition release = new ScaleTransition(Duration.millis(100), signupButton);
        release.setToX(1.0);
        release.setToY(1.0);

        SequentialTransition seq = new SequentialTransition(click, release);
        seq.setOnFinished(e -> {
            try { navigateToSignup(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        seq.play();
    }

    /**
     * Navigate to signup page
     */
    private void navigateToSignup() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Signup.fxml"));
        Parent signupPage = loader.load();
        Stage stage = (Stage) signupButton.getScene().getWindow();
        stage.setWidth(900);
        stage.setHeight(850);
        stage.getScene().setRoot(signupPage);
        stage.centerOnScreen();
    }

    /**
     * Handle forgot password link
     */
    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Recovery");
        alert.setHeaderText("Reset Your Password");
        alert.setContentText("Please contact your system administrator to reset your password.\n\n" +
                "Email: support@medicalsystem.com\n" +
                "Phone: (123) 456-7890");
        alert.showAndWait();
    }

    /**
     * Animate logo entrance
     */
    private void animateLogo() {
        if (logoContainer != null) {
            for (int i = 0; i < logoContainer.getChildren().size(); i++) {
                var child = logoContainer.getChildren().get(i);
                FadeTransition fade = new FadeTransition(Duration.millis(300), child);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                fade.setDelay(Duration.millis(i * 100));
                fade.play();
            }
        }
    }

    /**
     * Animate form fields entrance
     */
    private void animateFormFields() {
        if (formContainer != null) {
            for (int i = 0; i < formContainer.getChildren().size(); i++) {
                var child = formContainer.getChildren().get(i);
                TranslateTransition slide = new TranslateTransition(Duration.millis(400), child);
                slide.setFromY(20);
                slide.setToY(0);
                slide.setDelay(Duration.millis(i * 150));
                slide.play();
            }
        }
    }

    /**
     * Show error shake animation
     */
    private void showErrorAnimation() {
        if (mainCard == null) return;
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), mainCard);
        tt.setByX(10);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }

    /**
     * Show success message (you can enhance this with a toast notification)
     */
    private void showSuccessMessage(String message) {
        System.out.println(message);

        loginButton.setText("Success!");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        PauseTransition pause = new PauseTransition(Duration.millis(500));
        pause.setOnFinished(e -> {
            loginButton.setText("Log In");
            loginButton.setStyle("");
        });
        pause.play();
    }

    /**
     * Clear form fields (useful for logout scenario)
     */
    private void clearFields() {
        if (usernameField != null) usernameField.clear();
        if (passwordField != null) passwordField.clear();
        if (rememberMeCheck != null) rememberMeCheck.setSelected(false);
    }
}