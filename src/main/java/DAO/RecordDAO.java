package DAO;

import CommonModel.MedicalRecord;
import CommonModel.MedicalRecordSummary;
import MVP.Doctor.DoctorModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecordDAO {

    private Connection conn;

    public RecordDAO(Connection conn) {
        this.conn = conn;
    }

    public MedicalRecord getRecordByID(int recordID) throws SQLException {
        String query = "SELECT * FROM records WHERE idrecords = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, recordID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                MedicalRecord record = new MedicalRecord();
                record.idrecords = rs.getInt("idrecords");
                record.patientID = rs.getInt("patientID");
                record.patientName = rs.getString("patientName");
                record.age = rs.getInt("age");
                record.subj = rs.getString("subj");
                record.cc = rs.getString("cc");
                record.hpi = rs.getString("hpi");
                record.onset = rs.getString("onset");
                record.location = rs.getString("location");
                record.duration = rs.getString("duration");
                record.charac = rs.getString("charac");
                record.factors = rs.getString("factors");
                record.radiation = rs.getString("radiation");
                record.severity = rs.getString("severity");
                record.pmi = rs.getString("pmi");
                record.med = rs.getString("med");
                record.allergies = rs.getString("allergies");
                record.fh = rs.getString("fh");
                record.sh = rs.getString("sh");
                record.ros = rs.getString("ros");
                record.doctorID = rs.getInt("doctorID");
                return record;
            }
        }

        return null;
    }

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

        String sql =
                "SELECT r.idrecords, r.patientID, r.patientName, " +
                        "r.cc AS diagnosis, " +
                        "r.duration AS recordDate " +
                        "FROM records r " +
                        "WHERE r.doctorID = ? " +
                        "ORDER BY r.idrecords DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MedicalRecordSummary summary = new MedicalRecordSummary(
                        rs.getInt("idrecords"),
                        rs.getInt("patientID"),
                        rs.getString("patientName"),
                        rs.getString("diagnosis"),
                        rs.getString("recordDate")
                );

                records.add(summary);
            }

        } catch (Exception e) {
            System.err.println("Error fetching doctor's medical records: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    public MedicalRecord getRecordByPatientID(int patientID) throws SQLException {
        String sql = "SELECT * FROM records WHERE patientID = ? LIMIT 1";
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

            r.setDoctorID(rs.getInt("doctorID"));

            return r;
        }

        return null;
    }

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

    public boolean updateRecord(MedicalRecord r) throws SQLException {
        String sql = """
            UPDATE records SET
            patientName=?, age=?, subj=?, cc=?, hpi=?, onset=?, location=?,
            duration=?, charac=?, factors=?, radiation=?, severity=?,
            pmi=?, med=?, allergies=?, fh=?, sh=?, ros=?
            WHERE idrecords=?
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

        stmt.setInt(19, r.getIdrecords());

        return stmt.executeUpdate() > 0;
    }
}