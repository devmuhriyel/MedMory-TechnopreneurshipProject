package Controller.Doctor;

import MVP.Login; // For resource loading
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

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
                targetFxml = "Settings.fxml";
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

    /**
     * Handles when a case date button is clicked.
     */
    @FXML
    public void handleDateClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        System.out.println("Case date clicked: " + clickedButton.getText());

        // TODO: Implement showing details of this case
    }

    /**
     * Example method to dynamically add case items.
     * You can call this after loading data from database.
     */
    public void addCaseItem(String treatment, String patient, String diagnosis, String completionDate) {
        // TODO: Load FXML for each case item or dynamically create AnchorPane
        System.out.println("Adding case: " + treatment + " | " + patient + " | " + diagnosis + " | " + completionDate);
    }
}
