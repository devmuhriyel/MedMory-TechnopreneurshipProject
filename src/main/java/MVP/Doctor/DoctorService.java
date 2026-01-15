package MVP.Doctor;

import CommonModel.MedicalRecordSummary;
import DAO.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import CommonModel.DoctorCase;
import java.util.HashSet;
import java.util.Set;



public class DoctorService {

    private static final String URL = "jdbc:mysql://localhost:3306/medical";
    private static final String USER = "root";
    private static final String PASS = "";

    public DoctorModel getDoctorInfo(int doctorID) {
        DoctorModel doctor = null;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM doctors WHERE iddoctors = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, doctorID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                doctor = new DoctorModel();
                doctor.id = rs.getInt("iddoctors");
                doctor.name = rs.getString("name");
                doctor.specialization = rs.getString("specialization");
                doctor.email = rs.getString("email");
                doctor.phone = rs.getString("phone");
                doctor.biography = rs.getString("biography");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doctor;
    }

    /**
     * Verifies if the provided password matches the doctor's current password in the database.
     * @param doctorID The ID of the doctor
     * @param currentPassword The password to verify
     * @return true if the password matches, false otherwise
     */
    public boolean verifyCurrentPassword(int doctorID, String currentPassword) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT password FROM doctors WHERE iddoctors = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, doctorID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                // Compare passwords (currently stored as plain text based on LoginService)
                return currentPassword.equals(dbPassword);
            }
        } catch (Exception e) {
            System.err.println("Error verifying current password: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static List<MedicalRecordSummary> getDoctorMedicalRecords(int doctorID) {
        List<MedicalRecordSummary> records = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT r.idrecords, r.patientID, r.patientName, " +
                            "r.cc AS chiefComplaint, r.duration " +
                            "FROM records r " +
                            "WHERE r.doctorID = ? " +
                            "ORDER BY r.idrecords DESC"
            );

            ps.setInt(1, doctorID);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                records.add(new MedicalRecordSummary(
                        rs.getInt("idrecords"),
                        rs.getInt("patientID"),
                        rs.getString("patientName"),
                        rs.getString("chiefComplaint"),
                        rs.getString("duration")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching doctor's medical records: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    /**
     * Updates doctor information in the database.
     * @param doctorID The ID of the doctor to update
     * @param name The doctor's name
     * @param email The doctor's email
     * @param phone The doctor's phone number
     * @param specialization The doctor's specialization
     * @param biography The doctor's biography
     * @param password The new password (if provided, otherwise null/empty to skip password update)
     * @return true if update was successful, false otherwise
     */
    public boolean updateDoctor(int doctorID, String name, String email, String phone, 
                                 String specialization, String biography, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql;
            PreparedStatement stmt;
            
            // If password is provided and not empty, update all fields including password
            if (password != null && !password.trim().isEmpty()) {
                sql = "UPDATE doctors SET name = ?, email = ?, phone = ?, specialization = ?, biography = ?, password = ? WHERE iddoctors = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, phone);
                stmt.setString(4, specialization);
                stmt.setString(5, biography);
                stmt.setString(6, password);
                stmt.setInt(7, doctorID);
            } else {
                // Update all fields except password
                sql = "UPDATE doctors SET name = ?, email = ?, phone = ?, specialization = ?, biography = ? WHERE iddoctors = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, phone);
                stmt.setString(4, specialization);
                stmt.setString(5, biography);
                stmt.setInt(6, doctorID);
            }
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            System.err.println("Error updating doctor information: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static List<DoctorCase> getCasesHandledByDoctor(int doctorID) {
        List<DoctorCase> cases = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Database connection failed!");
                return cases;
            }

            String query = "SELECT c.case_id, c.patientID, u.name AS patientName, c.diagnosis, c.treatment, c.date_completed " +
                    "FROM cases c " +
                    "JOIN users u ON c.patientID = u.idusers " +
                    "WHERE c.doctorID = ? " +
                    "ORDER BY c.date_completed DESC";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, doctorID);
            ResultSet rs = stmt.executeQuery();

            Set<Integer> addedPatients = new HashSet<>();

            while (rs.next()) {
                int patientID = rs.getInt("patientID");
                if (addedPatients.contains(patientID)) continue; // Avoid duplicates
                addedPatients.add(patientID);

                DoctorCase c = new DoctorCase(
                        rs.getInt("case_id"),
                        patientID,
                        rs.getString("patientName"),
                        rs.getString("date_completed"),
                        rs.getString("diagnosis"),
                        rs.getString("treatment")
                );

                cases.add(c);

                // Debug print
                System.out.println("Loaded patient: " + patientID + " - " + rs.getString("patientName"));
                System.out.println("PatientID=" + patientID + ", Name=" + rs.getString("patientName"));

            }

            System.out.println("Total unique patients loaded: " + cases.size());

        } catch (SQLException e) {
            System.err.println("Error fetching cases: " + e.getMessage());
            e.printStackTrace();
        }

        return cases;
    }


}
