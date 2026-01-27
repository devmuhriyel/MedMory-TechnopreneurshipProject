package MVP.Client;

import CommonModel.PatientModel;
import DAO.DatabaseConnection;
import DAO.RecordDAO;

import java.sql.*;

/**
 * ClientService - Service layer for client/patient operations
 * Now uses RecordDAO for database access
 */
public class ClientService {

    /**
     * Get client/patient information by user ID
     * @param userID The patient's user ID
     * @return PatientModel containing patient information, or null if not found
     */
    public static PatientModel getClientInfo(int userID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            RecordDAO recordDAO = new RecordDAO(conn);
            PatientModel patient = recordDAO.getPatientById(userID);

            if (patient != null) {
                System.out.println("✅ Retrieved patient info for ID: " + userID);
            } else {
                System.out.println("⚠ No patient found with ID: " + userID);
            }

            return patient;

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving patient info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update patient information
     * @param userID The patient's user ID
     * @param name Full name
     * @param email Email address
     * @param phoneNumber Phone number
     * @param birthday Birthday in YYYY-MM-DD format
     * @return true if update successful, false otherwise
     */
    public static boolean updateClientInfo(int userID, String name, String email, String phoneNumber, String birthday) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            RecordDAO recordDAO = new RecordDAO(conn);

            boolean success = recordDAO.updatePatient(userID, name, email, phoneNumber, birthday);

            if (success) {
                System.out.println("✅ Updated patient info for ID: " + userID);
            } else {
                System.out.println("❌ Failed to update patient info for ID: " + userID);
            }

            return success;

        } catch (SQLException e) {
            System.err.println("❌ Error updating patient info: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update password for a patient
     * @param userID The patient's user ID
     * @param currentPass Current password (for verification)
     * @param newPass New password to set
     * @return true if password updated successfully, false otherwise
     */
    public static boolean updatePassword(int userID, String currentPass, String newPass) {
        String verifySql = "SELECT user_id FROM users WHERE user_id = ? AND password_hash = ? AND role = 'PATIENT'";
        String updateSql = "UPDATE users SET password_hash = ? WHERE user_id = ? AND role = 'PATIENT'";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1. Verify Current Password
            try (PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {
                verifyStmt.setInt(1, userID);
                verifyStmt.setString(2, currentPass);

                try (ResultSet rs = verifyStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("❌ Verification Failed: Current password does not match for ID " + userID);
                        return false;
                    }
                }
            }

            // 2. Update Password
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newPass);
                updateStmt.setInt(2, userID);

                int affectedRows = updateStmt.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("✅ Password updated successfully for ID " + userID);
                    return true;
                } else {
                    System.out.println("❌ No rows affected during password update for ID " + userID);
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL Error during password update: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verify if current password is correct
     * @param userID The patient's user ID
     * @param password Password to verify
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(int userID, String password) {
        String sql = "SELECT user_id FROM users WHERE user_id = ? AND password_hash = ? AND role = 'PATIENT'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userID);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                boolean matches = rs.next();

                if (matches) {
                    System.out.println("✅ Password verified for ID: " + userID);
                } else {
                    System.out.println("❌ Password verification failed for ID: " + userID);
                }

                return matches;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error verifying password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a patient account
     * @param userID The patient's user ID
     * @return true if deletion successful, false otherwise
     */
    public static boolean deletePatient(int userID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            RecordDAO recordDAO = new RecordDAO(conn);

            boolean success = recordDAO.deletePatientById(userID);

            if (success) {
                System.out.println("✅ Deleted patient with ID: " + userID);
            } else {
                System.out.println("❌ Failed to delete patient with ID: " + userID);
            }

            return success;

        } catch (SQLException e) {
            System.err.println("❌ Error deleting patient: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if email already exists for another patient
     * @param email Email to check
     * @param excludeUserID User ID to exclude from check (for updates)
     * @return true if email exists for another user, false otherwise
     */
    public static boolean emailExistsForOtherUser(String email, int excludeUserID) {
        String sql = "SELECT user_id FROM users WHERE email = ? AND user_id != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setInt(2, excludeUserID);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("❌ Error checking email existence: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get patient's assigned doctors
     * @param patientID The patient's user ID
     * @return Number of assigned doctors
     */
    public static int getAssignedDoctorCount(int patientID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            RecordDAO recordDAO = new RecordDAO(conn);

            var doctors = recordDAO.getPatientDoctors(patientID);
            return doctors.size();

        } catch (SQLException e) {
            System.err.println("❌ Error getting doctor count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get count of patient's medical records
     * @param patientID The patient's user ID
     * @return Number of medical records
     */
    public static int getMedicalRecordCount(int patientID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            RecordDAO recordDAO = new RecordDAO(conn);

            var records = recordDAO.getMedicalRecordsByPatient(patientID);
            return records.size();

        } catch (SQLException e) {
            System.err.println("❌ Error getting record count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get count of patient's medical cases
     * @param patientID The patient's user ID
     * @return Number of medical cases
     */
    public static int getMedicalCaseCount(int patientID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            RecordDAO recordDAO = new RecordDAO(conn);

            var cases = recordDAO.getMedicalCasesByPatient(patientID);
            return cases.size();

        } catch (SQLException e) {
            System.err.println("❌ Error getting case count: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}