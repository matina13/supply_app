package org.example.demo;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Validate input
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            showError(request, response, "Email and password are required.");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT username, password, role FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password");
                        if (BCrypt.checkpw(password, storedHash)) {
                            if (rs.getString("role").equals("user") || rs.getString("role").equals("plus")) {
                                // User login
                                HttpSession session = request.getSession();
                                session.setAttribute("username", rs.getString("username"));
                                session.setAttribute("email", email);
                                session.setAttribute("role", rs.getString("role"));
                                response.sendRedirect("dashboard.jsp");
                            } else if (rs.getString("role").equals("admin")) {
                                // Admin login
                                HttpSession session = request.getSession();
                                session.setAttribute("username", rs.getString("username"));
                                session.setAttribute("role", "admin");
                                response.sendRedirect("adminDashboard.jsp");
                            }
                        } else {
                            showError(request, response, "Incorrect password.");
                        }
                    } else {
                        showError(request, response, "No account found for that email.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Ideally, log to a logger
            showError(request, response, "Internal server error. Please try again later.");
        }
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("email", request.getParameter("email")); // Keep email in form
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    public static void alreadyLoggedIn(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        try {
            if (session.getAttribute("role").equals("user") || session.getAttribute("role").equals("plus")) response.sendRedirect("dashboard.jsp");
            else if (session.getAttribute("role").equals("admin")) response.sendRedirect("adminDashboard.jsp");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
