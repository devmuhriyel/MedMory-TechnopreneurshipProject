package Controller.Admin;

import DAO.DatabaseConnection;
import DAO.*;
import DAO.RecordDAO;
import CommonModel.AdminModel;
import CommonModel.SessionManager;
import CommonModel.UserRow;
import MVP.Doctor.DoctorModel;
import MVP.Login;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TextField;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;

public class ManageUsersController implements Initializable {

    @FXML private TableView<UserRow> userTable;
    @FXML private TableColumn<UserRow, String> idCol;
    @FXML private TableColumn<UserRow, String> nameCol;
    @FXML private TableColumn<UserRow, String> roleCol;
    @FXML private TableColumn<UserRow, String> statusCol;

    @FXML
    private TextField searchField;

    private ObservableList<UserRow> masterData = FXCollections.observableArrayList();


    private RecordDAO recordDAO;
    private Connection conn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Connection conn = DatabaseConnection.getConnection();
        recordDAO = new RecordDAO(conn);

        setupColumns();
        loadAllUsers();
    }

    private void setupColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }


    private void loadAllUsers() {

        masterData.clear();

        // Patients
        for (AdminModel p : recordDAO.getAllPatients()) {
            masterData.add(new UserRow(
                    p.getId(),
                    p.getName(),
                    "PATIENT",
                    "Active"
            ));
        }

        // Doctors
        for (DoctorModel d : recordDAO.getAllDoctors()) {
            masterData.add(new UserRow(
                    d.id,
                    d.name,
                    "DOCTOR",
                    "Active"
            ));
        }

        setupSearchFilter();
    }


    private void setupSearchFilter() {

        FilteredList<UserRow> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String lowerCaseFilter = newValue.toLowerCase();

            filteredData.setPredicate(user -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                if (String.valueOf(user.getId()).contains(lowerCaseFilter)) {
                    return true;
                }
                if (user.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (user.getRole().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (user.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
        });

        SortedList<UserRow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());

        userTable.setItems(sortedData);
    }


    // ================= NAVIGATION =================

    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();

        String targetFxml = switch (id) {
            case "homeButton" -> "AdminDashboard.fxml";
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

    @FXML
    private void handleDeleteUser() {

        UserRow selected = userTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            System.out.println("No Selection" + "Please select a user to delete.");
            return;
        }

        boolean success;

        if (selected.getRole().equals("PATIENT")) {
            success = recordDAO.deletePatientById(selected.getId());
        } else {
            success = recordDAO.deleteDoctorById(selected.getId());
        }

        if (success) {
            loadAllUsers();
            System.out.println("Success" + "User deleted successfully.");
        } else {
            System.out.println("Error" + "Failed to delete user.");
        }
    }
    @FXML
    private void handleCreateButton() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    Login.class.getResource("AddUser.fxml")
            );

            Parent root = loader.load();


            Stage stage = (Stage) userTable.getScene().getWindow();


            stage.setScene(new Scene(root));
            stage.setTitle("Edit User");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditUser() {

        UserRow selected = userTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            System.out.println("Please select a user to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    Login.class.getResource("EditUser.fxml")
            );

            Parent root = loader.load();


            EditUserController controller = loader.getController();
            controller.setUser(selected);


            Stage stage = (Stage) userTable.getScene().getWindow();


            stage.setScene(new Scene(root));
            stage.setTitle("Edit User");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
