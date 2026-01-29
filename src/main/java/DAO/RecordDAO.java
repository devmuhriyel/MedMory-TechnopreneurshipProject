package DAO;

import CommonModel.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecordDAO {

    private Connection conn;

    public RecordDAO(Connection conn) {
        this.conn = conn;
    }

    // ========================================
    // MEDICAL RECORDS CRUD
    // ========================================

    /**
     * Create a new medical record with complete data
     */
    public boolean createMedicalRecordComplete(MedicalRecordModel record) {
        String sql = """
        INSERT INTO medical_records (
            patient_id, patient_name, age,
            subjective, chief_complaint, history_present_illness,
            onset, location, duration, characteristics,
            aggravating_factors, radiation, severity,
            past_medical_illness, medications, allergies,
            family_history, social_history, review_of_systems,
            doctor_id
        ) VALUES (
            ?, ?, ?,
            ?, ?, ?,
            ?, ?, ?, ?,
            ?, ?, ?,
            ?, ?, ?,
            ?, ?, ?,
            ?
        )
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, record.getPatientId());
            ps.setString(2, record.getPatientName());
            ps.setInt(3, record.getAge());
            ps.setString(4, record.getSubjective());
            ps.setString(5, record.getChiefComplaint());
            ps.setString(6, record.getHistoryPresentIllness());
            ps.setString(7, record.getOnset());
            ps.setString(8, record.getLocation());
            ps.setString(9, record.getDuration());
            ps.setString(10, record.getCharacteristics());
            ps.setString(11, record.getAggravatingFactors());
            ps.setString(12, record.getRadiation());
            ps.setString(13, record.getSeverity());
            ps.setString(14, record.getPastMedicalIllness());
            ps.setString(15, record.getMedications());
            ps.setString(16, record.getAllergies());
            ps.setString(17, record.getFamilyHistory());
            ps.setString(18, record.getSocialHistory());
            ps.setString(19, record.getReviewOfSystems());
            ps.setInt(20, record.getDoctorId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in createMedicalRecordComplete: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create a new medical record (backward compatibility - simple version)
     */
    public boolean createMedicalRecord(int patientId, int doctorId, String subjective) {
        String sql = """
        INSERT INTO medical_records (
            patient_id, patient_name, age,
            subjective, chief_complaint, history_present_illness,
            onset, location, duration, characteristics,
            aggravating_factors, radiation, severity,
            past_medical_illness, medications, allergies,
            family_history, social_history, review_of_systems,
            doctor_id
        ) VALUES (
            ?, '', 0,
            ?, '', '',
            '', '', '', '',
            '', '', '',
            '', '', '',
            '', '', '',
            ?
        )
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setString(2, subjective);
            ps.setInt(3, doctorId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in createMedicalRecord: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all medical records for a patient - WITH ALL FIELDS
     */
    public List<MedicalRecordModel> getMedicalRecordsByPatient(int patientId) {
        List<MedicalRecordModel> records = new ArrayList<>();

        String sql = """
        SELECT mr.record_id, mr.patient_id, mr.patient_name, mr.age,
               mr.subjective, mr.chief_complaint, mr.history_present_illness,
               mr.onset, mr.location, mr.duration, mr.characteristics,
               mr.aggravating_factors, mr.radiation, mr.severity,
               mr.past_medical_illness, mr.medications, mr.allergies,
               mr.family_history, mr.social_history, mr.review_of_systems,
               mr.doctor_id, mr.created_at,
               d_user.name AS doctor_name
        FROM medical_records mr
        LEFT JOIN doctors d ON mr.doctor_id = d.doctor_id
        LEFT JOIN users d_user ON d.doctor_id = d_user.user_id
        WHERE mr.patient_id = ?
        ORDER BY mr.created_at DESC
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MedicalRecordModel record = new MedicalRecordModel();
                    record.setRecordId(rs.getInt("record_id"));
                    record.setPatientId(rs.getInt("patient_id"));
                    record.setPatientName(rs.getString("patient_name"));
                    record.setAge(rs.getInt("age"));
                    record.setSubjective(rs.getString("subjective"));
                    record.setChiefComplaint(rs.getString("chief_complaint"));
                    record.setHistoryPresentIllness(rs.getString("history_present_illness"));
                    record.setOnset(rs.getString("onset"));
                    record.setLocation(rs.getString("location"));
                    record.setDuration(rs.getString("duration"));
                    record.setCharacteristics(rs.getString("characteristics"));
                    record.setAggravatingFactors(rs.getString("aggravating_factors"));
                    record.setRadiation(rs.getString("radiation"));
                    record.setSeverity(rs.getString("severity"));
                    record.setPastMedicalIllness(rs.getString("past_medical_illness"));
                    record.setMedications(rs.getString("medications"));
                    record.setAllergies(rs.getString("allergies"));
                    record.setFamilyHistory(rs.getString("family_history"));
                    record.setSocialHistory(rs.getString("social_history"));
                    record.setReviewOfSystems(rs.getString("review_of_systems"));
                    record.setDoctorId(rs.getInt("doctor_id"));
                    record.setDoctorName(rs.getString("doctor_name"));

                    // Handle timestamp
                    java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
                    if (timestamp != null) {
                        record.setCreatedAt(timestamp.toLocalDateTime());
                    }

                    records.add(record);
                }
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in getMedicalRecordsByPatient: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    /**
     * Get a specific medical record by ID - WITH ALL FIELDS
     */
    public MedicalRecordModel getMedicalRecordById(int recordId) {
        String sql = """
        SELECT mr.record_id, mr.patient_id, mr.patient_name, mr.age,
               mr.subjective, mr.chief_complaint, mr.history_present_illness,
               mr.onset, mr.location, mr.duration, mr.characteristics,
               mr.aggravating_factors, mr.radiation, mr.severity,
               mr.past_medical_illness, mr.medications, mr.allergies,
               mr.family_history, mr.social_history, mr.review_of_systems,
               mr.doctor_id, mr.created_at,
               d_user.name AS doctor_name
        FROM medical_records mr
        LEFT JOIN doctors d ON mr.doctor_id = d.doctor_id
        LEFT JOIN users d_user ON d.doctor_id = d_user.user_id
        WHERE mr.record_id = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recordId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MedicalRecordModel record = new MedicalRecordModel();
                    record.setRecordId(rs.getInt("record_id"));
                    record.setPatientId(rs.getInt("patient_id"));
                    record.setPatientName(rs.getString("patient_name"));
                    record.setAge(rs.getInt("age"));
                    record.setSubjective(rs.getString("subjective"));
                    record.setChiefComplaint(rs.getString("chief_complaint"));
                    record.setHistoryPresentIllness(rs.getString("history_present_illness"));
                    record.setOnset(rs.getString("onset"));
                    record.setLocation(rs.getString("location"));
                    record.setDuration(rs.getString("duration"));
                    record.setCharacteristics(rs.getString("characteristics"));
                    record.setAggravatingFactors(rs.getString("aggravating_factors"));
                    record.setRadiation(rs.getString("radiation"));
                    record.setSeverity(rs.getString("severity"));
                    record.setPastMedicalIllness(rs.getString("past_medical_illness"));
                    record.setMedications(rs.getString("medications"));
                    record.setAllergies(rs.getString("allergies"));
                    record.setFamilyHistory(rs.getString("family_history"));
                    record.setSocialHistory(rs.getString("social_history"));
                    record.setReviewOfSystems(rs.getString("review_of_systems"));
                    record.setDoctorId(rs.getInt("doctor_id"));
                    record.setDoctorName(rs.getString("doctor_name"));

                    // Handle timestamp
                    java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
                    if (timestamp != null) {
                        record.setCreatedAt(timestamp.toLocalDateTime());
                    }

                    return record;
                }
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in getMedicalRecordById: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Update a medical record with complete data (creates a version entry)
     */
    public boolean updateMedicalRecordComplete(MedicalRecordModel record) {
        // First, save the old version
        String getOld = """
        SELECT patient_name, age, subjective, chief_complaint, history_present_illness,
               onset, location, duration, characteristics, aggravating_factors,
               radiation, severity, past_medical_illness, medications, allergies,
               family_history, social_history, review_of_systems
        FROM medical_records 
        WHERE record_id = ?
    """;

        String update = """
        UPDATE medical_records SET
            patient_name = ?, age = ?, subjective = ?, chief_complaint = ?,
            history_present_illness = ?, onset = ?, location = ?, duration = ?,
            characteristics = ?, aggravating_factors = ?, radiation = ?,
            severity = ?, past_medical_illness = ?, medications = ?,
            allergies = ?, family_history = ?, social_history = ?,
            review_of_systems = ?, doctor_id = ?
        WHERE record_id = ?
    """;

        String insertVersion = """
        INSERT INTO medical_record_versions (record_id, content, updated_by) 
        VALUES (?, ?, ?)
    """;

        try {
            conn.setAutoCommit(false);

            // Get old content for versioning
            String oldContent = null;
            try (PreparedStatement ps = conn.prepareStatement(getOld)) {
                ps.setInt(1, record.getRecordId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Build a summary of old content
                        StringBuilder content = new StringBuilder();
                        content.append("Patient: ").append(rs.getString("patient_name")).append("\n");
                        content.append("Age: ").append(rs.getInt("age")).append("\n");
                        content.append("Subjective: ").append(rs.getString("subjective")).append("\n");
                        content.append("Chief Complaint: ").append(rs.getString("chief_complaint")).append("\n");
                        // Add other fields as needed for version history
                        oldContent = content.toString();
                    }
                }
            }

            // Save old version
            if (oldContent != null) {
                try (PreparedStatement ps = conn.prepareStatement(insertVersion)) {
                    ps.setInt(1, record.getRecordId());
                    ps.setString(2, oldContent);
                    ps.setInt(3, record.getDoctorId());
                    ps.executeUpdate();
                }
            }

            // Update the record
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setString(1, record.getPatientName());
                ps.setInt(2, record.getAge());
                ps.setString(3, record.getSubjective());
                ps.setString(4, record.getChiefComplaint());
                ps.setString(5, record.getHistoryPresentIllness());
                ps.setString(6, record.getOnset());
                ps.setString(7, record.getLocation());
                ps.setString(8, record.getDuration());
                ps.setString(9, record.getCharacteristics());
                ps.setString(10, record.getAggravatingFactors());
                ps.setString(11, record.getRadiation());
                ps.setString(12, record.getSeverity());
                ps.setString(13, record.getPastMedicalIllness());
                ps.setString(14, record.getMedications());
                ps.setString(15, record.getAllergies());
                ps.setString(16, record.getFamilyHistory());
                ps.setString(17, record.getSocialHistory());
                ps.setString(18, record.getReviewOfSystems());
                ps.setInt(19, record.getDoctorId());
                ps.setInt(20, record.getRecordId());

                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            System.err.println("SQL Error in updateMedicalRecordComplete: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /**
     * Update a medical record (backward compatibility - simple version)
     */
    public boolean updateMedicalRecord(int recordId, String newSubjective, int updatedByDoctorId) {
        String getOld = "SELECT subjective FROM medical_records WHERE record_id = ?";
        String update = "UPDATE medical_records SET subjective = ? WHERE record_id = ?";
        String insertVersion =
                "INSERT INTO medical_record_versions (record_id, content, updated_by) VALUES (?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            String oldSubjective = null;

            try (PreparedStatement ps = conn.prepareStatement(getOld)) {
                ps.setInt(1, recordId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        oldSubjective = rs.getString("subjective");
                    }
                }
            }

            if (oldSubjective != null) {
                try (PreparedStatement ps = conn.prepareStatement(insertVersion)) {
                    ps.setInt(1, recordId);
                    ps.setString(2, oldSubjective);
                    ps.setInt(3, updatedByDoctorId);
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setString(1, newSubjective);
                ps.setInt(2, recordId);

                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            System.err.println("SQL Error in updateMedicalRecord: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /**
     * Delete a medical record
     */
    public boolean deleteMedicalRecord(int recordId) {
        String sql = "DELETE FROM medical_records WHERE record_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recordId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in deleteMedicalRecord: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get version history for a medical record
     */
    public List<MedicalRecordVersionModel> getRecordVersions(int recordId) {
        List<MedicalRecordVersionModel> versions = new ArrayList<>();

        String sql = """
        SELECT mrv.version_id, mrv.record_id, mrv.content,
               mrv.updated_by, mrv.updated_at,
               u.name AS doctor_name
        FROM medical_record_versions mrv
        INNER JOIN doctors d ON mrv.updated_by = d.doctor_id
        INNER JOIN users u ON d.doctor_id = u.user_id
        WHERE mrv.record_id = ?
        ORDER BY mrv.updated_at DESC
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recordId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MedicalRecordVersionModel version = new MedicalRecordVersionModel();
                    version.setVersionId(rs.getInt("version_id"));
                    version.setRecordId(rs.getInt("record_id"));
                    version.setContent(rs.getString("content"));
                    version.setUpdatedBy(rs.getInt("updated_by"));
                    version.setUpdatedAt(rs.getString("updated_at"));
                    version.setDoctorName(rs.getString("doctor_name"));
                    versions.add(version);
                }
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in getRecordVersions: " + e.getMessage());
            e.printStackTrace();
        }

        return versions;
    }

    // ========================================
    // MEDICAL CASES CRUD
    // ========================================

    /**
     * Create a new medical case
     */
    public boolean createMedicalCase(int patientId, int doctorId, String diagnosis, String treatment, String status) {
        String sql = "INSERT INTO medical_cases (patient_id, doctor_id, diagnosis, treatment, status) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ps.setString(3, diagnosis);
            ps.setString(4, treatment);
            ps.setString(5, status);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in createMedicalCase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all medical cases for a patient
     */
    public List<MedicalCaseModel> getMedicalCasesByPatient(int patientId) {
        List<MedicalCaseModel> cases = new ArrayList<>();

        String sql = """
            SELECT mc.case_id, mc.patient_id, mc.doctor_id, mc.diagnosis, mc.treatment, mc.status, mc.created_at,
                   p_user.name as patient_name, d_user.name as doctor_name
            FROM medical_cases mc
            INNER JOIN patients p ON mc.patient_id = p.patient_id
            INNER JOIN users p_user ON p.patient_id = p_user.user_id
            INNER JOIN doctors d ON mc.doctor_id = d.doctor_id
            INNER JOIN users d_user ON d.doctor_id = d_user.user_id
            WHERE mc.patient_id = ?
            ORDER BY mc.created_at DESC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MedicalCaseModel medCase = new MedicalCaseModel();
                    medCase.setCaseId(rs.getInt("case_id"));
                    medCase.setPatientId(rs.getInt("patient_id"));
                    medCase.setDoctorId(rs.getInt("doctor_id"));
                    medCase.setDiagnosis(rs.getString("diagnosis"));
                    medCase.setTreatment(rs.getString("treatment"));
                    medCase.setStatus(rs.getString("status"));
                    medCase.setCreatedAt(rs.getString("created_at"));
                    medCase.setPatientName(rs.getString("patient_name"));
                    medCase.setDoctorName(rs.getString("doctor_name"));
                    cases.add(medCase);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getMedicalCasesByPatient: " + e.getMessage());
            e.printStackTrace();
        }

        return cases;
    }

    /**
     * Get all medical cases for a doctor
     */
    public List<MedicalCaseModel> getMedicalCasesByDoctor(int doctorId) {
        List<MedicalCaseModel> cases = new ArrayList<>();

        String sql = """
            SELECT mc.case_id, mc.patient_id, mc.doctor_id, mc.diagnosis, mc.treatment, mc.status, mc.created_at,
                   p_user.name as patient_name, d_user.name as doctor_name
            FROM medical_cases mc
            INNER JOIN patients p ON mc.patient_id = p.patient_id
            INNER JOIN users p_user ON p.patient_id = p_user.user_id
            INNER JOIN doctors d ON mc.doctor_id = d.doctor_id
            INNER JOIN users d_user ON d.doctor_id = d_user.user_id
            WHERE mc.doctor_id = ?
            ORDER BY mc.created_at DESC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MedicalCaseModel medCase = new MedicalCaseModel();
                    medCase.setCaseId(rs.getInt("case_id"));
                    medCase.setPatientId(rs.getInt("patient_id"));
                    medCase.setDoctorId(rs.getInt("doctor_id"));
                    medCase.setDiagnosis(rs.getString("diagnosis"));
                    medCase.setTreatment(rs.getString("treatment"));
                    medCase.setStatus(rs.getString("status"));
                    medCase.setCreatedAt(rs.getString("created_at"));
                    medCase.setPatientName(rs.getString("patient_name"));
                    medCase.setDoctorName(rs.getString("doctor_name"));
                    cases.add(medCase);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getMedicalCasesByDoctor: " + e.getMessage());
            e.printStackTrace();
        }

        return cases;
    }

    /**
     * Update a medical case
     */
    public boolean updateMedicalCase(int caseId, String diagnosis, String treatment, String status) {
        String sql = "UPDATE medical_cases SET diagnosis = ?, treatment = ?, status = ? WHERE case_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, diagnosis);
            ps.setString(2, treatment);
            ps.setString(3, status);
            ps.setInt(4, caseId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in updateMedicalCase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Close a medical case
     */
    public boolean closeMedicalCase(int caseId) {
        String sql = "UPDATE medical_cases SET status = 'CLOSED' WHERE case_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caseId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in closeMedicalCase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a medical case
     */
    public boolean deleteMedicalCase(int caseId) {
        String sql = "DELETE FROM medical_cases WHERE case_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caseId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in deleteMedicalCase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ========================================
    // PATIENT CRUD
    // ========================================


    /**
     * Create a new patient with location
     */
    public boolean createPatient(String name, String email, String password, String phoneNumber, String birthday, String location) {
        String insertUser = "INSERT INTO users (name, email, phoneNumber, birthday, password_hash, role) VALUES (?, ?, ?, ?, ?, 'PATIENT')";
        String insertPatient = "INSERT INTO patients (patient_id) VALUES (?)";

        try {
            conn.setAutoCommit(false);

            int userId;

            // Insert into users table
            try (PreparedStatement ps = conn.prepareStatement(insertUser, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, phoneNumber);
                ps.setString(4, birthday);
                ps.setString(5, password);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Insert into patients table
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
            try { conn.rollback(); } catch (SQLException ignored) {}
            System.err.println("SQL Error in createPatient: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    public boolean createPatient(String name, String email, String password, String phoneNumber, String birthday) {
        return createPatient(name, email, password, phoneNumber, birthday, "");
    }

    /**
     * Get all patients with location
     */
    public List<PatientModel> getAllPatients() {
        List<PatientModel> patients = new ArrayList<>();

        String sql = """
        SELECT u.user_id, u.name, u.email, u.phoneNumber, u.birthday, u.created_at
        FROM users u
        INNER JOIN patients p ON u.user_id = p.patient_id
        WHERE u.role = 'PATIENT'
        ORDER BY u.user_id ASC
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PatientModel patient = new PatientModel();
                patient.setPatientId(rs.getInt("user_id"));
                patient.setName(rs.getString("name"));
                patient.setEmail(rs.getString("email"));
                patient.setPhoneNumber(rs.getString("phoneNumber"));
                patient.setBirthday(rs.getString("birthday"));
                patient.setCreatedAt(rs.getString("created_at"));
                patient.setLocation(""); // Location not stored in DB yet
                patient.setAge(patient.calculateAge());
                patients.add(patient);
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in getAllPatients: " + e.getMessage());
            e.printStackTrace();
        }

        return patients;
    }

    /**
     * Get patient by ID with location
     */
    public PatientModel getPatientById(int patientId) {
        String sql = """
        SELECT u.user_id, u.name, u.email, u.phoneNumber, u.birthday, u.created_at, u.location
        FROM users u
        INNER JOIN patients p ON u.user_id = p.patient_id
        WHERE u.user_id = ? AND u.role = 'PATIENT'
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PatientModel patient = new PatientModel();
                    patient.setPatientId(rs.getInt("user_id"));
                    patient.setName(rs.getString("name"));
                    patient.setEmail(rs.getString("email"));
                    patient.setPhoneNumber(rs.getString("phoneNumber"));
                    patient.setBirthday(rs.getString("birthday"));
                    patient.setCreatedAt(rs.getString("created_at"));
                    patient.setLocation(rs.getString("location")); // Location not stored in DB yet
                    patient.setAge(patient.calculateAge());
                    return patient;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getPatientById: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Update patient information with location
     */
    public boolean updatePatient(int patientId, String name, String email, String phoneNumber, String birthday, String location) {
        String sql = "UPDATE users SET name = ?, email = ?, phoneNumber = ?, birthday = ?, location = ? WHERE user_id = ? AND role = 'PATIENT'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phoneNumber);
            ps.setString(4, birthday);
            ps.setInt(5, patientId);
            ps.setString(6, location);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Patient updated successfully: " + name);
                return true;
            } else {
                System.err.println("No patient found with ID: " + patientId);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in updatePatient: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePatient(int patientId, String name, String email, String phoneNumber, String birthday) {
        return updatePatient(patientId, name, email, phoneNumber, birthday, "");
    }


    /**
     * Delete patient (CASCADE will handle patients table)
     */
    public boolean deletePatientById(int patientId) {
        String sql = "DELETE FROM users WHERE user_id = ? AND role = 'PATIENT'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in deletePatientById: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ========================================
    // DOCTOR CRUD (from your existing code)
    // ========================================

    /**
     * Register a new doctor
     */
    public boolean registerDoctor(String name, String email, String password, String phoneNumber,
                                  String birthday, String specialization, String biography, String address) {
        String insertUser = "INSERT INTO users (name, email, phoneNumber, birthday, password_hash, role) VALUES (?, ?, ?, ?, ?, 'DOCTOR')";
        String insertDoctor = "INSERT INTO doctors (doctor_id, specialization, biography, address) VALUES (?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            int userId;

            // Insert into users table
            try (PreparedStatement ps = conn.prepareStatement(insertUser, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, phoneNumber);
                ps.setString(4, birthday);
                ps.setString(5, password);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Insert into doctors table
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
            try { conn.rollback(); } catch (SQLException ignored) {}
            System.err.println("SQL Error in registerDoctor: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /**
     * Get all doctors
     */
    public List<DoctorModel> getAllDoctors() {
        List<DoctorModel> doctors = new ArrayList<>();

        String sql = """
            SELECT u.user_id, u.name, u.email, u.phoneNumber, u.birthday,
                   d.specialization, d.biography, d.address
            FROM users u
            INNER JOIN doctors d ON u.user_id = d.doctor_id
            WHERE u.role = 'DOCTOR'
            ORDER BY u.user_id ASC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DoctorModel doctor = new DoctorModel();
                doctor.id = rs.getInt("user_id");
                doctor.name = rs.getString("name");
                doctor.email = rs.getString("email");
                doctor.phone = rs.getString("phoneNumber");
                doctor.birthday = rs.getString("birthday");
                doctor.specialization = rs.getString("specialization");
                doctor.biography = rs.getString("biography");
                doctor.address = rs.getString("address");
                doctors.add(doctor);
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in getAllDoctors: " + e.getMessage());
            e.printStackTrace();
        }

        return doctors;
    }

    /**
     * Get doctor by ID
     */
    public DoctorModel getDoctorById(int doctorId) {
        String sql = """
            SELECT u.user_id, u.name, u.email, u.phoneNumber, u.birthday,
                   d.specialization, d.biography, d.address
            FROM users u
            INNER JOIN doctors d ON u.user_id = d.doctor_id
            WHERE u.user_id = ? AND u.role = 'DOCTOR'
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DoctorModel doctor = new DoctorModel();
                    doctor.id = rs.getInt("user_id");
                    doctor.name = rs.getString("name");
                    doctor.email = rs.getString("email");
                    doctor.phone = rs.getString("phoneNumber");
                    doctor.birthday = rs.getString("birthday");
                    doctor.specialization = rs.getString("specialization");
                    doctor.biography = rs.getString("biography");
                    doctor.address = rs.getString("address");
                    return doctor;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getDoctorById: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Update doctor information
     */
    public boolean updateDoctor(int doctorId, String name, String email, String phoneNumber,
                                String specialization, String biography, String address) {
        String updateUser = "UPDATE users SET name = ?, email = ?, phoneNumber = ? WHERE user_id = ? AND role = 'DOCTOR'";
        String updateDoctor = "UPDATE doctors SET specialization = ?, biography = ?, address = ? WHERE doctor_id = ?";

        try {
            conn.setAutoCommit(false);

            // Update users table
            try (PreparedStatement ps = conn.prepareStatement(updateUser)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, phoneNumber);
                ps.setInt(4, doctorId);

                int rows = ps.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Update doctors table
            try (PreparedStatement ps = conn.prepareStatement(updateDoctor)) {
                ps.setString(1, specialization);
                ps.setString(2, biography);
                ps.setString(3, address);
                ps.setInt(4, doctorId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            System.err.println("SQL Error in updateDoctor: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /**
     * Delete doctor
     */
    public boolean deleteDoctorById(int doctorId) {
        String sql = "DELETE FROM users WHERE user_id = ? AND role = 'DOCTOR'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in deleteDoctorById: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get doctors by specialization
     */
    public List<DoctorModel> getDoctorsBySpecialization(String specialization) {
        List<DoctorModel> doctors = new ArrayList<>();

        String sql = """
            SELECT u.user_id, u.name, u.email, u.phoneNumber, u.birthday,
                   d.specialization, d.biography, d.address
            FROM users u
            INNER JOIN doctors d ON u.user_id = d.doctor_id
            WHERE u.role = 'DOCTOR' AND d.specialization = ?
            ORDER BY u.name ASC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, specialization);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DoctorModel doctor = new DoctorModel();
                    doctor.id = rs.getInt("user_id");
                    doctor.name = rs.getString("name");
                    doctor.email = rs.getString("email");
                    doctor.phone = rs.getString("phoneNumber");
                    doctor.birthday = rs.getString("birthday");
                    doctor.specialization = rs.getString("specialization");
                    doctor.biography = rs.getString("biography");
                    doctor.address = rs.getString("address");
                    doctors.add(doctor);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getDoctorsBySpecialization: " + e.getMessage());
            e.printStackTrace();
        }

        return doctors;
    }

    // ========================================
    // DOCTOR-PATIENT ASSIGNMENT
    // ========================================

    /**
     * Assign a patient to a doctor
     */
    public boolean assignPatientToDoctor(int doctorId, int patientId) {
        String sql = "INSERT INTO doctor_patient_assignment (doctor_id, patient_id) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, patientId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in assignPatientToDoctor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove patient assignment from doctor
     */
    public boolean removePatientAssignment(int doctorId, int patientId) {
        String sql = "DELETE FROM doctor_patient_assignment WHERE doctor_id = ? AND patient_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, patientId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in removePatientAssignment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all patients assigned to a doctor
     */
    public List<PatientModel> getDoctorPatients(int doctorId) {
        List<PatientModel> patients = new ArrayList<>();

        String sql = """
            SELECT u.user_id, u.name, u.email, u.phoneNumber, u.birthday, u.created_at
            FROM users u
            INNER JOIN patients p ON u.user_id = p.patient_id
            INNER JOIN doctor_patient_assignment dpa ON p.patient_id = dpa.patient_id
            WHERE dpa.doctor_id = ? AND u.role = 'PATIENT'
            ORDER BY u.name ASC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PatientModel patient = new PatientModel();
                    patient.setPatientId(rs.getInt("user_id"));
                    patient.setName(rs.getString("name"));
                    patient.setEmail(rs.getString("email"));
                    patient.setPhoneNumber(rs.getString("phoneNumber"));
                    patient.setBirthday(rs.getString("birthday"));
                    patient.setCreatedAt(rs.getString("created_at"));
                    patients.add(patient);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getDoctorPatients: " + e.getMessage());
            e.printStackTrace();
        }

        return patients;
    }

    /**
     * Get all doctors assigned to a patient
     */
    public List<DoctorModel> getPatientDoctors(int patientId) {
        List<DoctorModel> doctors = new ArrayList<>();

        String sql = """
            SELECT u.user_id, u.name, u.email, u.phoneNumber, u.birthday,
                   d.specialization, d.biography, d.address
            FROM users u
            INNER JOIN doctors d ON u.user_id = d.doctor_id
            INNER JOIN doctor_patient_assignment dpa ON d.doctor_id = dpa.doctor_id
            WHERE dpa.patient_id = ? AND u.role = 'DOCTOR'
            ORDER BY u.name ASC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DoctorModel doctor = new DoctorModel();
                    doctor.id = rs.getInt("user_id");
                    doctor.name = rs.getString("name");
                    doctor.email = rs.getString("email");
                    doctor.phone = rs.getString("phoneNumber");
                    doctor.birthday = rs.getString("birthday");
                    doctor.specialization = rs.getString("specialization");
                    doctor.biography = rs.getString("biography");
                    doctor.address = rs.getString("address");
                    doctors.add(doctor);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getPatientDoctors: " + e.getMessage());
            e.printStackTrace();
        }

        return doctors;
    }

    // ========================================
    // PATIENT ATTACHMENTS
    // ========================================
    /**
     * Upload a patient attachment
     */
    public boolean uploadPatientAttachment(int patientId, int uploadedByDoctorId, String fileName,
                                           String fileType, byte[] fileData) {
        String sql = "INSERT INTO patient_attachments (patient_id, uploaded_by, file_name, file_type, file_size, file_data) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, uploadedByDoctorId);
            ps.setString(3, fileName);
            ps.setString(4, fileType);
            ps.setLong(5, fileData.length);
            ps.setBytes(6, fileData);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in uploadPatientAttachment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all attachments for a patient (without file data for performance)
     */
    public List<PatientAttachmentModel> getPatientAttachments(int patientId) {
        List<PatientAttachmentModel> attachments = new ArrayList<>();

        String sql = """
        SELECT pa.attachment_id, pa.patient_id, pa.uploaded_by, pa.file_name, 
               pa.file_type, pa.file_size, pa.uploaded_at,
               p_user.name as patient_name, d_user.name as doctor_name
        FROM patient_attachments pa
        INNER JOIN patients p ON pa.patient_id = p.patient_id
        INNER JOIN users p_user ON p.patient_id = p_user.user_id
        INNER JOIN doctors d ON pa.uploaded_by = d.doctor_id
        INNER JOIN users d_user ON d.doctor_id = d_user.user_id
        WHERE pa.patient_id = ?
        ORDER BY pa.uploaded_at DESC
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PatientAttachmentModel attachment = new PatientAttachmentModel();
                    attachment.setAttachmentId(rs.getInt("attachment_id"));
                    attachment.setPatientId(rs.getInt("patient_id"));
                    attachment.setUploadedBy(rs.getInt("uploaded_by"));
                    attachment.setFileName(rs.getString("file_name"));
                    attachment.setFileType(rs.getString("file_type"));
                    attachment.setFileSize(rs.getLong("file_size"));
                    attachment.setUploadedAt(rs.getString("uploaded_at"));
                    attachment.setPatientName(rs.getString("patient_name"));
                    attachment.setDoctorName(rs.getString("doctor_name"));
                    attachments.add(attachment);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getPatientAttachments: " + e.getMessage());
            e.printStackTrace();
        }

        return attachments;
    }

    /**
     * Get a specific attachment with file data for download
     */
    public PatientAttachmentModel getPatientAttachmentById(int attachmentId) {
        String sql = """
        SELECT pa.attachment_id, pa.patient_id, pa.uploaded_by, pa.file_name, 
               pa.file_type, pa.file_size, pa.file_data, pa.uploaded_at,
               p_user.name as patient_name, d_user.name as doctor_name
        FROM patient_attachments pa
        INNER JOIN patients p ON pa.patient_id = p.patient_id
        INNER JOIN users p_user ON p.patient_id = p_user.user_id
        INNER JOIN doctors d ON pa.uploaded_by = d.doctor_id
        INNER JOIN users d_user ON d.doctor_id = d_user.user_id
        WHERE pa.attachment_id = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attachmentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PatientAttachmentModel attachment = new PatientAttachmentModel();
                    attachment.setAttachmentId(rs.getInt("attachment_id"));
                    attachment.setPatientId(rs.getInt("patient_id"));
                    attachment.setUploadedBy(rs.getInt("uploaded_by"));
                    attachment.setFileName(rs.getString("file_name"));
                    attachment.setFileType(rs.getString("file_type"));
                    attachment.setFileSize(rs.getLong("file_size"));
                    attachment.setFileData(rs.getBytes("file_data"));
                    attachment.setUploadedAt(rs.getString("uploaded_at"));
                    attachment.setPatientName(rs.getString("patient_name"));
                    attachment.setDoctorName(rs.getString("doctor_name"));
                    return attachment;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getPatientAttachmentById: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Delete a patient attachment
     */
    public boolean deletePatientAttachment(int attachmentId) {
        String sql = "DELETE FROM patient_attachments WHERE attachment_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, attachmentId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in deletePatientAttachment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // ========================================
    // AUDIT LOGS
    // ========================================

    /**
     * Create an audit log entry
     */
    public boolean createAuditLog(int userId, String action, int patientId, String recordType, int recordId) {
        String sql = "INSERT INTO audit_logs (user_id, action, patient_id, record_type, record_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, action);

            if (patientId > 0) {
                ps.setInt(3, patientId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setString(4, recordType);

            if (recordId > 0) {
                ps.setInt(5, recordId);
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in createAuditLog: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get audit logs for a specific patient
     */
    public List<AuditLogModel> getAuditLogsByPatient(int patientId) {
        List<AuditLogModel> logs = new ArrayList<>();

        String sql = """
            SELECT al.log_id, al.user_id, al.action, al.patient_id, al.record_type, al.record_id, al.timestamp,
                   u.name as user_name, p_user.name as patient_name
            FROM audit_logs al
            INNER JOIN users u ON al.user_id = u.user_id
            LEFT JOIN patients p ON al.patient_id = p.patient_id
            LEFT JOIN users p_user ON p.patient_id = p_user.user_id
            WHERE al.patient_id = ?
            ORDER BY al.timestamp DESC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLogModel log = new AuditLogModel();
                    log.setLogId(rs.getInt("log_id"));
                    log.setUserId(rs.getInt("user_id"));
                    log.setAction(rs.getString("action"));
                    log.setPatientId(rs.getInt("patient_id"));
                    log.setRecordType(rs.getString("record_type"));
                    log.setRecordId(rs.getInt("record_id"));
                    log.setTimestamp(rs.getString("timestamp"));
                    log.setUserName(rs.getString("user_name"));
                    log.setPatientName(rs.getString("patient_name"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getAuditLogsByPatient: " + e.getMessage());
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Get all audit logs (admin view)
     */
    public List<AuditLogModel> getAllAuditLogs() {
        List<AuditLogModel> logs = new ArrayList<>();

        String sql = """
            SELECT al.log_id, al.user_id, al.action, al.patient_id, al.record_type, al.record_id, al.timestamp,
                   u.name as user_name, p_user.name as patient_name
            FROM audit_logs al
            INNER JOIN users u ON al.user_id = u.user_id
            LEFT JOIN patients p ON al.patient_id = p.patient_id
            LEFT JOIN users p_user ON p.patient_id = p_user.user_id
            ORDER BY al.timestamp DESC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AuditLogModel log = new AuditLogModel();
                log.setLogId(rs.getInt("log_id"));
                log.setUserId(rs.getInt("user_id"));
                log.setAction(rs.getString("action"));
                log.setPatientId(rs.getInt("patient_id"));
                log.setRecordType(rs.getString("record_type"));
                log.setRecordId(rs.getInt("record_id"));
                log.setTimestamp(rs.getString("timestamp"));
                log.setUserName(rs.getString("user_name"));
                log.setPatientName(rs.getString("patient_name"));
                logs.add(log);
            }

        } catch (SQLException e) {
            System.err.println("SQL Error in getAllAuditLogs: " + e.getMessage());
            e.printStackTrace();
        }

        return logs;
    }
}