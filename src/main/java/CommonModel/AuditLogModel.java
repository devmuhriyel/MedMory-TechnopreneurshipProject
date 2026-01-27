package CommonModel;

// AuditLogModel.java - Audit log for tracking user actions

public class AuditLogModel {
    private int logId;
    private int userId;
    private String action;
    private int patientId;
    private String recordType;
    private int recordId;
    private String timestamp;

    // Additional field for joined data
    private String userName;
    private String patientName;

    // Constructors
    public AuditLogModel() {
    }

    public AuditLogModel(int userId, String action, int patientId, String recordType, int recordId) {
        this.userId = userId;
        this.action = action;
        this.patientId = patientId;
        this.recordType = recordType;
        this.recordId = recordId;
    }

    // Getters and Setters
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    @Override
    public String toString() {
        return "AuditLogModel{" +
                "logId=" + logId +
                ", userId=" + userId +
                ", action='" + action + '\'' +
                ", patientId=" + patientId +
                ", recordType='" + recordType + '\'' +
                ", recordId=" + recordId +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
