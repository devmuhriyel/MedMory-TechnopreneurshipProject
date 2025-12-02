package Controller.Doctor;

import CommonModel.DoctorCase;
import DAO.DoctorService;
import MVP.Login; // For resource loading
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class HistoryController {

    // Sidebar buttons
    @FXML private Button accountButton;
    @FXML private Button casesButton;
    @FXML private Button calendarButton;
    @FXML private Button notificationsButton;
    @FXML private Button settingsButton;

    // VBox that contains the case items
    @FXML private VBox casesListVBox;

    /**
     * Handles all sidebar navigation button clicks.
     */
    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        String targetFxml = "";

        switch (buttonId) {
            case "accountButton":
                targetFxml = "DoctorAccount.fxml";
                break;
            case "casesButton":
                targetFxml = "History.fxml"; // Stay in current view
                break;
            case "calendarButton":
                targetFxml = "Calendar.fxml";
                break;
            case "notificationsButton":
                targetFxml = "Chat.fxml";
                break;
            case "settingsButton":
                targetFxml = "DoctorSettings.fxml";
                break;
            default:
                System.out.println("Unknown navigation target: " + buttonId);
                return;
        }

        System.out.println("Navigating to: " + targetFxml);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource(targetFxml));
            Scene scene = new Scene(fxmlLoader.load(), 1280, 768); // Windowed size
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load scene: " + targetFxml);
        }
    }

    @FXML
    public void initialize() {
        int doctorID = 1; // Dummy until login is set

        System.out.println("Retrieving cases for doctor " + doctorID);
        List<DoctorCase> cases = DoctorService.getCasesHandledByDoctor(doctorID);

        if (cases.isEmpty()) {
            System.out.println("No cases found for this doctor.");
            // Display a message in the UI
            Label noCasesLabel = new Label("No cases found.");
            noCasesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");
            casesListVBox.getChildren().add(noCasesLabel);
            return;
        }

        // Dynamically populate the UI with cases
        for (DoctorCase c : cases) {
            System.out.println(
                    "Case #" + c.caseID +
                            " | Patient: " + c.patientName +
                            " | Diagnosis: " + c.diagnosis +
                            " | Treatment: " + c.treatment +
                            " | Date: " + c.dateCompleted
            );

            // Add case item to the UI
            addCaseItem(c);
        }
    }

    /**
     * Dynamically creates and adds a case item to the UI.
     */
    private void addCaseItem(DoctorCase doctorCase) {
        // Create the main container for the case item
        AnchorPane casePane = new AnchorPane();
        casePane.setPrefHeight(80);
        casePane.setMaxWidth(Double.MAX_VALUE);
        casePane.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; " +
                "-fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        VBox.setMargin(casePane, new Insets(0, 0, 10, 0));

        // Create HBox for the main content
        HBox contentBox = new HBox(20);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        contentBox.setPadding(new Insets(15));
        AnchorPane.setTopAnchor(contentBox, 0.0);
        AnchorPane.setBottomAnchor(contentBox, 0.0);
        AnchorPane.setLeftAnchor(contentBox, 0.0);
        AnchorPane.setRightAnchor(contentBox, 0.0);

        // Treatment label
        Label treatmentLabel = new Label(doctorCase.treatment);
        treatmentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        treatmentLabel.setPrefWidth(200);

        // Patient label
        Label patientLabel = new Label("Patient: " + doctorCase.patientName);
        patientLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
        patientLabel.setPrefWidth(200);

        // Diagnosis label
        Label diagnosisLabel = new Label("Diagnosis: " + doctorCase.diagnosis);
        diagnosisLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
        diagnosisLabel.setPrefWidth(250);

        // Date button
        Button dateButton = new Button(doctorCase.dateCompleted);
        dateButton.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 8 15 8 15; " +
                "-fx-border-radius: 3; -fx-background-radius: 3;");
        dateButton.setOnAction(event -> handleCaseClick(doctorCase));

        // Add all elements to the content box
        contentBox.getChildren().addAll(treatmentLabel, patientLabel, diagnosisLabel, dateButton);

        // Add content box to the pane
        casePane.getChildren().add(contentBox);

        // Add the complete case pane to the VBox
        casesListVBox.getChildren().add(casePane);
    }

    /**
     * Handles when a case date button is clicked.
     */
    @FXML
    public void handleCaseClick(DoctorCase doctorCase) {
        System.out.println("Case clicked: #" + doctorCase.caseID);
        System.out.println("Patient: " + doctorCase.patientName);
        System.out.println("Diagnosis: " + doctorCase.diagnosis);
        System.out.println("Treatment: " + doctorCase.treatment);
        System.out.println("Date: " + doctorCase.dateCompleted);

        // TODO: Implement showing details of this case
    }
}