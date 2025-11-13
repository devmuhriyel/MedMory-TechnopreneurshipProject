package Controller.Doctor;

import MVP.Login; // Assuming this is where your resources are located
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsController {

    // Sidebar Buttons
    @FXML private Button accountButton;
    @FXML private Button casesButton;
    @FXML private Button calendarButton;
    @FXML private Button notificationsButton;
    @FXML private Button settingsButton;

    // Settings Checkboxes
    @FXML private CheckBox emailNotifCheck;
    @FXML private CheckBox smsNotifCheck;
    @FXML private CheckBox pushNotifCheck;
    @FXML private CheckBox darkModeCheck;
    @FXML private CheckBox autoUpdateCheck;

    // Action Buttons
    @FXML private Button saveButton;
    @FXML private Button logoutButton;

    /**
     * Initializes the controller.
     * You can set default checkbox states here if needed.
     */
    @FXML
    public void initialize() {
        System.out.println("Settings controller initialized.");

        // Example default states
        emailNotifCheck.setSelected(true);
        pushNotifCheck.setSelected(true);
        autoUpdateCheck.setSelected(true);
    }

    /**
     * Handles sidebar navigation button clicks.
     */
    @FXML
    private void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String targetFxml = "";

        if (clickedButton == accountButton) targetFxml = "DoctorAccount.fxml";
        else if (clickedButton == casesButton) targetFxml = "History.fxml";
        else if (clickedButton == calendarButton) targetFxml = "DoctorCalendar.fxml";
        else if (clickedButton == notificationsButton) targetFxml = "Chat.fxml";
        else if (clickedButton == settingsButton) targetFxml = "Settings.fxml";
        else {
            System.out.println("Unknown target: " + clickedButton);
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource(targetFxml));
            Scene scene = new Scene(fxmlLoader.load(), 1280, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles saving the settings changes.
     */
    @FXML
    private void handleSaveChanges(ActionEvent event) {
        System.out.println("Saving doctor settings...");

        boolean emailNotif = emailNotifCheck.isSelected();
        boolean smsNotif = smsNotifCheck.isSelected();
        boolean pushNotif = pushNotifCheck.isSelected();
        boolean darkMode = darkModeCheck.isSelected();
        boolean autoUpdate = autoUpdateCheck.isSelected();

        System.out.println("Email Notifications: " + emailNotif);
        System.out.println("SMS Notifications: " + smsNotif);
        System.out.println("Push Notifications: " + pushNotif);
        System.out.println("Dark Mode: " + darkMode);
        System.out.println("Auto Updates: " + autoUpdate);

        // TODO: Save these settings to a database or file
    }

    /**
     * Handles logout action.
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logging out...");

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource("Login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1280, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
