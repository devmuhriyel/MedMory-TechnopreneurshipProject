package Controller;

import CommonModel.SessionManager;
import DAO.DatabaseConnection;
import java.sql.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class LoginService {

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return null;
        }
    }

    public static LoginResult validateCredentials(String username, String password) {
        String hashedInput = hashPassword(password);
        if (hashedInput == null) return new LoginResult("error");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return new LoginResult("error");

            // 1. Check DOCTOR table
            String doctorQuery = "SELECT iddoctors FROM doctors WHERE name = ? AND password = ?";
            try (PreparedStatement ds = conn.prepareStatement(doctorQuery)) {
                ds.setString(1, username);
                ds.setString(2, hashedInput);
                ResultSet dr = ds.executeQuery();
                if (dr.next()) {
                    int id = dr.getInt("iddoctors");
                    SessionManager.setDoctorSession(id, -1);
                    return new LoginResult("doctor", id, -1);
                }
            }

            // 2. Check USERS table
            String clientQuery = "SELECT idusers FROM users WHERE name = ? AND password = ?";
            try (PreparedStatement cs = conn.prepareStatement(clientQuery)) {
                cs.setString(1, username);
                cs.setString(2, hashedInput);
                ResultSet cr = cs.executeQuery();
                if (cr.next()) {
                    int id = cr.getInt("idusers");
                    SessionManager.setClientSession(id);
                    return new LoginResult("client", id, id);
                }
            }
            // 2. Check USERS table
            String adminQuery = "SELECT idadmin, password FROM admin WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(adminQuery)) {
                ps.setString(1, username.trim());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String dbPassword = rs.getString("password");

                    // plaintext comparison
                    if (password.equals(dbPassword)) {
                        int id = rs.getInt("idadmin");
                        SessionManager.setClientSession(id);
                        return new LoginResult("admin", id, id);
                    }
                }
            }
            return new LoginResult("invalid");
        } catch (Exception e) {
            return new LoginResult("error");
        }
    }

    // New: Actually saves the user to your phpMyAdmin database
    public static boolean registerUser(String name, String plainPassword) {
        String hashed = hashPassword(plainPassword);
        String sql = "INSERT INTO users (name, password) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, hashed);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}