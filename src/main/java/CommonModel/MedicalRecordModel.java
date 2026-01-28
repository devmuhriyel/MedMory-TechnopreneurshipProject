package CommonModel;

import java.time.LocalDateTime;

/**
 * Model class representing a medical record with complete SOAP documentation
 * Maps to the medical_records table in the database
 */
public class MedicalRecordModel {

    // Primary identifiers
    private int recordId;
    private int patientId;
    private int doctorId;

    // Patient information
    private String patientName;
    private int age;

    // SOAP - Subjective
    private String subjective;
    private String chiefComplaint;
    private String historyPresentIllness;

    // OPQRST Assessment
    private String onset;
    private String location;
    private String duration;
    private String characteristics;
    private String aggravatingFactors;
    private String radiation;
    private String severity;

    // Medical History
    private String pastMedicalIllness;
    private String medications;
    private String allergies;
    private String familyHistory;
    private String socialHistory;
    private String reviewOfSystems;

    // Metadata
    private LocalDateTime createdAt;

    // Joined display fields (from JOINs)
    private String doctorName;

    // Constructors
    public MedicalRecordModel() {}

    // Minimal constructor (for lists)
    public MedicalRecordModel(int recordId, int patientId, int doctorId, String subjective) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.subjective = subjective;
    }

    // Full constructor
    public MedicalRecordModel(int recordId, int patientId, String patientName, int age,
                              String subjective, String chiefComplaint, String historyPresentIllness,
                              String onset, String location, String duration, String characteristics,
                              String aggravatingFactors, String radiation, String severity,
                              String pastMedicalIllness, String medications, String allergies,
                              String familyHistory, String socialHistory, String reviewOfSystems,
                              int doctorId, LocalDateTime createdAt) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.age = age;
        this.subjective = subjective;
        this.chiefComplaint = chiefComplaint;
        this.historyPresentIllness = historyPresentIllness;
        this.onset = onset;
        this.location = location;
        this.duration = duration;
        this.characteristics = characteristics;
        this.aggravatingFactors = aggravatingFactors;
        this.radiation = radiation;
        this.severity = severity;
        this.pastMedicalIllness = pastMedicalIllness;
        this.medications = medications;
        this.allergies = allergies;
        this.familyHistory = familyHistory;
        this.socialHistory = socialHistory;
        this.reviewOfSystems = reviewOfSystems;
        this.doctorId = doctorId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSubjective() {
        return subjective;
    }

    public void setSubjective(String subjective) {
        this.subjective = subjective;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getHistoryPresentIllness() {
        return historyPresentIllness;
    }

    public void setHistoryPresentIllness(String historyPresentIllness) {
        this.historyPresentIllness = historyPresentIllness;
    }

    public String getOnset() {
        return onset;
    }

    public void setOnset(String onset) {
        this.onset = onset;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
    }

    public String getAggravatingFactors() {
        return aggravatingFactors;
    }

    public void setAggravatingFactors(String aggravatingFactors) {
        this.aggravatingFactors = aggravatingFactors;
    }

    public String getRadiation() {
        return radiation;
    }

    public void setRadiation(String radiation) {
        this.radiation = radiation;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getPastMedicalIllness() {
        return pastMedicalIllness;
    }

    public void setPastMedicalIllness(String pastMedicalIllness) {
        this.pastMedicalIllness = pastMedicalIllness;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getFamilyHistory() {
        return familyHistory;
    }

    public void setFamilyHistory(String familyHistory) {
        this.familyHistory = familyHistory;
    }

    public String getSocialHistory() {
        return socialHistory;
    }

    public void setSocialHistory(String socialHistory) {
        this.socialHistory = socialHistory;
    }

    public String getReviewOfSystems() {
        return reviewOfSystems;
    }

    public void setReviewOfSystems(String reviewOfSystems) {
        this.reviewOfSystems = reviewOfSystems;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // For backward compatibility with String createdAt
    public void setCreatedAt(String createdAt) {
        if (createdAt != null && !createdAt.isEmpty()) {
            try {
                this.createdAt = LocalDateTime.parse(createdAt.replace(" ", "T"));
            } catch (Exception e) {
                // If parsing fails, try other formats or set to null
                this.createdAt = null;
            }
        }
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    // Backward compatibility methods
    public String getNotes() {
        return subjective;
    }

    public void setNotes(String notes) {
        this.subjective = notes;
    }

    // Helper method to check if record has complete OPQRST data
    public boolean hasOPQRSTData() {
        return (onset != null && !onset.trim().isEmpty()) ||
                (location != null && !location.trim().isEmpty()) ||
                (duration != null && !duration.trim().isEmpty()) ||
                (characteristics != null && !characteristics.trim().isEmpty()) ||
                (aggravatingFactors != null && !aggravatingFactors.trim().isEmpty()) ||
                (radiation != null && !radiation.trim().isEmpty()) ||
                (severity != null && !severity.trim().isEmpty());
    }

    // Helper method to get formatted creation date
    public String getFormattedCreatedAt() {
        if (createdAt != null) {
            return createdAt.toString();
        }
        return "N/A";
    }

    @Override
    public String toString() {
        return "MedicalRecordModel{" +
                "recordId=" + recordId +
                ", patientId=" + patientId +
                ", patientName='" + patientName + '\'' +
                ", age=" + age +
                ", doctorId=" + doctorId +
                ", createdAt=" + createdAt +
                '}';
    }
}