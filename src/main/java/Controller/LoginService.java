package Controller;

import CommonModel.SessionManager;
import java.sql.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class LoginService {

    private static final String URL = "jdbc:mysql://localhost:3306/medical";
    private static final String USER = "root";
    private static final String PASS = "";

    /**
     * Converts a plain text password into a SHA-256 hash.
     * No external library required.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            System.err.println("Hashing Error: " + e.getMessage());
            return null;
        }
    }

    public static LoginResult validateCredentials(String username, String password) {
        // Hash the user's input immediately so we can compare it to the DB
        String hashedInput = hashPassword(password);

        if (hashedInput == null) {
            return new LoginResult("error", -1, -1);
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

                // 1. Check DOCTOR login
                String doctorQuery = "SELECT iddoctors FROM doctors WHERE name = ? AND password = ?";
                try (PreparedStatement ds = conn.prepareStatement(doctorQuery)) {
                    ds.setString(1, username);
                    ds.setString(2, hashedInput);
                    ResultSet dr = ds.executeQuery();

                    if (dr.next()) {
                        int doctorID = dr.getInt("iddoctors");
                        SessionManager.setDoctorSession(doctorID, -1);
                        return new LoginResult("doctor", doctorID, -1);
                    }
                }

                // 2. Check CLIENT login
                String clientQuery = "SELECT idusers FROM users WHERE name = ? AND password = ?";
                try (PreparedStatement cs = conn.prepareStatement(clientQuery)) {
                    cs.setString(1, username);
                    cs.setString(2, hashedInput);
                    ResultSet cr = cs.executeQuery();

                    if (cr.next()) {
                        int userID = cr.getInt("idusers");
                        SessionManager.setClientSession(userID);
                        System.out.println("Client logged in with ID: " + userID);
                        return new LoginResult("client", userID, userID);
                    }
                }
            }

            return new LoginResult("invalid", -1, -1);

        } catch (Exception e) {
            e.printStackTrace();
            return new LoginResult("error", -1, -1);
        }
    }
}