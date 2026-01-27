package Controller.Admin;

import MVP.SessionManager;
import DAO.DatabaseConnection;
import DAO.RecordDAO;
import MVP.Login;
import CommonModel.*;

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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.Optional;
import java.util.ResourceBundle;

public class ManageUsersController implements Initializable {

    @FXML private TableView<UserRow> userTable;
    @FXML private TableColumn<UserRow, Integer> idCol;
    @FXML private TableColumn<UserRow, String> nameCol;
    @FXML private TableColumn<UserRow, String> emailCol;
    @FXML private TableColumn<UserRow, String> phoneCol;
    @FXML private TableColumn<UserRow, String> roleCol;
    @FXML private TableColumn<UserRow, String> statusCol;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;
    @FXML private Label totalUsersLabel;
    @FXML private Label doctorsCountLabel;
    @FXML private Label patientsCountLabel;

    private ObservableList<UserRow> masterData = FXCollections.observableArrayList();
    private RecordDAO recordDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[DEBUG] ManageUsersController initialize() called");

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("[ERROR] Database connection is NULL");
            } else {
                System.out.println("[DEBUG] Database connection established");
            }

            recordDAO = new RecordDAO(conn);

            setupColumns();
            setupRoleFilter();
            loadAllUsers();
            updateStatistics();

            System.out.println("[DEBUG] ManageUsersController initialized successfully");

        } catch (Exception e) {
            System.err.println("[ERROR] Failed during initialize()");
            e.printStackTrace();
        }
    }


    /**
     * Setup table columns
     */
    private void setupColumns() {
        System.out.println("[DEBUG] Setting up table columns");

        try {
            if (idCol == null || nameCol == null) {
                System.err.println("[ERROR] One or more TableColumns are NULL (FXML mismatch?)");
                return;
            }

            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
            phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
            roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

            System.out.println("[DEBUG] Table columns bound successfully");

        } catch (Exception e) {
            System.err.println("[ERROR] setupColumns() failed");
            e.printStackTrace();
        }
    }


    /**
     * Setup role filter dropdown
     */
    private void setupRoleFilter() {
        if (roleFilterCombo != null) {
            roleFilterCombo.getItems().addAll("All Users", "Doctors", "Patients", "Admins");
            roleFilterCombo.setValue("All Users");
            roleFilterCombo.setOnAction(e -> setupSearchFilter());
        }
    }

    /**
     * Load all users from database
     */
    private void loadAllUsers() {
        System.out.println("[DEBUG] Loading all users");
        masterData.clear();

        try {
            System.out.println("[DEBUG] Fetching patients...");
            for (PatientModel p : recordDAO.getAllPatients()) {
                masterData.add(new UserRow(
                        p.getPatientId(),
                        p.getName(),
                        "PATIENT",
                        "Active",
                        p.getEmail(),
                        p.getPhoneNumber(),
                        "",
                        "",
                        p.getBirthday()
                ));
            }
            System.out.println("[DEBUG] Patients loaded");

            System.out.println("[DEBUG] Fetching doctors...");
            for (DoctorModel d : recordDAO.getAllDoctors()) {
                masterData.add(new UserRow(
                        d.id,
                        d.name,
                        "DOCTOR",
                        "Active",
                        d.email,
                        d.phone,
                        d.address,
                        "",
                        d.birthday
                ));
            }
            System.out.println("[DEBUG] Doctors loaded");

            setupSearchFilter();
            updateStatistics();

            System.out.println("[DEBUG] loadAllUsers() completed successfully");

        } catch (Exception e) {
            System.err.println("[ERROR] loadAllUsers() failed");
            e.printStackTrace();
        }
    }

    /**
     * Setup search and filter functionality
     */
    private void setupSearchFilter() {
        System.out.println("[DEBUG] Setting up search & filter");

        try {
            FilteredList<UserRow> filteredData = new FilteredList<>(masterData, p -> true);

            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                updateFilter(filteredData);
            });

            if (roleFilterCombo != null) {
                roleFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                    updateFilter(filteredData);
                });
            } else {
                System.err.println("[ERROR] roleFilterCombo is NULL");
            }

            SortedList<UserRow> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(userTable.comparatorProperty());
            userTable.setItems(sortedData);

            System.out.println("[DEBUG] Search & filter setup complete");

        } catch (Exception e) {
            System.err.println("[ERROR] setupSearchFilter() failed");
            e.printStackTrace();
        }
    }


    /**
     * Update filter predicate based on search and role filter
     */
    private void updateFilter(FilteredList<UserRow> filteredData) {
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String roleFilter = roleFilterCombo != null ? roleFilterCombo.getValue() : "All Users";

        filteredData.setPredicate(user -> {
            // Role filter
            boolean roleMatch = switch (roleFilter) {
                case "Doctors" -> user.getRole().equals("DOCTOR");
                case "Patients" -> user.getRole().equals("PATIENT");
                case "Admins" -> user.getRole().equals("ADMIN");
                default -> true; // "All Users"
            };

            if (!roleMatch) return false;

            // Search filter
            if (searchText.isEmpty()) return true;

            return String.valueOf(user.getId()).contains(searchText) ||
                    user.getName().toLowerCase().contains(searchText) ||
                    user.getEmail().toLowerCase().contains(searchText) ||
                    user.getPhone().toLowerCase().contains(searchText) ||
                    user.getRole().toLowerCase().contains(searchText) ||
                    user.getStatus().toLowerCase().contains(searchText);
        });
    }

    /**
     * Update statistics labels
     */
    private void updateStatistics() {
        long totalUsers = masterData.size();
        long doctors = masterData.stream().filter(u -> u.getRole().equals("DOCTOR")).count();
        long patients = masterData.stream().filter(u -> u.getRole().equals("PATIENT")).count();

        if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(totalUsers));
        if (doctorsCountLabel != null) doctorsCountLabel.setText(String.valueOf(doctors));
        if (patientsCountLabel != null) patientsCountLabel.setText(String.valueOf(patients));
    }

    // ================= USER ACTIONS =================

    /**
     * Handle delete user action
     */
    @FXML
    private void handleDeleteUser() {
        UserRow selected = userTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to delete.");
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete User: " + selected.getName());
        confirmAlert.setContentText("Are you sure you want to delete this user?\n\n" +
                "ID: " + selected.getId() + "\n" +
                "Name: " + selected.getName() + "\n" +
                "Role: " + selected.getRole() + "\n\n" +
                "This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        boolean success;

        if (selected.getRole().equals("PATIENT")) {
            success = recordDAO.deletePatientById(selected.getId());
        } else if (selected.getRole().equals("DOCTOR")) {
            success = recordDAO.deleteDoctorById(selected.getId());
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot delete admin users from this interface.");
            return;
        }

        if (success) {
            loadAllUsers();
            showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user. The user may have associated records.");
        }
    }

    /**
     * Handle create user action
     */
    @FXML
    private void handleCreateButton() {
        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("AddUser.fxml"));
            Parent root = loader.load();

            // Pass controller reference if needed
            AddUserController controller = loader.getController();
            controller.setManageUsersController(this);

            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Add New User");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load Add User screen.");
        }
    }

    /**
     * Handle edit user action
     */
    @FXML
    private void handleEditUser() {
        UserRow selected = userTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource("EditUser.fxml"));
            Parent root = loader.load();

            EditUserController controller = loader.getController();
            controller.setUser(selected);
            controller.setManageUsersController(this);

            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit User - " + selected.getName());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load Edit User screen.");
        }
    }

    /**
     * View user details
     */
    @FXML
    private void handleViewUser() {
        UserRow selected = userTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to view.");
            return;
        }

        // Create a detailed view dialog
        Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
        detailsAlert.setTitle("User Details");
        detailsAlert.setHeaderText(selected.getName() + " - " + selected.getRole());

        String details = String.format(
                "User ID: %d\n" +
                        "Name: %s\n" +
                        "Email: %s\n" +
                        "Phone: %s\n" +
                        "Role: %s\n" +
                        "Status: %s\n" +
                        "Birthday: %s\n" +
                        "Address: %s",
                selected.getId(),
                selected.getName(),
                selected.getEmail(),
                selected.getPhone(),
                selected.getRole(),
                selected.getStatus(),
                selected.getBirthday() != null ? selected.getBirthday() : "Not set",
                selected.getAddress() != null && !selected.getAddress().isEmpty() ? selected.getAddress() : "Not set"
        );

        detailsAlert.setContentText(details);
        detailsAlert.showAndWait();
    }

    /**
     * Refresh user list
     */
    @FXML
    private void handleRefresh() {
        loadAllUsers();
        showAlert(Alert.AlertType.INFORMATION, "Refreshed", "User list has been refreshed.");
    }

    // ================= NAVIGATION =================

    /**
     * Handle navigation buttons
     */
    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String id = clickedButton.getId();

        System.out.println("[DEBUG] Navigation button clicked: " + id);

        String targetFxml = switch (id) {
            case "homeButton", "dashboardButton" -> "AdminDashboard.fxml";
            case "usersButton" -> "ManageUsers.fxml";
            default -> {
                System.err.println("[ERROR] Unknown navigation button: " + id);
                yield null;
            }
        };

        if (targetFxml != null) {
            navigateTo(targetFxml, event);
        }
    }


    /**
     * Handle logout
     */
    @FXML
    public void handleLogOut(ActionEvent actionEvent) {
        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Logout");
        confirmAlert.setHeaderText("Are you sure you want to log out?");
        confirmAlert.setContentText("You will need to log in again to access the system.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        // Clear session
        SessionManager.clearSession();

        // Navigate to login
        navigateTo("Login.fxml", actionEvent);
    }

    /**
     * Navigate to a different screen
     */
    private void navigateTo(String fxmlFile, ActionEvent event) {
        System.out.println("[DEBUG] Navigating to " + fxmlFile);

        try {
            FXMLLoader loader = new FXMLLoader(Login.class.getResource(fxmlFile));

            if (Login.class.getResource(fxmlFile) == null) {
                System.err.println("[ERROR] FXML not found: " + fxmlFile);
                return;
            }

            Scene scene = new Scene(loader.load(), 1280, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            System.out.println("[DEBUG] Navigation successful");

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to navigate to " + fxmlFile);
            e.printStackTrace();
        }
    }


    // ================= UTILITY METHODS =================

    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Refresh the table (called from other controllers after updates)
     */
    public void refreshTable() {
        loadAllUsers();
    }

    /**
     * Get RecordDAO for child controllers
     */
    public RecordDAO getRecordDAO() {
        return recordDAO;
    }
}