package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/medical_system";
    private static final String USER = "root";
    private static final String PASS = ""; // Default XAMPP password is empty

    public static Connection getConnection() {
        try {
            // Explicitly load the MySQL driver for Java 21 compatibility
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found!");
            return null;
        } catch (SQLException e) {
            System.err.println("Connection failed! Check if MySQL is running in XAMPP.");
            return null;
        }
    }
}