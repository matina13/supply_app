package org.example.demo;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/UserManagementServlet")
public class UserManagementServlet extends HttpServlet {

    // Simple country to continent coordinates mapping (same as RegisterServlet)
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

    // User class to hold user data
    public static class User {
        private int id;
        private String username;
        private String email;
        private String role;

        public User(int id, String username, String email, String role) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
        }

        // Getters
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check admin authorization
        HttpSession session = request.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            handleDeleteUser(request, response);
        } else if ("edit".equals(action)) {
            handleGetUserForEdit(request, response);
        } else {
            // Default: get all users and forward to admin dashboard
            getAllUsers(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check admin authorization
        HttpSession session = request.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("add".equals(action)) {
            handleAddUser(request, response);
        } else if ("update".equals(action)) {
            handleUpdateUser(request, response);
        }
    }

    private void getAllUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<User> users = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT id, username, email, role FROM users ORDER BY id";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading users: " + e.getMessage());
        }

        request.setAttribute("users", users);
        request.getRequestDispatcher("adminDashboard.jsp").forward(request, response);
    }

    private void handleAddUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String countryCode = request.getParameter("country");

        // Basic validation
        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                role == null || role.trim().isEmpty() ||
                countryCode == null || countryCode.trim().isEmpty()) {
            request.setAttribute("error", "All fields are required");
            getAllUsers(request, response);
            return;
        }

        // Password validation
        if (password.length() < 8) {
            request.setAttribute("error", "Password must be at least 8 characters");
            getAllUsers(request, response);
            return;
        }

        // Country validation
        int[] coordinates = COUNTRY_COORDINATES.get(countryCode);
        if (coordinates == null) {
            request.setAttribute("error", "Invalid country selection");
            getAllUsers(request, response);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            // Use transaction for data consistency
            conn.setAutoCommit(false);

            try {
                // Check if username already exists
                String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();

                if (rs.getInt(1) > 0) {
                    conn.rollback();
                    request.setAttribute("error", "Username already exists");
                    getAllUsers(request, response);
                    return;
                }

                // Check if email already exists
                String checkEmailSql = "SELECT COUNT(*) FROM users WHERE email = ?";
                PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailSql);
                checkEmailStmt.setString(1, email);
                ResultSet emailRs = checkEmailStmt.executeQuery();
                emailRs.next();

                if (emailRs.getInt(1) > 0) {
                    conn.rollback();
                    request.setAttribute("error", "Email already exists");
                    getAllUsers(request, response);
                    return;
                }

                // Hash the password with BCrypt
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                // Insert new user
                String sql = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, hashedPassword);
                stmt.setString(4, role);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    int id = DBUtil.getRegisteredUserId(conn, email);
                    if (id != -1) {
                        // Insert UserData with location coordinates
                        String sql2 = "INSERT INTO userdata(user_id, money, last_date, location_x, location_y) VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement stmt2 = conn.prepareStatement(sql2);
                        stmt2.setInt(1, id);
                        stmt2.setInt(2, 10000); // Starting money
                        stmt2.setDate(3, Date.valueOf(LocalDate.of(2025, 5, 1))); // Starting date
                        stmt2.setInt(4, coordinates[0]); // location_x
                        stmt2.setInt(5, coordinates[1]); // location_y
                        stmt2.executeUpdate();

                        // Commit transaction
                        conn.commit();
                        request.setAttribute("success", "User added successfully with location data");
                    } else {
                        conn.rollback();
                        request.setAttribute("error", "Failed to retrieve user ID after creation");
                    }
                } else {
                    conn.rollback();
                    request.setAttribute("error", "Failed to add user");
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Database error: " + e.getMessage());
        }

        getAllUsers(request, response);
    }

    private void handleUpdateUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String password = request.getParameter("password");

        if (idStr == null || username == null || email == null || role == null) {
            request.setAttribute("error", "Missing required fields");
            getAllUsers(request, response);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql;
            PreparedStatement stmt;

            if (password != null && !password.trim().isEmpty()) {
                // Validate password length
                if (password.length() < 8) {
                    request.setAttribute("error", "Password must be at least 8 characters");
                    getAllUsers(request, response);
                    return;
                }

                // Hash the new password
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                // Update with new password
                sql = "UPDATE users SET username = ?, email = ?, role = ?, password = ? WHERE id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, role);
                stmt.setString(4, hashedPassword);
                stmt.setInt(5, Integer.parseInt(idStr));
            } else {
                // Update without changing password
                sql = "UPDATE users SET username = ?, email = ?, role = ? WHERE id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, role);
                stmt.setInt(4, Integer.parseInt(idStr));
            }

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                request.setAttribute("success", "User updated successfully");
            } else {
                request.setAttribute("error", "Failed to update user");
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("error", "Error updating user: " + e.getMessage());
        }

        getAllUsers(request, response);
    }

    private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");

        if (idStr == null) {
            request.setAttribute("error", "User ID is required");
            getAllUsers(request, response);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);

            int userId = Integer.parseInt(idStr);

            try {
                // Delete from related tables first (due to foreign key constraints)

                // Delete from ProducerInventory
                String sql1 = "DELETE FROM ProducerInventory WHERE user_id = ?";
                PreparedStatement stmt1 = conn.prepareStatement(sql1);
                stmt1.setInt(1, userId);
                stmt1.executeUpdate();

                // Delete from Transactions
                String sql2 = "DELETE FROM Transactions WHERE user_id = ?";
                PreparedStatement stmt2 = conn.prepareStatement(sql2);
                stmt2.setInt(1, userId);
                stmt2.executeUpdate();

                // Delete from Transit (where buyer_id = user_id)
                String sql3 = "DELETE FROM Transit WHERE buyer_id = ?";
                PreparedStatement stmt3 = conn.prepareStatement(sql3);
                stmt3.setInt(1, userId);
                stmt3.executeUpdate();

                // Delete from UserData
                String sql4 = "DELETE FROM userdata WHERE user_id = ?";
                PreparedStatement stmt4 = conn.prepareStatement(sql4);
                stmt4.setInt(1, userId);
                stmt4.executeUpdate();

                // Finally delete from users table
                String sql5 = "DELETE FROM users WHERE id = ?";
                PreparedStatement stmt5 = conn.prepareStatement(sql5);
                stmt5.setInt(1, userId);
                int rowsAffected = stmt5.executeUpdate();

                if (rowsAffected > 0) {
                    conn.commit(); // Commit transaction
                    request.setAttribute("success", "User and all related data deleted successfully");
                } else {
                    conn.rollback(); // Rollback transaction
                    request.setAttribute("error", "Failed to delete user");
                }

            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                throw e;
            } finally {
                conn.setAutoCommit(true); // Reset auto-commit
            }

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("error", "Error deleting user: " + e.getMessage());
        }

        getAllUsers(request, response);
    }

    private void handleGetUserForEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");

        if (idStr == null) {
            getAllUsers(request, response);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT id, username, email, role FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(idStr));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("role")
                );
                request.setAttribute("editUser", user);
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading user: " + e.getMessage());
        }

        getAllUsers(request, response);
    }
}