package MVP.Doctor;

import java.sql.*;

public class DoctorService {

    private static final String URL = "jdbc:mysql://localhost:3306/medical";
    private static final String USER = "root";
    private static final String PASS = "";

    public static DoctorModel getDoctorInfo(int doctorID) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT d.name, d.specialization, d.email, d.phone, d.bio " +
                            "FROM doctors d WHERE d.iddoctors = ?"
            );
            stmt.setInt(1, doctorID);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new DoctorModel(
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("bio")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
