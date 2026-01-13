package Controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SignupController {

    @FXML private TextField firstNameField, lastNameField, emailField, phoneField, birthdayField, locationField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private CheckBox consentCheck1, consentCheck2, consentCheck3;
    @FXML private Button createAccountButton, cancelButton;
    @FXML private AnchorPane mainCard;
    @FXML private GridPane formGrid;
    @FXML private VBox consentContainer;
    @FXML private HBox logoContainer;

    @FXML
    private void initialize() {
        // Ensure buttons are visible
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

        // Restore entrance animations
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
            new ParallelTransition(fade, slide).play();
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
                ParallelTransition logoPart = new ParallelTransition(fade, slide);
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
                ParallelTransition fieldAnimation = new ParallelTransition(fade, slide);
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
        new ParallelTransition(fade, slide).play();
    }

    // =========================================================
    // UPDATED LOGIC FOR DATABASE AND NAVIGATION
    // =========================================================
    @FXML
    private void handleCreateAccount() {
        if (!validateForm()) {
            showErrorAnimation();
            return;
        }

        showLoadingAnimation();

        // Prepare data
        String fullName = firstNameField.getText().trim() + " " + lastNameField.getText().trim();
        String pass = passwordField.getText();

        // Save to Database
        boolean isSaved = LoginService.registerUser(fullName, pass);

        Timeline createProcess = new Timeline(new KeyFrame(Duration.millis(2000), event -> {
            createAccountButton.getStyleClass().remove("loading");

            if (isSaved) {
                showSuccessMessage("Account created successfully!");

                // LEAD BACK TO LOGIN
                PauseTransition delay = new PauseTransition(Duration.millis(1500));
                delay.setOnFinished(e -> {
                    try { navigateToLogin(); } catch (Exception ex) { ex.printStackTrace(); }
                });
                delay.play();
            } else {
                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setContentText("Database error. Could not save account.");
                    a.show();
                });
            }
        }));
        createProcess.play();
    }

    private void navigateToLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MVP/Login.fxml"));
        Parent loginPage = loader.load();
        Stage stage = (Stage) createAccountButton.getScene().getWindow();
        stage.setWidth(800);
        stage.setHeight(600);
        stage.getScene().setRoot(loginPage);
        stage.centerOnScreen();
    }

    @FXML
    private void handleCancel() {
        try { navigateToLogin(); } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean validateForm() {
        boolean isValid = true;
        if (firstNameField.getText().trim().isEmpty()) { highlightField(firstNameField); isValid = false; }
        if (lastNameField.getText().trim().isEmpty()) { highlightField(lastNameField); isValid = false; }
        if (passwordField.getText().isEmpty()) { highlightField(passwordField); isValid = false; }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            highlightField(passwordField); highlightField(confirmPasswordField); isValid = false;
        }
        if (!consentCheck1.isSelected() || !consentCheck2.isSelected() || !consentCheck3.isSelected()) {
            animateConsentCheckboxes(); isValid = false;
        }
        return isValid;
    }

    private void highlightField(Control field) {
        field.getStyleClass().add("error");
        Timeline removeError = new Timeline(new KeyFrame(Duration.millis(3000), e -> field.getStyleClass().remove("error")));
        removeError.play();
    }

    private void showLoadingAnimation() {
        createAccountButton.getStyleClass().add("loading");
        createAccountButton.setText("Registering...");
    }

    private void showSuccessMessage(String message) {
        createAccountButton.setText("Success!");
        createAccountButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(message);
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
                pulse.setToX(1.2); pulse.setToY(1.2); pulse.setCycleCount(2); pulse.setAutoReverse(true);
                pulse.play();
            }
        }
    }
}