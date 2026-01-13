package Controller;

import DAO.DatabaseConnection;
import DAO.RecordDAO;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;
    @FXML private AnchorPane mainCard;
    @FXML private VBox formContainer;
    @FXML private VBox logoContainer;

    private SequentialTransition pulseSequence;

    @FXML
    private void initialize() {
        Platform.runLater(this::startPageAnimations);
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

        if (username.isEmpty() || password.isEmpty()) {
            showErrorAnimation();
            return;
        }

        showLoadingAnimation();

        Timeline loginProcess = new Timeline(
                new KeyFrame(Duration.millis(1500), event -> {
                    stopLoadingAnimation();

                    // 1. Authenticate user credentials
                    LoginResult result = LoginService.validateCredentials(username, password);

                    switch (result.role) {
                        case "doctor":
                            // 2. BLOCKCHAIN INTEGRITY CHECK: Verify records before allowing access
                            RecordDAO recordDAO = new RecordDAO(DatabaseConnection.getConnection());
                            if (recordDAO.validateBlockchain()) {
                                showSuccessMessage("Integrity Verified. Doctor login success!");
                                loadFXML("MVP/DoctorAccount.fxml");
                            } else {
                                // Trigger if any record data or the hash chain was altered in the DB
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.WARNING);
                                    alert.setTitle("Security Alert");
                                    alert.setHeaderText("Database Integrity Compromised");
                                    alert.setContentText("Warning: The blockchain validation failed. " +
                                            "Medical records may have been tampered with. Access denied.");
                                    alert.showAndWait();
                                });
                            }
                            break;

                        case "client":
                            showSuccessMessage("Client login success!");
                            loadFXML("MVP/ClientAccount.fxml");
                            break;

                        case "invalid":
                            showErrorAnimation();
                            Platform.runLater(() -> showErrorPopup("Invalid username or password."));
                            break;

                        default:
                            Platform.runLater(() -> showErrorPopup("System error occurred while logging in."));
                            break;
                    }
                })
        );
        loginProcess.play();
    }

    private void stopLoadingAnimation() {
        if (pulseSequence != null) pulseSequence.stop();
        loginButton.getStyleClass().remove("loading");
        loginButton.setScaleX(1.0);
        loginButton.setScaleY(1.0);
        loginButton.setText("Log In");
    }

    private void showErrorPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadFXML(String filePath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + filePath));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> showErrorPopup("Failed to load interface: " + filePath));
        }
    }

    private void showLoadingAnimation() {
        loginButton.getStyleClass().add("loading");
        loginButton.setText("Logging in...");

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

    private void navigateToSignup() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Signup.fxml"));
        Parent signupPage = loader.load();
        Stage stage = (Stage) signupButton.getScene().getWindow();
        stage.setWidth(900);
        stage.setHeight(850);
        stage.getScene().setRoot(signupPage);
        stage.centerOnScreen();
    }

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

    private void showErrorAnimation() {
        if (mainCard == null) return;
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), mainCard);
        tt.setByX(10);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }

    private void showSuccessMessage(String message) {
        System.out.println(message);
    }
}