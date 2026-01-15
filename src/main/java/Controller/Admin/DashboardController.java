package Controller.Admin;

import CommonModel.SessionManager;
import MVP.Login;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();

        String targetFxml = switch (id) {
            case "homeButton" -> "AdminDashboard.fxml";
            case "accountButton" -> "ManageUsers.fxml";
            case "settingsButton" -> "Settings.fxml";
            default -> throw new IllegalStateException("Unexpected value: " + id);
        };

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

    public void handleLogOut(ActionEvent actionEvent) {
        SessionManager.clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("Login.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 768);

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
