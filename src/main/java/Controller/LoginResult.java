package Controller;

public class LoginResult {
    private String status;      // "doctor", "patient", "admin", "invalid", "error"
    private int userId;
    private int patientId;
    private String userName;
    private String errorMessage;

    // Constructor for simple status
    public LoginResult(String status) {
        this.status = status;
        this.userId = -1;
        this.patientId = -1;
        this.userName = "";
        this.errorMessage = "";
    }

    // Constructor for status with error message
    public LoginResult(String status, String errorMessage) {
        this.status = status;
        this.userId = -1;
        this.patientId = -1;
        this.userName = "";
        this.errorMessage = errorMessage;
    }

    // Constructor for successful login (backward compatible)
    public LoginResult(String status, int userId, int patientId) {
        this.status = status;
        this.userId = userId;
        this.patientId = patientId;
        this.userName = "";
        this.errorMessage = "";
    }

    // Constructor for successful login with name
    public LoginResult(String status, int userId, int patientId, String userName) {
        this.status = status;
        this.userId = userId;
        this.patientId = patientId;
        this.userName = userName;
        this.errorMessage = "";
    }

    // Getters
    public String getStatus() {
        return status;
    }

    public int getUserId() {
        return userId;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getUserName() {
        return userName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // Convenience methods
    public boolean isSuccess() {
        return status.equals("doctor") || status.equals("patient") || status.equals("admin");
    }

    public boolean isDoctor() {
        return status.equals("doctor");
    }

    public boolean isPatient() {
        return status.equals("patient");
    }

    public boolean isAdmin() {
        return status.equals("admin");
    }

    public boolean isInvalid() {
        return status.equals("invalid");
    }

    public boolean isError() {
        return status.equals("error");
    }

    @Override
    public String toString() {
        return "LoginResult{" +
                "status='" + status + '\'' +
                ", userId=" + userId +
                ", patientId=" + patientId +
                ", userName='" + userName + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}