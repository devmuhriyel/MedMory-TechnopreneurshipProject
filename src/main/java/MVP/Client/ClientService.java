package MVP.Client;

import java.sql.*;

public class ClientService {

    private static final String URL = "jdbc:mysql://localhost:3306/medical";
    private static final String USER = "root";
    private static final String PASS = "";

    public static ClientModel getClientInfo(int userID) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);

            String sql = "SELECT idusers, name FROM users WHERE idusers = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userID);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new ClientModel(
                        rs.getInt("idusers"),
                        rs.getString("name")

                );
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
