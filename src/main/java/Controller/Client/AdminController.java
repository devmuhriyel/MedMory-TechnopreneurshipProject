package Controller.Client;
import MVP.Login;
import CommonModel.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
public class AdminController {
    @FXML private Button logoutButton;

    public void handleLogout(ActionEvent event) {
        System.out.println("Logging out...");

        SessionManager.clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("Login.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 768);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Failed to load Login.fxml");
            e.printStackTrace();
        }
    }
}
