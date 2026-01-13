package DAO;

import CommonModel.DoctorCase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorService {

    public static List<DoctorCase> getCasesHandledByDoctor(int doctorID) {
        List<DoctorCase> list = new ArrayList<>();

        try {
            Connection conn = DatabaseConnection.getConnection();

            String sql = """
                SELECT 
                    c.case_id,
                    u.idusers AS patientID,
                    u.name AS patientName,
                    c.diagnosis,
                    c.treatment,
                    COALESCE(c.case_date, c.date_completed) AS case_date,
                    c.date_completed
                FROM cases c
                JOIN users u ON c.patientID = u.idusers
                WHERE c.doctorID = ?
                ORDER BY COALESCE(c.case_date, c.date_completed) DESC, c.date_completed DESC
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
                dc.caseDate = rs.getString("case_date");
                dc.dateCompleted = rs.getString("date_completed");

                list.add(dc);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
