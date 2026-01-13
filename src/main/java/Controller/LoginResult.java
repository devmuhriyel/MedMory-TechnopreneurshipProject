package Controller;

public class LoginResult {
    public String role;   // "doctor", "client", "invalid", or "error"
    public int accountID;
    public int userID;

    // Constructor for errors/invalid status
    public LoginResult(String role) {
        this.role = role;
        this.accountID = -1;
        this.userID = -1;
    }

    // Constructor for successful login
    public LoginResult(String role, int accountID, int userID) {
        this.role = role;
        this.accountID = accountID;
        this.userID = userID;
    }
}