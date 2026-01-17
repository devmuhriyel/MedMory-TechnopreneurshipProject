package MVP.Client;

import java.sql.*;

public class ClientService {
    private static final String URL = "jdbc:mysql://localhost:3306/medical";
    private static final String USER = "root";
    private static final String PASS = "";

    public static ClientModel getClientInfo(int userID) {
        ClientModel client = null;
        String sql = "SELECT idusers, name, last_name, email, phone, birthday, location FROM users WHERE idusers = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                client = new ClientModel();
                client.id = rs.getInt("idusers");
                client.name = rs.getString("name");
                client.lastName = rs.getString("last_name");
                client.email = rs.getString("email");
                client.phoneNumber = rs.getString("phone");
                client.birthday = rs.getString("birthday");
                client.location = rs.getString("location");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return client;
    }

    public static boolean updatePassword(int userID, String currentPass, String newPass) {
        // IMPORTANT: Verify that 'password' and 'idusers' match your actual DB column names
        String verifySql = "SELECT idusers FROM users WHERE idusers = ? AND password = ?";
        String updateSql = "UPDATE users SET password = ? WHERE idusers = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            // 1. Verify Current Password
            try (PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {
                verifyStmt.setInt(1, userID);
                verifyStmt.setString(2, currentPass);
                ResultSet rs = verifyStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("❌ Verification Failed: Current password does not match for ID " + userID);
                    return false;
                }
            }

            // 2. Perform the Update
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newPass);
                updateStmt.setInt(2, userID);
                int affectedRows = updateStmt.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("✅ Database Updated: Password changed for ID " + userID);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}