package Controller;

public class LoginResult {
    public String role;   // "doctor", "client", "invalid"
    public int accountID;
    public int userID;

    public LoginResult(String role, int accountID, int userID) {
        this.role = role;
        this.accountID = accountID;
        this.userID = userID;
    }
}
