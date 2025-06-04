<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="org.example.demo.UserManagementServlet.User" %>
<%
    if (session == null) response.sendRedirect("/");
    else {
        if (session.getAttribute("role") == null || !session.getAttribute("role").equals("admin")) response.sendRedirect("/");
    }
    String username = (String) session.getAttribute("username");
    String role = (String) session.getAttribute("role");

    List<User> users = (List<User>) request.getAttribute("users");
    User editUser = (User) request.getAttribute("editUser");
    String error = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Dashboard</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Custom CSS -->
    <style>
        .topbar {
            background-color: #0c4a86;
            color: white;
            padding: 1rem;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        .topbar a:last-child {
            background-color: #bc0c0c;
        }
        .topbar a:last-child:hover {
            background-color: #a00;
        }
        .card {
            margin-bottom: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .actions .btn {
            padding: 0.25rem 0.5rem;
            font-size: 0.875rem;
        }
    </style>
</head>
<body>
<!-- Topbar (same style as original) -->
<div class="topbar mb-4">
    <div class="container-fluid d-flex justify-content-between align-items-center">
        <div class="title fw-bold fs-5">Welcome, <%= username %></div>
        <div class="nav">
            <a href="LogoutServlet" class="btn btn-sm text-white">Logout</a>
        </div>
    </div>
</div>

<div class="container-fluid">
    <h2 class="mb-4">Admin Dashboard</h2>

    <!-- Alerts -->
    <% if (error != null) { %>
    <div class="alert alert-danger alert-dismissible fade show">
        <%= error %>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <% } %>

    <% if (success != null) { %>
    <div class="alert alert-success alert-dismissible fade show">
        <%= success %>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <% } %>

    <div class="row">
        <div class="col-md-6">
            <!-- Add/Edit User Card -->
            <div class="card">
                <div class="card-header bg-primary text-white">
                    <h5 class="mb-0"><%= editUser != null ? "Edit User" : "Add New User" %></h5>
                </div>
                <div class="card-body">
                    <form method="post" action="UserManagementServlet">
                        <input type="hidden" name="action" value="<%= editUser != null ? "update" : "add" %>">
                        <% if (editUser != null) { %>
                        <input type="hidden" name="id" value="<%= editUser.getId() %>">
                        <% } %>

                        <div class="mb-3">
                            <label for="username" class="form-label">Username</label>
                            <input type="text" class="form-control" id="username" name="username"
                                   value="<%= editUser != null ? editUser.getUsername() : "" %>" required>
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label">Email</label>
                            <input type="email" class="form-control" id="email" name="email"
                                   value="<%= editUser != null ? editUser.getEmail() : "" %>" required>
                        </div>

                        <div class="mb-3">
                            <label for="password" class="form-label">Password</label>
                            <input type="password" class="form-control" id="password" name="password"
                                <%= editUser == null ? "required" : "" %>
                                   placeholder="<%= editUser != null ? "Leave blank to keep current password" : "" %>">
                        </div>

                        <div class="mb-3">
                            <label for="role" class="form-label">Role</label>
                            <select class="form-select" id="role" name="role" required>
                                <option value="">Select Role</option>
                                <option value="admin" <%= editUser != null && "admin".equals(editUser.getRole()) ? "selected" : "" %>>Admin</option>
                                <option value="plus" <%= editUser != null && "plus".equals(editUser.getRole()) ? "selected" : "" %>>Plus</option>
                                <option value="user" <%= editUser != null && "user".equals(editUser.getRole()) ? "selected" : "" %>>User</option>
                            </select>
                        </div>

                        <button type="submit" class="btn btn-primary">
                            <%= editUser != null ? "Update User" : "Add User" %>
                        </button>

                        <% if (editUser != null) { %>
                        <a href="UserManagementServlet" class="btn btn-secondary">Cancel Edit</a>
                        <% } %>
                    </form>
                </div>
            </div>
        </div>

        <div class="col-md-6">
            <!-- Users Table Card -->
            <div class="card">
                <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">All Users</h5>
                    <a href="UserManagementServlet?action=view" class="btn btn-sm btn-success">View All</a>
                </div>
                <div class="card-body">
                    <% if (users != null && !users.isEmpty()) { %>
                    <div class="table-responsive">
                        <table class="table table-striped table-hover">
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Username</th>
                                <th>Email</th>
                                <th>Role</th>
                                <th>Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            <% for (User user : users) { %>
                            <tr>
                                <td><%= user.getId() %></td>
                                <td><%= user.getUsername() %></td>
                                <td><%= user.getEmail() %></td>
                                <td><span class="badge bg-<%= "admin".equals(user.getRole()) ? "danger" : ("plus".equals(user.getRole()) ? "warning" : "success") %>">
                                            <%= user.getRole() %>
                                        </span></td>
                                <td class="actions">
                                    <a href="UserManagementServlet?action=edit&id=<%= user.getId() %>"
                                       class="btn btn-sm btn-primary">Edit</a>
                                    <form class="d-inline" method="get" action="UserManagementServlet"
                                          onsubmit="return confirm('Are you sure you want to delete this user?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="<%= user.getId() %>">
                                        <button type="submit" class="btn btn-sm btn-danger">Delete</button>
                                    </form>
                                </td>
                            </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </div>
                    <% } else { %>
                    <p class="text-muted">No users found.</p>
                    <% } %>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Bootstrap JS Bundle with Popper -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Auto-dismiss alerts after 5 seconds
    document.addEventListener('DOMContentLoaded', function() {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(alert => {
            setTimeout(() => {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }, 5000);
        });
    });
</script>
</body>
</html>