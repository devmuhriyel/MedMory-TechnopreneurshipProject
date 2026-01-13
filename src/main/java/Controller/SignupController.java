package Controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SignupController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField birthdayField;

    @FXML
    private TextField locationField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private CheckBox consentCheck1;

    @FXML
    private CheckBox consentCheck2;

    @FXML
    private CheckBox consentCheck3;

    @FXML
    private Button createAccountButton;

    @FXML
    private Button cancelButton;

    @FXML
    private AnchorPane mainCard;

    @FXML
    private GridPane formGrid;

    @FXML
    private VBox consentContainer;

    @FXML
    private HBox logoContainer;

    @FXML
    private void initialize() {
        System.out.println("SignupController initialized");
        System.out.println("cancelButton: " + cancelButton);
        System.out.println("createAccountButton: " + createAccountButton);

        // Make sure buttons are always visible
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

        // Removed: Platform.runLater(this::startPageAnimations);
        // This prevents animations from playing every time the page loads
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

        // Staggered animations for different sections
        Timeline pageAnimation = new Timeline();
        pageAnimation.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(1000), event -> animateWelcomeSection()),
                new KeyFrame(Duration.millis(1200), event -> animateLogo()),
                new KeyFrame(Duration.millis(1400), event -> animateFormFields()),
                new KeyFrame(Duration.millis(1600), event -> animateConsentSection())
        );
        pageAnimation.play();
    }

    private void animateWelcomeSection() {
        if (mainCard == null || mainCard.getChildren() == null) return;

        // Animate welcome text
        VBox welcomeSection = (VBox) mainCard.getChildren().stream()
                .filter(node -> node instanceof VBox && node.getStyleClass().contains("welcome-section"))
                .findFirst().orElse(null);

        if (welcomeSection != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(500), welcomeSection);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);

            TranslateTransition slide = new TranslateTransition(Duration.millis(500), welcomeSection);
            slide.setFromX(-20);
            slide.setToX(0);

            ParallelTransition welcomeAnimation = new ParallelTransition(fade, slide);
            welcomeAnimation.play();
        }
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
        if (formGrid != null && formGrid.getChildren() != null) {
            for (int i = 0; i < formGrid.getChildren().size(); i++) {
                var child = formGrid.getChildren().get(i);

                FadeTransition fade = new FadeTransition(Duration.millis(300), child);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);

                TranslateTransition slide = new TranslateTransition(Duration.millis(300), child);
                slide.setFromY(20);
                slide.setToY(0);

                ScaleTransition scale = new ScaleTransition(Duration.millis(300), child);
                scale.setFromX(0.95);
                scale.setFromY(0.95);
                scale.setToX(1.0);
                scale.setToY(1.0);

                ParallelTransition fieldAnimation = new ParallelTransition(fade, slide, scale);
                fieldAnimation.setDelay(Duration.millis(i * 50));
                fieldAnimation.play();
            }
        }
    }

    private void animateConsentSection() {
        if (consentContainer == null) return;

        FadeTransition fade = new FadeTransition(Duration.millis(500), consentContainer);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(500), consentContainer);
        slide.setFromY(30);
        slide.setToY(0);

        ParallelTransition consentAnimation = new ParallelTransition(fade, slide);
        consentAnimation.play();

        // Animate checkboxes with delay
        Timeline checkboxAnimation = new Timeline();
        checkboxAnimation.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(200), event -> animateCheckbox(consentCheck1)),
                new KeyFrame(Duration.millis(400), event -> animateCheckbox(consentCheck2)),
                new KeyFrame(Duration.millis(600), event -> animateCheckbox(consentCheck3))
        );
        checkboxAnimation.play();
    }

    private void animateCheckbox(CheckBox checkbox) {
        if (checkbox == null) return;

        FadeTransition fade = new FadeTransition(Duration.millis(300), checkbox);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), checkbox);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.0);
        scale.setToY(1.0);

        ParallelTransition checkboxAnimation = new ParallelTransition(fade, scale);
        checkboxAnimation.play();
    }

    @FXML
    private void handleCreateAccount() {
        // Validate form
        if (!validateForm()) {
            showErrorAnimation();
            return;
        }

        // Show loading animation
        showLoadingAnimation();

        // Simulate account creation process
        Timeline createProcess = new Timeline();
        createProcess.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(3000), event -> {
                    // Reset button
                    createAccountButton.getStyleClass().remove("loading");
                    createAccountButton.setText("Create Account");

                    // Show success message
                    showSuccessMessage("Account created successfully!");
                })
        );
        createProcess.play();
    }

    @FXML
    private void handleCancel() {
        if (cancelButton == null) return;

        // Animate button click
        ScaleTransition clickAnimation = new ScaleTransition(Duration.millis(100), cancelButton);
        clickAnimation.setFromX(1.0);
        clickAnimation.setFromY(1.0);
        clickAnimation.setToX(0.95);
        clickAnimation.setToY(0.95);

        ScaleTransition releaseAnimation = new ScaleTransition(Duration.millis(100), cancelButton);
        releaseAnimation.setFromX(0.95);
        releaseAnimation.setFromY(0.95);
        releaseAnimation.setToX(1.0);
        releaseAnimation.setToY(1.0);

        SequentialTransition clickSequence = new SequentialTransition(clickAnimation, releaseAnimation);
        clickSequence.setOnFinished(event -> {
            // Navigate back to login page
            try {
                navigateToLogin();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        clickSequence.play();
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Check required fields
        if (firstNameField.getText().trim().isEmpty()) {
            highlightField(firstNameField);
            isValid = false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            highlightField(lastNameField);
            isValid = false;
        }
        if (emailField.getText().trim().isEmpty()) {
            highlightField(emailField);
            isValid = false;
        }
        if (passwordField.getText().trim().isEmpty()) {
            highlightField(passwordField);
            isValid = false;
        }
        if (confirmPasswordField.getText().trim().isEmpty()) {
            highlightField(confirmPasswordField);
            isValid = false;
        }

        // Check password match
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            highlightField(passwordField);
            highlightField(confirmPasswordField);
            isValid = false;
        }

        // Check consent checkboxes
        if (!consentCheck1.isSelected() || !consentCheck2.isSelected() || !consentCheck3.isSelected()) {
            animateConsentCheckboxes();
            isValid = false;
        }

        return isValid;
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

    private void animateConsentCheckboxes() {
        // Pulse animation for unchecked consent checkboxes
        for (CheckBox checkbox : new CheckBox[]{consentCheck1, consentCheck2, consentCheck3}) {
            if (checkbox != null && !checkbox.isSelected()) {
                ScaleTransition pulse1 = new ScaleTransition(Duration.millis(200), checkbox);
                pulse1.setFromX(1.0);
                pulse1.setFromY(1.0);
                pulse1.setToX(1.2);
                pulse1.setToY(1.2);

                ScaleTransition pulse2 = new ScaleTransition(Duration.millis(200), checkbox);
                pulse2.setFromX(1.2);
                pulse2.setFromY(1.2);
                pulse2.setToX(1.0);
                pulse2.setToY(1.0);

                SequentialTransition pulseSequence = new SequentialTransition(pulse1, pulse2);
                pulseSequence.setCycleCount(2);
                pulseSequence.play();
            }
        }
    }

    private void showLoadingAnimation() {
        if (createAccountButton == null) return;

        createAccountButton.getStyleClass().add("loading");
        createAccountButton.setText("Creating Account...");

        // Pulse animation for loading
        ScaleTransition pulse1 = new ScaleTransition(Duration.millis(500), createAccountButton);
        pulse1.setFromX(1.0);
        pulse1.setFromY(1.0);
        pulse1.setToX(1.05);
        pulse1.setToY(1.05);

        ScaleTransition pulse2 = new ScaleTransition(Duration.millis(500), createAccountButton);
        pulse2.setFromX(1.05);
        pulse2.setFromY(1.05);
        pulse2.setToX(1.0);
        pulse2.setToY(1.0);

        SequentialTransition pulseSequence = new SequentialTransition(pulse1, pulse2);
        pulseSequence.setCycleCount(Animation.INDEFINITE);
        pulseSequence.play();
    }

    private void showSuccessMessage(String message) {
        // Create success notification
        System.out.println(message);

        if (createAccountButton == null) return;

        // Success animation
        ScaleTransition successAnimation = new ScaleTransition(Duration.millis(200), createAccountButton);
        successAnimation.setFromX(1.0);
        successAnimation.setFromY(1.0);
        successAnimation.setToX(1.1);
        successAnimation.setToY(1.1);

        ScaleTransition successReturn = new ScaleTransition(Duration.millis(200), createAccountButton);
        successReturn.setFromX(1.1);
        successReturn.setFromY(1.1);
        successReturn.setToX(1.0);
        successReturn.setToY(1.0);

        SequentialTransition successSequence = new SequentialTransition(successAnimation, successReturn);
        successSequence.play();
    }

    private void navigateToLogin() throws Exception {
        // Load login page
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Login.fxml"));
        AnchorPane loginPage = loader.load();

        // Get the stage first
        javafx.stage.Stage stage = (javafx.stage.Stage) cancelButton.getScene().getWindow();

        // Resize stage BEFORE changing the scene content
        stage.setWidth(800);
        stage.setHeight(600);

        // Replace current scene content
        AnchorPane currentRoot = (AnchorPane) cancelButton.getScene().getRoot();
        currentRoot.getChildren().clear();
        currentRoot.getChildren().add(loginPage);

        // Ensure the login page fills the parent
        AnchorPane.setTopAnchor(loginPage, 0.0);
        AnchorPane.setBottomAnchor(loginPage, 0.0);
        AnchorPane.setLeftAnchor(loginPage, 0.0);
        AnchorPane.setRightAnchor(loginPage, 0.0);

        // Center the stage after resizing
        stage.centerOnScreen();
    }

    // Additional signup validation methods
    public boolean validateEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    public boolean validatePassword(String password) {
        return password != null && password.length() >= 6;
    }

    public boolean validatePhoneNumber(String phone) {
        return phone != null && phone.matches("\\d{10,15}");
    }

    public boolean validateAllConsentCheckboxes() {
        return consentCheck1.isSelected() && consentCheck2.isSelected() && consentCheck3.isSelected();
    }
}