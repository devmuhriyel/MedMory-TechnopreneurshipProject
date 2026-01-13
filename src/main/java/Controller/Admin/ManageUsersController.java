package Controller.Admin;

import MVP.Login;
import CommonModel.SessionManager;
import MVP.Admin.ManageUsers;
import MVP.Admin.Settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ManageUsersController {
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();

        String targetFxml = switch (id) {
            case "settingsButton" -> "Settings.fxml";
            case "logOutButton" -> "Login.fxml";
            default -> throw new IllegalStateException("Unexpected value: " + id);
        };

        if (targetFxml == null) {
            System.err.println("Unknown navigation target: " + id);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource(targetFxml));
            Scene scene = new Scene(loader.load(), 1280, 768);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Failed to load: " + targetFxml);
            e.printStackTrace();
        }


    }

    public void handleLogOut(ActionEvent actionEvent) {
        System.out.println("Logging out...");

        SessionManager.clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("Login.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 768);

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Failed to load Login.fxml");
            e.printStackTrace();
        }
    }
}
