package CommonModel;

// PatientAttachmentModel.java - File attachments for patients (X-rays, lab results, etc.)

public class PatientAttachmentModel {
    private int attachmentId;
    private int patientId;
    private int uploadedBy;     // doctor_id who uploaded
    private String fileName;
    private String fileType;    // MIME type (e.g., application/pdf, image/jpeg)
    private long fileSize;      // File size in bytes
    private byte[] fileData;    // Binary file content
    private String uploadedAt;

    // Additional fields for joined data
    private String patientName;
    private String doctorName;

    // Constructors
    public PatientAttachmentModel() {
    }

    public PatientAttachmentModel(int attachmentId, int patientId, int uploadedBy,
                                  String fileName, String fileType, long fileSize, byte[] fileData) {
        this.attachmentId = attachmentId;
        this.patientId = patientId;
        this.uploadedBy = uploadedBy;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileData = fileData;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
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

    /**
     * Get human-readable file size
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Get file extension from fileName
     */
    public String getFileExtension() {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    @Override
    public String toString() {
        return "PatientAttachmentModel{" +
                "attachmentId=" + attachmentId +
                ", patientId=" + patientId +
                ", uploadedBy=" + uploadedBy +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", uploadedAt='" + uploadedAt + '\'' +
                ", hasFileData=" + (fileData != null) +
                '}';
    }
}