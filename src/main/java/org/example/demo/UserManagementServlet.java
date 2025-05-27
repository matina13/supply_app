package org.example.demo;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/UserManagementServlet")
public class UserManagementServlet extends HttpServlet {

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

        // Basic validation
        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                role == null || role.trim().isEmpty()) {
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

        try (Connection conn = DBUtil.getConnection()) {
            // Check if username already exists
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
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
                request.setAttribute("success", "User added successfully");

                int id = DBUtil.getRegisteredUserId(conn, email);
                if (id != -1) {
                    String sql2 = "INSERT INTO UserData (user_id, money, last_date) VALUES (?, ?, ?)";
                    PreparedStatement stmt2 = conn.prepareStatement(sql2);
                    stmt2.setInt(1, id);
                    stmt2.setInt(2, 10000);
                    stmt2.setDate(3, Date.valueOf(LocalDate.of(2025,5,1)));
                    stmt2.executeUpdate();
                }
            } else {
                request.setAttribute("error", "Failed to add user");
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
            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(idStr));

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                request.setAttribute("success", "User deleted successfully");
            } else {
                request.setAttribute("error", "Failed to delete user");
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