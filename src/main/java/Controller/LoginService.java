package Controller;

import DAO.DatabaseConnection;
import MVP.SessionManager;

import java.sql.*;

public class LoginService {

    /**
     * Validate user credentials and return appropriate login result
     * Checks users table with role-based authentication
     */
    public static LoginResult validateCredentials(String email, String password) {

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                return new LoginResult("error");
            }

            // Query the users table - all users (ADMIN, DOCTOR, PATIENT) are here
            String userQuery = """
                SELECT user_id, name, email, role 
                FROM users 
                WHERE email = ? AND password_hash = ?
            """;

            try (PreparedStatement ps = conn.prepareStatement(userQuery)) {
                ps.setString(1, email.trim());
                ps.setString(2, password); // In production, compare hashed password

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String name = rs.getString("name");
                    String role = rs.getString("role");

                    // Set session based on role
                    switch (role) {
                        case "ADMIN":
                            SessionManager.setClientSession(userId);
                            return new LoginResult("admin", userId, userId, name);

                        case "DOCTOR":
                            // Check if doctor profile exists
                            if (isDoctorProfileComplete(conn, userId)) {
                                SessionManager.setDoctorSession(userId, -1);
                                return new LoginResult("doctor", userId, -1, name);
                            } else {
                                return new LoginResult("error", "Doctor profile incomplete");
                            }

                        case "PATIENT":
                            // Check if patient profile exists
                            if (isPatientProfileComplete(conn, userId)) {
                                SessionManager.setClientSession(userId);
                                return new LoginResult("patient", userId, userId, name);
                            } else {
                                return new LoginResult("error", "Patient profile incomplete");
                            }

                        default:
                            return new LoginResult("error", "Invalid user role");
                    }
                }
            }

            // No matching credentials found
            return new LoginResult("invalid");

        } catch (SQLException e) {
            System.err.println("SQL Error in validateCredentials: " + e.getMessage());
            e.printStackTrace();
            return new LoginResult("error");
        }
    }

    /**
     * Alternative login method using username instead of email
     */
    public static LoginResult validateCredentialsByUsername(String username, String password) {

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                return new LoginResult("error");
            }

            String userQuery = """
                SELECT user_id, name, email, role 
                FROM users 
                WHERE name = ? AND password_hash = ?
            """;

            try (PreparedStatement ps = conn.prepareStatement(userQuery)) {
                ps.setString(1, username.trim());
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String name = rs.getString("name");
                    String role = rs.getString("role");

                    switch (role) {
                        case "ADMIN":
                            SessionManager.setClientSession(userId);
                            return new LoginResult("admin", userId, userId, name);

                        case "DOCTOR":
                            if (isDoctorProfileComplete(conn, userId)) {
                                SessionManager.setDoctorSession(userId, -1);
                                return new LoginResult("doctor", userId, -1, name);
                            } else {
                                return new LoginResult("error", "Doctor profile incomplete");
                            }

                        case "PATIENT":
                            if (isPatientProfileComplete(conn, userId)) {
                                SessionManager.setClientSession(userId);
                                return new LoginResult("patient", userId, userId, name);
                            } else {
                                return new LoginResult("error", "Patient profile incomplete");
                            }

                        default:
                            return new LoginResult("error", "Invalid user role");
                    }
                }
            }

            return new LoginResult("invalid");

        } catch (SQLException e) {
            System.err.println("SQL Error in validateCredentialsByUsername: " + e.getMessage());
            e.printStackTrace();
            return new LoginResult("error");
        }
    }

    /**
     * Check if doctor profile is complete in doctors table
     */
    private static boolean isDoctorProfileComplete(Connection conn, int userId) throws SQLException {
        String sql = "SELECT doctor_id FROM doctors WHERE doctor_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    /**
     * Check if patient profile is complete in patients table
     */
    private static boolean isPatientProfileComplete(Connection conn, int userId) throws SQLException {
        String sql = "SELECT patient_id FROM patients WHERE patient_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    /**
     * Register a new patient user
     */
    public static boolean registerPatient(String name, String email, String password, String phoneNumber, String birthday) {

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                return false;
            }

            conn.setAutoCommit(false);

            // Check if email already exists
            if (emailExists(email)) {
                return false;
            }

            int userId;

            // Step 1: Insert into users table
            String insertUser = """
                INSERT INTO users (name, email, phoneNumber, birthday, password_hash, role) 
                VALUES (?, ?, ?, ?, ?, 'PATIENT')
            """;

            try (PreparedStatement ps = conn.prepareStatement(insertUser, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, phoneNumber);
                ps.setString(4, birthday); // Format: YYYY-MM-DD
                ps.setString(5, password); // In production, hash the password first

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }

                // Get generated user_id
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Step 2: Insert into patients table
            String insertPatient = "INSERT INTO patients (patient_id) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(insertPatient)) {
                ps.setInt(1, userId);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            System.err.println("SQL Error in registerPatient: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Register a new doctor user
     */
    public static boolean registerDoctor(String name, String email, String password, String phoneNumber,
                                         String birthday, String specialization, String biography, String address) {

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                return false;
            }

            conn.setAutoCommit(false);

            // Check if email already exists
            if (emailExists(email)) {
                return false;
            }

            int userId;

            // Step 1: Insert into users table
            String insertUser = """
                INSERT INTO users (name, email, phoneNumber, birthday, password_hash, role) 
                VALUES (?, ?, ?, ?, ?, 'DOCTOR')
            """;

            try (PreparedStatement ps = conn.prepareStatement(insertUser, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, phoneNumber);
                ps.setString(4, birthday); // Format: YYYY-MM-DD
                ps.setString(5, password); // In production, hash the password first

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }

                // Get generated user_id
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Step 2: Insert into doctors table
            String insertDoctor = """
                INSERT INTO doctors (doctor_id, specialization, biography, address) 
                VALUES (?, ?, ?, ?)
            """;

            try (PreparedStatement ps = conn.prepareStatement(insertDoctor)) {
                ps.setInt(1, userId);
                ps.setString(2, specialization);
                ps.setString(3, biography);
                ps.setString(4, address);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            System.err.println("SQL Error in registerDoctor: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Register a new admin user
     */
    public static boolean registerAdmin(String name, String email, String password, String phoneNumber) {

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                return false;
            }

            // Check if email already exists
            if (emailExists(email)) {
                return false;
            }

            String insertUser = """
                INSERT INTO users (name, email, phoneNumber, password_hash, role) 
                VALUES (?, ?, ?, ?, 'ADMIN')
            """;

            try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, phoneNumber);
                ps.setString(4, password); // In production, hash the password first

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in registerAdmin: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Check if email already exists in the system
     */
    public static boolean emailExists(String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if username already exists in the system
     */
    public static boolean usernameExists(String username) {
        String sql = "SELECT user_id FROM users WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("SQL Error in usernameExists: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Change user password
     */
    public static boolean changePassword(int userId, String oldPassword, String newPassword) {

        String verifyQuery = "SELECT password_hash FROM users WHERE user_id = ?";
        String updateQuery = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                return false;
            }

            // Verify old password
            try (PreparedStatement ps = conn.prepareStatement(verifyQuery)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String currentPassword = rs.getString("password_hash");

                    // Check if old password matches
                    if (!currentPassword.equals(oldPassword)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }

            // Update to new password
            try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                ps.setString(1, newPassword); // In production, hash the new password first
                ps.setInt(2, userId);
                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in changePassword: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get user role by user ID
     */
    public static String getUserRole(int userId) {
        String sql = "SELECT role FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("role");
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in getUserRole: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get user information by user ID
     */
    public static UserInfo getUserInfo(int userId) {
        String sql = "SELECT user_id, name, email, phoneNumber, birthday, role FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new UserInfo(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phoneNumber"),
                        rs.getString("birthday"),
                        rs.getString("role")
                );
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in getUserInfo: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}