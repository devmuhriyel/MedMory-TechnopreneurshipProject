package DAO;

import CommonModel.AdminModel;
import CommonModel.MedicalRecord;
import CommonModel.MedicalRecordSummary;
import MVP.Doctor.DoctorModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;

public class RecordDAO {

    private Connection conn;

    public RecordDAO(Connection conn) {
        this.conn = conn;
    }

    // =========================================================
    // BLOCKCHAIN HASH UTILITY (DETERMINISTIC)
    // =========================================================
//    private String sha256(String input) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            byte[] hash = md.digest(input.getBytes("UTF-8"));
//            StringBuilder hex = new StringBuilder();
//            for (byte b : hash) {
//                hex.append(String.format("%02x", b));
//            }
//            return hex.toString();
//        } catch (Exception e) {
//            throw new RuntimeException("Hashing error", e);
//        }
//    }

    // =========================================================
    // BLOCKCHAIN VALIDATION
    // =========================================================
//    public boolean validateBlockchain() {
//        String sql = "SELECT * FROM records ORDER BY idrecords ASC";
//        String previousHash = "0";
//
//        try (PreparedStatement ps = conn.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//
//            while (rs.next()) {
//                String data =
//                        rs.getInt("patientID") +
//                                rs.getString("patientName") +
//                                rs.getString("cc") +
//                                rs.getString("previous_hash");
//
//                String recalculatedHash = sha256(data);
//
//                if (!recalculatedHash.equals(rs.getString("current_hash"))) {
//                    System.err.println("TAMPERING DETECTED at record ID " + rs.getInt("idrecords"));
//                    return false;
//                }
//
//                if (!previousHash.equals(rs.getString("previous_hash"))) {
//                    System.err.println("CHAIN BROKEN at record ID " + rs.getInt("idrecords"));
//                    return false;
//                }
//
//                previousHash = recalculatedHash;
//            }
//
//            return true;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    // =========================================================
    // INSERT RECORD (WITH BLOCKCHAIN LINKING)
    // =========================================================
    public boolean insertRecord(MedicalRecord r) throws SQLException {

        String lastHash = "0";
        String lastHashSQL = "SELECT current_hash FROM records ORDER BY idrecords DESC LIMIT 1";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(lastHashSQL)) {
            if (rs.next()) lastHash = rs.getString("current_hash");
        }

        String dataToHash =
                r.getPatientID() +
                        r.getPatientName() +
                        r.getCc() +
                        lastHash;

//        String currentHash = sha256(dataToHash);

        String sql = """
            INSERT INTO records
            (patientID, patientName, age, subj, cc, hpi, onset, location,
             duration, charac, factors, radiation, severity,
             pmi, med, allergies, fh, sh, ros,
             doctorID, previous_hash, current_hash)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
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
        stmt.setInt(20, r.getDoctorID());
        stmt.setString(21, lastHash);
//        stmt.setString(22, currentHash);

        return stmt.executeUpdate() > 0;
    }

    // =========================================================
    // FETCH RECORD BY ID (FULL)
    // =========================================================
    public MedicalRecord getRecordByID(int recordID) throws SQLException {
        String query = "SELECT * FROM records WHERE idrecords = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, recordID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                MedicalRecord r = new MedicalRecord();
                r.idrecords = rs.getInt("idrecords");
                r.patientID = rs.getInt("patientID");
                r.patientName = rs.getString("patientName");
                r.age = rs.getInt("age");
                r.subj = rs.getString("subj");
                r.cc = rs.getString("cc");
                r.hpi = rs.getString("hpi");
                r.onset = rs.getString("onset");
                r.location = rs.getString("location");
                r.duration = rs.getString("duration");
                r.charac = rs.getString("charac");
                r.factors = rs.getString("factors");
                r.radiation = rs.getString("radiation");
                r.severity = rs.getString("severity");
                r.pmi = rs.getString("pmi");
                r.med = rs.getString("med");
                r.allergies = rs.getString("allergies");
                r.fh = rs.getString("fh");
                r.sh = rs.getString("sh");
                r.ros = rs.getString("ros");
                r.doctorID = rs.getInt("doctorID");
                return r;
            }
        }
        return null;
    }

    // =========================================================
    // FETCH RECORD BY PATIENT ID
    // =========================================================
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

    // =========================================================
    // DOCTOR INFO
    // =========================================================
    public DoctorModel getDoctorInfo(int doctorID) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM doctors WHERE iddoctors = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, doctorID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                DoctorModel d = new DoctorModel();
                d.id = rs.getInt("iddoctors");
                d.name = rs.getString("name");
                d.specialization = rs.getString("specialization");
                d.email = rs.getString("email");
                d.phone = rs.getString("phone");
                d.biography = rs.getString("biography");
                return d;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // =========================================================
    // DOCTOR RECORD LIST
    // =========================================================
    public static List<MedicalRecordSummary> getDoctorMedicalRecords(int doctorID) {
        List<MedicalRecordSummary> records = new ArrayList<>();

        String sql =
                "SELECT idrecords, patientID, patientName, cc AS diagnosis, duration AS recordDate " +
                        "FROM records WHERE doctorID = ? ORDER BY idrecords DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                records.add(new MedicalRecordSummary(
                        rs.getInt("idrecords"),
                        rs.getInt("patientID"),
                        rs.getString("patientName"),
                        rs.getString("diagnosis"),
                        rs.getString("recordDate")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return records;
    }

    // =========================================================
    // UPDATE RECORD (WITH BLOCKCHAIN RE-HASHING)
    // =========================================================
    public boolean updateRecord(MedicalRecord r) throws SQLException {
        // Get previous hash from the record before this one
        String previousHash = "0";
        String prevHashSQL = "SELECT current_hash FROM records WHERE idrecords < ? ORDER BY idrecords DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(prevHashSQL)) {
            ps.setInt(1, r.getIdrecords());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                previousHash = rs.getString("current_hash");
            }
        }

        // Recalculate hash for updated record
        String dataToHash = r.getPatientID() + r.getPatientName() + r.getCc() + previousHash;
//        String currentHash = sha256(dataToHash);

        // Update all subsequent records' hashes (blockchain integrity)
        String sql = """
            UPDATE records 
            SET patientID = ?, patientName = ?, age = ?, subj = ?, cc = ?, hpi = ?,
                onset = ?, location = ?, duration = ?, charac = ?, factors = ?,
                radiation = ?, severity = ?, pmi = ?, med = ?, allergies = ?,
                fh = ?, sh = ?, ros = ?, previous_hash = ?, current_hash = ?
            WHERE idrecords = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
            stmt.setString(20, previousHash);
//            stmt.setString(21, currentHash);
            stmt.setInt(22, r.getIdrecords());

            boolean updated = stmt.executeUpdate() > 0;

            // Recalculate hashes for all subsequent records
            if (updated) {
                recalculateSubsequentHashes(r.getIdrecords());
            }

            return updated;
        }
    }

    // =========================================================
    // RECALCULATE SUBSEQUENT HASHES (for blockchain integrity)
    // =========================================================
    private void recalculateSubsequentHashes(int recordID) throws SQLException {
        String sql = "SELECT * FROM records WHERE idrecords > ? ORDER BY idrecords ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recordID);
            ResultSet rs = ps.executeQuery();

            String previousHash = null;
            // Get the hash of the updated record
            String getHashSQL = "SELECT current_hash FROM records WHERE idrecords = ?";
            try (PreparedStatement hashPs = conn.prepareStatement(getHashSQL)) {
                hashPs.setInt(1, recordID);
                ResultSet hashRs = hashPs.executeQuery();
                if (hashRs.next()) {
                    previousHash = hashRs.getString("current_hash");
                }
            }

            while (rs.next() && previousHash != null) {
                int nextID = rs.getInt("idrecords");
                String data = rs.getInt("patientID") + rs.getString("patientName") +
                             rs.getString("cc") + previousHash;
//                String newHash = sha256(data);

                String updateHashSQL = "UPDATE records SET previous_hash = ?, current_hash = ? WHERE idrecords = ?";
                try (PreparedStatement updatePs = conn.prepareStatement(updateHashSQL)) {
                    updatePs.setString(1, previousHash);
//                    updatePs.setString(2, newHash);
                    updatePs.setInt(3, nextID);
                    updatePs.executeUpdate();
                }

//                previousHash = newHash;
            }
        }
    }

    // =========================================================
    // DELETE RECORD
    // =========================================================
    public boolean deleteRecord(int recordID) throws SQLException {
        String sql = "DELETE FROM records WHERE idrecords = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recordID);
            return stmt.executeUpdate() > 0;
        }
    }

    // =========================================================
    // SEARCH RECORDS BY DOCTOR ID WITH FILTER
    // =========================================================
    public static List<MedicalRecordSummary> searchDoctorMedicalRecords(int doctorID, String searchTerm) {
        List<MedicalRecordSummary> records = new ArrayList<>();

        String sql = """
            SELECT idrecords, patientID, patientName, cc AS diagnosis, duration AS recordDate 
            FROM records 
            WHERE doctorID = ? 
            AND (patientName LIKE ? OR cc LIKE ? OR duration LIKE ?)
            ORDER BY idrecords DESC
        """;

        String searchPattern = "%" + searchTerm + "%";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorID);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setString(4, searchPattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                records.add(new MedicalRecordSummary(
                    rs.getInt("idrecords"),
                    rs.getInt("patientID"),
                    rs.getString("patientName"),
                    rs.getString("diagnosis"),
                    rs.getString("recordDate")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return records;
    }

    // =========================================================
// FETCH ALL USERS (NAME + EMAIL)
// =========================================================
    public List<AdminModel> getAllUsers() {

        List<AdminModel> users = new ArrayList<>();

        String sql = "SELECT name, email FROM users ORDER BY idusers ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(new AdminModel(
                        rs.getString("name")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public List<AdminModel> getAllPatients() {

        List<AdminModel> patients = new ArrayList<>();

        String sql = "SELECT idusers, name FROM users ORDER BY idusers ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AdminModel patient = new AdminModel();
                patient.setId(rs.getInt("idusers"));   // 🔥 THIS WAS MISSING
                patient.setName(rs.getString("name"));
                patients.add(patient);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return patients;
    }


    public List<DoctorModel> getAllDoctors() {

        List<DoctorModel> doctors = new ArrayList<>();

        String sql = "SELECT * FROM doctors ORDER BY iddoctors ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DoctorModel d = new DoctorModel();
                d.id = rs.getInt("iddoctors");
                d.name = rs.getString("name");
                d.specialization = rs.getString("specialization");
                d.email = rs.getString("email");
                d.phone = rs.getString("phone");
                d.biography = rs.getString("biography");

                doctors.add(d);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return doctors;
    }

    // =========================
// DELETE PATIENT
// =========================
    public boolean deletePatientById(int id) {
        String sql = "DELETE FROM users WHERE idusers = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
// DELETE DOCTOR
// =========================
    public boolean deleteDoctorById(int id) {
        String sql = "DELETE FROM doctors WHERE iddoctors = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePatient(int id, String name) {

        System.out.println("=== DAO.updatePatient DEBUG ===");
        System.out.println("ID   : " + id);
        System.out.println("Name : '" + name + "'");

        String sql = "UPDATE users SET name = ? WHERE idusers = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            System.out.println("Rows affected: " + rows);

            if (rows == 0) {
                System.out.println("⚠ No rows updated. Possible causes:");
                System.out.println(" - ID does not exist");
                System.out.println(" - Name is same as existing value");
            }

            return rows > 0;

        } catch (Exception e) {
            System.out.println("❌ Exception in updatePatient()");
            e.printStackTrace();
            return false;
        } finally {
            System.out.println("=== END updatePatient DEBUG ===");
        }
    }


    public boolean updateDoctor(int id, String name) {

        System.out.println("=== DAO.updateDoctor DEBUG ===");
        System.out.println("ID     : " + id);
        System.out.println("Name   : '" + name + "'");

        String sql = "UPDATE doctors SET name = ? WHERE iddoctors = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            System.out.println("Rows affected: " + rows);

            if (rows == 0) {
                System.out.println("⚠ No rows updated. Possible causes:");
                System.out.println(" - Doctor ID does not exist");
                System.out.println(" - Data is unchanged");
            }

            return rows > 0;

        } catch (Exception e) {
            System.out.println("❌ Exception in updateDoctor()");
            e.printStackTrace();
            return false;
        } finally {
            System.out.println("=== END updateDoctor DEBUG ===");
        }
    }



}
