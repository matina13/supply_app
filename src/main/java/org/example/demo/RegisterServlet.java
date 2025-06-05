package org.example.demo;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    // Simple country to continent coordinates mapping
    private static final Map<String, int[]> COUNTRY_COORDINATES = new HashMap<>();

    static {
        // North America (0, 0)
        String[] northAmerica = {"US", "CA", "MX", "GT", "CR", "PA", "CU", "JM"};
        for (String code : northAmerica) {
            COUNTRY_COORDINATES.put(code, new int[]{0, 0});
        }

        // South America (0, 1)
        String[] southAmerica = {"BR", "AR", "CL", "PE", "CO", "VE", "EC", "BO", "PY", "UY", "GY", "SR"};
        for (String code : southAmerica) {
            COUNTRY_COORDINATES.put(code, new int[]{0, 1});
        }

        // Europe (1, 0)
        String[] europe = {"GB", "DE", "FR", "IT", "ES", "NL", "BE", "CH", "AT", "SE", "NO", "DK",
                "FI", "PL", "CZ", "HU", "PT", "GR", "IE", "RO", "BG", "HR", "SK", "SI",
                "EE", "LV", "LT", "LU", "MT", "CY", "RU", "UA", "BY", "RS", "BA", "ME",
                "MK", "AL", "MD"};
        for (String code : europe) {
            COUNTRY_COORDINATES.put(code, new int[]{1, 0});
        }

        // Africa (1, 1)
        String[] africa = {"NG", "ET", "EG", "ZA", "KE", "UG", "DZ", "SD", "MA", "AO", "GH", "MZ",
                "MG", "CM", "CI", "NE", "BF", "ML", "MW", "ZM", "SO", "SN", "TD", "SL",
                "LY", "TN", "BW", "NA", "ZW", "TZ", "RW", "CG", "CD", "CF"};
        for (String code : africa) {
            COUNTRY_COORDINATES.put(code, new int[]{1, 1});
        }

        // Asia (2, 0)
        String[] asia = {"CN", "IN", "ID", "PK", "BD", "JP", "PH", "VN", "TR", "IR", "TH", "MM",
                "KR", "IQ", "AF", "SA", "UZ", "MY", "NP", "YE", "KP", "SY", "KH", "JO",
                "AZ", "AE", "TJ", "IL", "LA", "SG", "OM", "KW", "GE", "MN", "AM", "QA",
                "BH", "TL", "LB", "KG", "TM", "BT", "BN", "MV"};
        for (String code : asia) {
            COUNTRY_COORDINATES.put(code, new int[]{2, 0});
        }

        // Oceania (2, 1)
        String[] oceania = {"AU", "PG", "NZ", "FJ", "SB", "NC", "PF", "VU", "WS", "FM", "TO", "KI",
                "PW", "MH", "TV", "NR"};
        for (String code : oceania) {
            COUNTRY_COORDINATES.put(code, new int[]{2, 1});
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String countryCode = request.getParameter("country");

        // Country validation
        if (countryCode == null || countryCode.trim().isEmpty()) {
            showError(request, response, "Please select your country");
            return;
        }

        // Password validation
        if (password.length() < 8) {
            showError(request, response, "Password must be at least 8 characters");
            return;
        }

        // Get coordinates from country
        int[] coordinates = COUNTRY_COORDINATES.get(countryCode);
        if (coordinates == null) {
            showError(request, response, "Invalid country selection");
            return;
        }

        // Proper password hashing with BCrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = DBUtil.getConnection()) {
            // Check if email already exists
            if (emailExists(conn, email)) {
                showError(request, response, "Email already in use. Please use a different email.");
                return;
            }

            // Use transaction for data consistency
            conn.setAutoCommit(false);

            try {
                // Insert user
                String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, email);
                    stmt.setString(3, hashedPassword);

                    int rowsInserted = stmt.executeUpdate();

                    if (rowsInserted > 0) {
                        int userId = DBUtil.getRegisteredUserId(conn, email);
                        if (userId != -1) {
                            // Only initialize UserData with location coordinates
                            String sql2 = "INSERT INTO UserData (user_id, money, last_date, location_x, location_y) VALUES (?, ?, ?, ?, ?)";
                            try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                                stmt2.setInt(1, userId);
                                stmt2.setInt(2, 10000); // Starting money
                                stmt2.setDate(3, Date.valueOf(LocalDate.of(2025, 5, 1))); // Starting date
                                stmt2.setInt(4, coordinates[0]); // location_x
                                stmt2.setInt(5, coordinates[1]); // location_y
                                stmt2.executeUpdate();
                            }

                            // Commit transaction
                            conn.commit();

                            // Set session and redirect to dashboard
                            HttpSession session = request.getSession();
                            session.setAttribute("username", username);
                            session.setAttribute("email", email);
                            session.setAttribute("role", "user");

                            response.sendRedirect("dashboard.jsp");
                        } else {
                            conn.rollback();
                            showError(request, response, "Failed to retrieve user ID after registration.");
                        }
                    } else {
                        conn.rollback();
                        showError(request, response, "Registration failed. Please try again.");
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError(request, response, "Database error: " + e.getMessage());
        }
    }

    private boolean emailExists(Connection conn, String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}