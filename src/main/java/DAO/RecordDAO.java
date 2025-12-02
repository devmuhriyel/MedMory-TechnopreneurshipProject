package DAO;

import CommonModel.MedicalRecord;
import java.sql.*;

public class RecordDAO {

    private Connection conn;

    public RecordDAO(Connection conn) {
        this.conn = conn;
    }

    // Load record by patientID
    public MedicalRecord getRecordByPatientID(int patientID) throws SQLException {
        String sql = "SELECT * FROM records WHERE patientID = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, patientID);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            MedicalRecord r = new MedicalRecord();
            r.setIdrecords(rs.getInt("idrecords"));
            r.setPatientID(rs.getInt("patientID"));
            r.setPatientName(rs.getString("patientName"));
            r.setAge(rs.getInt("age"));

            r.setSubj(rs.getString("subj"));
            r.setCc(rs.getString("cc"));
            r.setHpi(rs.getString("hpi"));
            r.setOnset(rs.getString("onset"));
            r.setLocation(rs.getString("location"));
            r.setDuration(rs.getString("duration"));
            r.setCharacter(rs.getString("charac"));
            r.setFactors(rs.getString("factors"));
            r.setRadiation(rs.getString("radiation"));
            r.setSeverity(rs.getString("severity"));

            r.setPmi(rs.getString("pmi"));
            r.setMed(rs.getString("med"));
            r.setAllergies(rs.getString("allergies"));
            r.setFh(rs.getString("fh"));
            r.setSh(rs.getString("sh"));
            r.setRos(rs.getString("ros"));

            return r;
        }

        return null;
    }

    // Save new record
    public boolean insertRecord(MedicalRecord r) throws SQLException {
        String sql = """
            INSERT INTO records
            (patientID, patientName, age, subj, cc, hpi, onset, location,
             duration, charac, factors, radiation, severity,
             pmi, med, allergies, fh, sh, ros)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setInt(1, r.getPatientID());
        stmt.setString(2, r.getPatientName());
        stmt.setInt(3, r.getAge());

        stmt.setString(4, r.getSubj());
        stmt.setString(5, r.getCc());
        stmt.setString(6, r.getHpi());
        stmt.setString(7, r.getOnset());
        stmt.setString(8, r.getLocation());
        stmt.setString(9, r.getDuration());
        stmt.setString(10, r.getCharacter());
        stmt.setString(11, r.getFactors());
        stmt.setString(12, r.getRadiation());
        stmt.setString(13, r.getSeverity());

        stmt.setString(14, r.getPmi());
        stmt.setString(15, r.getMed());
        stmt.setString(16, r.getAllergies());
        stmt.setString(17, r.getFh());
        stmt.setString(18, r.getSh());
        stmt.setString(19, r.getRos());

        return stmt.executeUpdate() > 0;
    }

    // Update existing record
    public boolean updateRecord(MedicalRecord r) throws SQLException {
        String sql = """
            UPDATE records SET
            patientName=?, age=?, subj=?, cc=?, hpi=?, onset=?, location=?,
            duration=?, charac=?, factors=?, radiation=?, severity=?,
            pmi=?, med=?, allergies=?, fh=?, sh=?, ros=?
            WHERE patientID=?
        """;

        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, r.getPatientName());
        stmt.setInt(2, r.getAge());
        stmt.setString(3, r.getSubj());
        stmt.setString(4, r.getCc());
        stmt.setString(5, r.getHpi());
        stmt.setString(6, r.getOnset());
        stmt.setString(7, r.getLocation());
        stmt.setString(8, r.getDuration());
        stmt.setString(9, r.getCharacter());
        stmt.setString(10, r.getFactors());
        stmt.setString(11, r.getRadiation());
        stmt.setString(12, r.getSeverity());

        stmt.setString(13, r.getPmi());
        stmt.setString(14, r.getMed());
        stmt.setString(15, r.getAllergies());
        stmt.setString(16, r.getFh());
        stmt.setString(17, r.getSh());
        stmt.setString(18, r.getRos());

        stmt.setInt(19, r.getPatientID());

        return stmt.executeUpdate() > 0;
    }
}
