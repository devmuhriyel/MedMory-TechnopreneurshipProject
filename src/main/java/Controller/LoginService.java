package Controller;

import CommonModel.SessionManager;
import java.sql.*;

public class LoginService {

    private static final String URL = "jdbc:mysql://localhost:3306/medical";
    private static final String USER = "root";
    private static final String PASS = "";

    public static LoginResult validateCredentials(String username, String password) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);

            // --------------------------
            // 1. Check DOCTOR login
            // --------------------------
            String doctorQuery = "SELECT * FROM doctors WHERE name = ? AND password = ?";
            PreparedStatement ds = conn.prepareStatement(doctorQuery);
            ds.setString(1, username);
            ds.setString(2, password);
            ResultSet dr = ds.executeQuery();

            if (dr.next()) {
                int doctorID = dr.getInt("iddoctors");

                // Doctors don't have userid unless you manually added it
                SessionManager.setDoctorSession(doctorID, -1);

                conn.close();
                return new LoginResult("doctor", doctorID, -1);
            }

            // --------------------------
            // 2. Check CLIENT login
            // --------------------------
            String clientQuery = "SELECT * FROM users WHERE name = ? AND password = ?";
            PreparedStatement cs = conn.prepareStatement(clientQuery);
            cs.setString(1, username);
            cs.setString(2, password);
            ResultSet cr = cs.executeQuery(); // FIXED BUG HERE

            if (cr.next()) {
                int userID = cr.getInt("idusers");
                SessionManager.setClientSession(userID);  // correct!
                System.out.println("Client logged in with ID: " + userID);
                System.out.println("SessionManager stored userID = " + SessionManager.getUserID());
                return new LoginResult("client", userID, userID);


            }

            conn.close();
            return new LoginResult("invalid", -1, -1);

        } catch (Exception e) {
            e.printStackTrace();
            return new LoginResult("error", -1, -1);
        }


    }

}
