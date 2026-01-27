package CommonModel;

// DoctorPatientAssignmentModel.java - Doctor-Patient assignment relationship

public class DoctorPatientAssignmentModel {
    private int doctorId;
    private int patientId;
    private String assignedAt;

    // Additional fields for joined data
    private String doctorName;
    private String patientName;
    private String doctorSpecialization;

    // Constructors
    public DoctorPatientAssignmentModel() {
    }

    public DoctorPatientAssignmentModel(int doctorId, int patientId) {
        this.doctorId = doctorId;
        this.patientId = patientId;
    }

    public DoctorPatientAssignmentModel(int doctorId, int patientId, String assignedAt) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.assignedAt = assignedAt;
    }

    // Getters and Setters
    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(String assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    @Override
    public String toString() {
        return "DoctorPatientAssignmentModel{" +
                "doctorId=" + doctorId +
                ", patientId=" + patientId +
                ", assignedAt='" + assignedAt + '\'' +
                '}';
    }
}
