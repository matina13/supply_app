package org.example.demo;

import java.sql.*;

public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/supply_app_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private static DBUtil INSTANCE;
    private static Connection db;

    // Static block to load the JDBC driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");  // Explicitly load the driver
            System.out.println("MySQL JDBC Driver loaded successfully!");
        } catch (Exception e) {
            System.err.println("Failed to load MySQL JDBC driver");
            e.printStackTrace();
            throw new RuntimeException("Cannot load JDBC driver", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (INSTANCE == null || returnConnection().isClosed()) {
            INSTANCE = new DBUtil();
            db = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return returnConnection();
    }

    private static Connection returnConnection() {
        return db;
    }

    public static int getRegisteredUserId(Connection conn, String email) {
        try {
            String sql = "SELECT id FROM users WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
}
