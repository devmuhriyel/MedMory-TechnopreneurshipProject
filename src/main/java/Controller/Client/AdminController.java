package Controller.Client;


import DAO.DatabaseConnection;
import DAO.RecordDAO;
import CommonModel.AdminModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private Label name1, role1, email1, status1;
    @FXML private Label name2, role2, email2, status2;
    @FXML private Label name3, role3, email3, status3;

    private RecordDAO recordDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Connection conn = DatabaseConnection.getConnection();
        recordDAO = new RecordDAO(conn);

        loadUsersFromDatabase();
    }

    private void loadUsersFromDatabase() {

        List<AdminModel> users = recordDAO.getAllUsers();

        if (users.size() > 0) setRow1(users.get(0));
        if (users.size() > 1) setRow2(users.get(1));
        if (users.size() > 2) setRow3(users.get(2));
    }

    private void setRow1(AdminModel user) {
        name1.setText(user.getName());
        role1.setText("User");      // or fetch from DB later
    }

    private void setRow2(AdminModel user) {
        name2.setText(user.getName());
        role2.setText("User");
    }

    private void setRow3(AdminModel user) {
        name3.setText(user.getName());
        role3.setText("User");
    }
}

