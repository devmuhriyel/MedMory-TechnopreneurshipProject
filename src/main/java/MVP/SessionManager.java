package MVP;

public class SessionManager {

    private static int loggedInDoctorID = -1;
    private static int loggedInUserID = -1;
    private static String role = "";

    public static void setDoctorSession(int doctorID, int userID) {
        loggedInDoctorID = doctorID;
        loggedInUserID = userID;
        role = "doctor";
    }

    public static void setClientSession(int userID) {
        loggedInUserID = userID;
        role = "client";
    }

    public static int getDoctorID() {
        return loggedInDoctorID;
    }

    public static int getUserID() {
        return loggedInUserID;
    }

    public static String getRole() {
        return role;
    }

    public static void clearSession() {
        loggedInDoctorID = -1;
        loggedInUserID = -1;
        role = "";
    }
}
