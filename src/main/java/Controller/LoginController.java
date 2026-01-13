package Controller;

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

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signupButton;

    @FXML
    private AnchorPane mainCard;

    @FXML
    private VBox formContainer;

    @FXML
    private VBox logoContainer;

    @FXML
    private void initialize() {
        System.out.println("LoginController initialized");
        System.out.println("mainCard: " + mainCard);
        System.out.println("logoContainer: " + logoContainer);
        System.out.println("formContainer: " + formContainer);

        // Defer animations until after the scene is fully laid out
        Platform.runLater(this::startPageAnimations);
    }

    private void startPageAnimations() {
        // Check if mainCard is null before animating
        if (mainCard == null) {
            return;
        }

        // Main card slide in animation
        FadeTransition cardFade = new FadeTransition(Duration.millis(800), mainCard);
        cardFade.setFromValue(0.0);
        cardFade.setToValue(1.0);

        TranslateTransition cardSlide = new TranslateTransition(Duration.millis(800), mainCard);
        cardSlide.setFromY(-20);
        cardSlide.setToY(0);

        ParallelTransition cardAnimation = new ParallelTransition(cardFade, cardSlide);
        cardAnimation.play();

        // Logo animation with delay
        Timeline logoAnimation = new Timeline();
        logoAnimation.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(1000), event -> animateLogo()),
                new KeyFrame(Duration.millis(1200), event -> animateFormFields())
        );
        logoAnimation.play();
    }

    private void loadFXML(String filePath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + filePath));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorPopup("Failed to load interface: " + filePath);
        }
    }

    private void showErrorPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Login Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void animateLogo() {
        if (logoContainer != null && logoContainer.getChildren() != null) {
            for (int i = 0; i < logoContainer.getChildren().size(); i++) {
                var child = logoContainer.getChildren().get(i);

                FadeTransition fade = new FadeTransition(Duration.millis(300), child);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);

                TranslateTransition slide = new TranslateTransition(Duration.millis(300), child);
                slide.setFromY(-10);
                slide.setToY(0);

                ScaleTransition scale = new ScaleTransition(Duration.millis(300), child);
                scale.setFromX(0.8);
                scale.setFromY(0.8);
                scale.setToX(1.0);
                scale.setToY(1.0);

                ParallelTransition logoPart = new ParallelTransition(fade, slide, scale);
                logoPart.setDelay(Duration.millis(i * 100));
                logoPart.play();
            }
        }
    }

    private void animateFormFields() {
        if (formContainer != null && formContainer.getChildren() != null) {
            for (int i = 0; i < formContainer.getChildren().size(); i++) {
                var child = formContainer.getChildren().get(i);

                FadeTransition fade = new FadeTransition(Duration.millis(400), child);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);

                TranslateTransition slide = new TranslateTransition(Duration.millis(400), child);
                slide.setFromY(20);
                slide.setToY(0);

                ParallelTransition fieldAnimation = new ParallelTransition(fade, slide);
                fieldAnimation.setDelay(Duration.millis(i * 150));
                fieldAnimation.play();
            }
        }
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

                    // Stop loading animation
                    loginButton.getStyleClass().remove("loading");
                    loginButton.setText("Log In");

                    // Validate credentials via DB
                    LoginResult result = LoginService.validateCredentials(username, password);

                    switch (result.role) {

                        case "doctor":
                            showSuccessMessage("Doctor login success!");
                            loadFXML("MVP/DoctorAccount.fxml");
                            break;

                        case "client":
                            showSuccessMessage("Client login success!");
                            loadFXML("MVP/ClientAccount.fxml");
                            break;

                        case "invalid":
                            showErrorAnimation();
                            showErrorPopup("Invalid username or password.");
                            break;

                        case "error":
                            showErrorPopup("System error occurred while logging in.");
                            break;
                    }
                })
        );

        loginProcess.play();
    }

    @FXML
    private void handleSignup() {
        // Animate button click
        ScaleTransition clickAnimation = new ScaleTransition(Duration.millis(100), signupButton);
        clickAnimation.setFromX(1.0);
        clickAnimation.setFromY(1.0);
        clickAnimation.setToX(0.95);
        clickAnimation.setToY(0.95);

        ScaleTransition releaseAnimation = new ScaleTransition(Duration.millis(100), signupButton);
        releaseAnimation.setFromX(0.95);
        releaseAnimation.setFromY(0.95);
        releaseAnimation.setToX(1.0);
        releaseAnimation.setToY(1.0);

        SequentialTransition clickSequence = new SequentialTransition(clickAnimation, releaseAnimation);
        clickSequence.setOnFinished(event -> {
            // Navigate to signup page
            try {
                navigateToSignup();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        clickSequence.play();
    }

    private void showErrorAnimation() {
        if (mainCard == null) return;

        // Shake animation for error
        TranslateTransition shake1 = new TranslateTransition(Duration.millis(50), mainCard);
        shake1.setFromX(0);
        shake1.setToX(-5);

        TranslateTransition shake2 = new TranslateTransition(Duration.millis(50), mainCard);
        shake2.setFromX(-5);
        shake2.setToX(5);

        TranslateTransition shake3 = new TranslateTransition(Duration.millis(50), mainCard);
        shake3.setFromX(5);
        shake3.setToX(-5);

        TranslateTransition shake4 = new TranslateTransition(Duration.millis(50), mainCard);
        shake4.setFromX(-5);
        shake4.setToX(0);

        SequentialTransition shakeSequence = new SequentialTransition(shake1, shake2, shake3, shake4);
        shakeSequence.play();

        // Highlight empty fields
        if (usernameField.getText().trim().isEmpty()) {
            highlightField(usernameField);
        }
        if (passwordField.getText().trim().isEmpty()) {
            highlightField(passwordField);
        }
    }

    private void highlightField(Control field) {
        if (field == null) return;

        field.getStyleClass().add("error");

        Timeline removeError = new Timeline();
        removeError.getKeyFrames().add(new KeyFrame(Duration.millis(3000), event -> {
            field.getStyleClass().remove("error");
        }));
        removeError.play();
    }

    private void showLoadingAnimation() {
        if (loginButton == null) return;

        loginButton.getStyleClass().add("loading");
        loginButton.setText("Logging in...");

        // Pulse animation for loading
        ScaleTransition pulse1 = new ScaleTransition(Duration.millis(500), loginButton);
        pulse1.setFromX(1.0);
        pulse1.setFromY(1.0);
        pulse1.setToX(1.05);
        pulse1.setToY(1.05);

        ScaleTransition pulse2 = new ScaleTransition(Duration.millis(500), loginButton);
        pulse2.setFromX(1.05);
        pulse2.setFromY(1.05);
        pulse2.setToX(1.0);
        pulse2.setToY(1.0);

        SequentialTransition pulseSequence = new SequentialTransition(pulse1, pulse2);
        pulseSequence.setCycleCount(Animation.INDEFINITE);
        pulseSequence.play();
    }

    private void showSuccessMessage(String message) {
        // Create success notification (you can customize this)
        System.out.println(message);

        if (loginButton == null) return;

        // You can add a success animation here
        ScaleTransition successAnimation = new ScaleTransition(Duration.millis(200), loginButton);
        successAnimation.setFromX(1.0);
        successAnimation.setFromY(1.0);
        successAnimation.setToX(1.1);
        successAnimation.setToY(1.1);

        ScaleTransition successReturn = new ScaleTransition(Duration.millis(200), loginButton);
        successReturn.setFromX(1.1);
        successReturn.setFromY(1.1);
        successReturn.setToX(1.0);
        successReturn.setToY(1.0);

        SequentialTransition successSequence = new SequentialTransition(successAnimation, successReturn);
        successSequence.play();
    }

    private void navigateToSignup() throws Exception {
        // Load signup page
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Signup.fxml"));
        AnchorPane signupPage = loader.load();

        // Get the stage first
        javafx.stage.Stage stage = (javafx.stage.Stage) signupButton.getScene().getWindow();

        // Resize stage BEFORE changing the scene content
        stage.setWidth(900);
        stage.setHeight(850);

        // Replace current scene content
        AnchorPane currentRoot = (AnchorPane) signupButton.getScene().getRoot();
        currentRoot.getChildren().clear();
        currentRoot.getChildren().add(signupPage);

        // Ensure the signup page fills the parent
        AnchorPane.setTopAnchor(signupPage, 0.0);
        AnchorPane.setBottomAnchor(signupPage, 0.0);
        AnchorPane.setLeftAnchor(signupPage, 0.0);
        AnchorPane.setRightAnchor(signupPage, 0.0);

        // Center the stage after resizing
        stage.centerOnScreen();
    }

    // Additional login validation methods
    public boolean validateUsername(String username) {
        return username != null && !username.trim().isEmpty();
    }

    public boolean validatePassword(String password) {
        return password != null && !password.trim().isEmpty();
    }

    public boolean isValidLogin(String username, String password) {
        // This is a placeholder - in a real application, you would validate against a database
        return validateUsername(username) && validatePassword(password);
    }
}