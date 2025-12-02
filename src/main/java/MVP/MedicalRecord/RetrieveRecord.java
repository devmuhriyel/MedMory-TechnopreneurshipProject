package MVP.MedicalRecord;

import CommonModel.MedicalRecord;

import java.sql.*;

public class RetrieveRecord {

    private static final String URL = "jdbc:mysql://localhost:3306/medical";
    private static final String USER = "root";          // your mysql username
    private static final String PASSWORD = "";          // your mysql password

    public MedicalRecord getRecordByUserID(int userID) {
        MedicalRecord record = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");  // <---- ADD THIS
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String query = "SELECT * FROM records WHERE patientID = ?";

        try (Connection conn = DriverManager.   getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userID);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                record = new MedicalRecord();

                record.setIdrecords(rs.getInt("idrecords"));
                record.setPatientID(rs.getInt("patientID"));
                record.setPatientName(rs.getString("patientName"));
                record.setAge(rs.getInt("age"));
                record.setSubj(rs.getString("subj"));
                record.setCc(rs.getString("cc"));
                record.setHpi(rs.getString("hpi"));
                record.setOnset(rs.getString("onset"));
                record.setLocation(rs.getString("location"));
                record.setDuration(rs.getString("duration"));
                record.setCharacter(rs.getString("charac"));
                record.setFactors(rs.getString("factors"));
                record.setRadiation(rs.getString("radiation"));
                record.setSeverity(rs.getString("severity"));
                record.setPmi(rs.getString("pmi"));
                record.setMed(rs.getString("med"));
                record.setAllergies(rs.getString("allergies"));
                record.setFh(rs.getString("fh"));
                record.setSh(rs.getString("sh"));
                record.setRos(rs.getString("ros"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return record;
    }

    // Simple test
    public static void main(String[] args) {
        RetrieveRecord rr = new RetrieveRecord();
        MedicalRecord rec = rr.getRecordByUserID(1);

        if (rec != null) {
            System.out.println("Record Found: " + rec.getPatientName());
        } else {
            System.out.println("No medical record found for that user.");
        }
    }
}

