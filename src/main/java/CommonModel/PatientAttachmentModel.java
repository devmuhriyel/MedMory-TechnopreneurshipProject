package CommonModel;

// PatientAttachmentModel.java - File attachments for patients (X-rays, lab results, etc.)

public class PatientAttachmentModel {
    private int attachmentId;
    private int patientId;
    private int uploadedBy;     // doctor_id who uploaded
    private String filePath;
    private String uploadedAt;

    // Additional fields for joined data
    private String patientName;
    private String doctorName;
    private String fileName;    // Extracted from filePath

    // Constructors
    public PatientAttachmentModel() {
    }

    public PatientAttachmentModel(int attachmentId, int patientId, int uploadedBy, String filePath) {
        this.attachmentId = attachmentId;
        this.patientId = patientId;
        this.uploadedBy = uploadedBy;
        this.filePath = filePath;
    }

    // Getters and Setters
    public int getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(int attachmentId) {
        this.attachmentId = attachmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(int uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "PatientAttachmentModel{" +
                "attachmentId=" + attachmentId +
                ", patientId=" + patientId +
                ", uploadedBy=" + uploadedBy +
                ", filePath='" + filePath + '\'' +
                ", uploadedAt='" + uploadedAt + '\'' +
                '}';
    }
}
