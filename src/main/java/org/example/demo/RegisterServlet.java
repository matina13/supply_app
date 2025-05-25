package org.example.demo;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Password validation
        if (password.length() < 8) {
            showError(request, response, "Password must be at least 8 characters");
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

            String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, hashedPassword);

                int rowsInserted = stmt.executeUpdate();

                if (rowsInserted > 0) {
                    HttpSession session = request.getSession();
                    session.setAttribute("username", username);
                    response.sendRedirect("dashboard.jsp");
                } else {
                    showError(request, response, "Registration failed. Please try again.");
                }
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