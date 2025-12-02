package Controller.Client;

import MVP.SessionManager;
import MVP.Client.ClientService;
import MVP.Client.ClientModel;
import MVP.Client.ClientMedicalRecord;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ClientHomepageController {

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;

    // fields for medical record if needed:
    @FXML private TextArea subjArea;
    @FXML private TextArea ccArea;
    @FXML private TextArea hpiArea;

    @FXML
    public void initialize() {
        int userID = SessionManager.getUserID();

        if (userID <= 0) {
            System.out.println("⚠ No client logged in.");
            return;
        }

        // Load client info
        ClientModel client = ClientService.getClientInfo(userID);
        if (client != null) {
            nameLabel.setText(client.name);
            emailLabel.setText(client.email);
            phoneLabel.setText(client.phone);
        }


    }
}
