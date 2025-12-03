package MVP.Doctor;

import CommonModel.MedicalRecordSummary;
import DAO.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
}
