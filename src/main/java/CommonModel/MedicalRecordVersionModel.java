package CommonModel;

// MedicalRecordVersionModel.java - Version history for medical records

public class MedicalRecordVersionModel {
    private int versionId;
    private int recordId;
    private String content;
    private int updatedBy;      // doctor_id who made the update
    private String updatedAt;

    // Additional field for joined data
    private String doctorName;

    // Constructors
    public MedicalRecordVersionModel() {
    }

    public MedicalRecordVersionModel(int versionId, int recordId, String content, int updatedBy) {
        this.versionId = versionId;
        this.recordId = recordId;
        this.content = content;
        this.updatedBy = updatedBy;
    }

    // Getters and Setters
    public int getVersionId() {
        return versionId;
    }

    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    @Override
    public String toString() {
        return "MedicalRecordVersionModel{" +
                "versionId=" + versionId +
                ", recordId=" + recordId +
                ", updatedBy=" + updatedBy +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
