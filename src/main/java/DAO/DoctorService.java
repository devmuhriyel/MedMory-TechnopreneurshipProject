package DAO;

import CommonModel.DoctorCase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorService {

    public static List<DoctorCase> getCasesHandledByDoctor(int doctorID) {
        List<DoctorCase> list = new ArrayList<>();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/medical",
                    "root",
                    ""
            );

            String sql = """
                SELECT 
                    c.case_id,
                    u.idusers AS patientID,
                    u.name AS patientName,
                    c.diagnosis,
                    c.treatment,
                    c.date_completed
                FROM cases c
                JOIN users u ON c.patientID = u.idusers
                WHERE c.doctorID = ?
                ORDER BY c.date_completed DESC
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, doctorID);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                DoctorCase dc = new DoctorCase();
                dc.caseID = rs.getInt("case_id");
                dc.patientID = rs.getInt("patientID");
                dc.patientName = rs.getString("patientName");
                dc.diagnosis = rs.getString("diagnosis");
                dc.treatment = rs.getString("treatment");
                dc.dateCompleted = rs.getString("date_completed");

                list.add(dc);
            }

            conn.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
