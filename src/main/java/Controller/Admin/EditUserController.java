package Controller.Admin;

import CommonModel.SessionManager;
import CommonModel.UserRow;
import DAO.DatabaseConnection;
import DAO.RecordDAO;
import MVP.Login;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EditUserController implements Initializable {

    @FXML private TextField userNameField;
    @FXML private TextField iDField;
    @FXML private TextField userPasswordField;

    private UserRow user;
    private RecordDAO recordDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        recordDAO = new RecordDAO(DatabaseConnection.getConnection());
    }

    public void setUser(UserRow user) {
        this.user = user;

        userNameField.setText(user.getName());
        iDField.setText(String.valueOf(user.getId()));
        iDField.setDisable(true);
    }


    @FXML
    private void handleSave() {

        System.out.println("=== EDIT USER DEBUG START ===");

        if (user == null) {
            System.out.println("❌ User object is NULL");
            return;
        }

        System.out.println("User ID   : " + user.getId());
        System.out.println("User Role : " + user.getRole());
        System.out.println("Name Input: '" + userNameField.getText() + "'");

        boolean success = false;

        try {
            if ("PATIENT".equals(user.getRole())) {

                System.out.println("Calling updatePatient(...)");

                success = recordDAO.updatePatient(
                        user.getId(),
                        userNameField.getText()
                );

            } else {

                System.out.println("Calling updateDoctor(...)");
                System.out.println("Password Input: '" + userPasswordField.getText() + "'");

                success = recordDAO.updateDoctor(
                        user.getId(),
                        userNameField.getText()
                );
            }

            System.out.println("DAO returned: " + success);

        } catch (Exception e) {
            System.out.println("❌ Exception during update:");
            e.printStackTrace();
        }

        if (success) {
            System.out.println("✅ Update SUCCESS");
        } else {
            System.out.println("❌ Update FAILED");
        }

        System.out.println("=== EDIT USER DEBUG END ===");
    }



    private void closeWindow() {
        Stage stage = (Stage) userNameField.getScene().getWindow();
        stage.close();
    }

    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();

        String targetFxml = switch (id) {
            case "settingsButton" -> "Settings.fxml";
            case "backButton" -> "ManageUsers.fxml";
            default -> null;
        };

        if (targetFxml == null) return;

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

}
