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

    // Get users list and messages
    List<User> users = (List<User>) request.getAttribute("users");
    User editUser = (User) request.getAttribute("editUser");
    String error = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
%>
<html>
<head>
    <title>Admin Dashboard</title>
    <link rel="stylesheet" type="text/css" href="css/adminDashboard.css">
</head>

<body>
<div class="topbar">
    <div class="title">Welcome, <%= username %></div>
    <div class="nav">
<%-- <a href="UserManagementServlet">User Management</a>--%>
 <a href="LogoutServlet">Logout</a>
</div>
</div>

<div class="content">
<h2>Admin Dashboard</h2>

<!-- Success/Error Messages -->
<% if (error != null) { %>
<div class="alert alert-error">
 <%= error %>
</div>
<% } %>

<% if (success != null) { %>
<div class="alert alert-success">
 <%= success %>
</div>
<% } %>

<!-- Add/Edit User Form -->
<div class="card">
 <h3><%= editUser != null ? "Edit User" : "Add New User" %></h3>
 <form method="post" action="UserManagementServlet">
     <input type="hidden" name="action" value="<%= editUser != null ? "update" : "add" %>">
     <% if (editUser != null) { %>
     <input type="hidden" name="id" value="<%= editUser.getId() %>">
     <% } %>

     <div class="form-group">
         <label for="username">Username:</label>
         <input type="text" id="username" name="username"
                value="<%= editUser != null ? editUser.getUsername() : "" %>" required>
     </div>

     <div class="form-group">
         <label for="email">Email:</label>
         <input type="email" id="email" name="email"
                value="<%= editUser != null ? editUser.getEmail() : "" %>" required>
     </div>

     <div class="form-group">
         <label for="password">Password:</label>
         <input type="password" id="password" name="password"
             <%= editUser == null ? "required" : "" %>
                placeholder="<%= editUser != null ? "Leave blank to keep current password" : "" %>">
     </div>

     <div class="form-group">
         <label for="role">Role:</label>
         <select id="role" name="role" required>
             <option value="">Select Role</option>
             <option value="admin" <%= editUser != null && "admin".equals(editUser.getRole()) ? "selected" : "" %>>Admin</option>
             <option value="plus" <%= editUser != null && "plus".equals(editUser.getRole()) ? "selected" : "" %>>Plus</option>
             <option value="user" <%= editUser != null && "user".equals(editUser.getRole()) ? "selected" : "" %>>User</option>
         </select>
     </div>

     <button type="submit" class="btn-primary">
         <%= editUser != null ? "Update User" : "Add User" %>
     </button>

     <% if (editUser != null) { %>
     <a href="UserManagementServlet" class="btn-edit">Cancel Edit</a>
     <% } %>
 </form>
</div>
<div class="action-buttons">
 <a href="UserManagementServlet?action=view" class="btn-view">
     View/Edit All Users
 </a>
</div>
<!-- Users Table -->
<div class="card">
 <h3>All Users</h3>
 <% if (users != null && !users.isEmpty()) { %>
 <table class="user-table">
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
         <td><%= user.getRole() %></td>
         <td class="actions">
             <a href="UserManagementServlet?action=edit&id=<%= user.getId() %>"
                class="btn-edit">Edit</a>
             <form class="inline-form" method="get" action="UserManagementServlet"
                   onsubmit="return confirm('Are you sure you want to delete this user?');">
                 <input type="hidden" name="action" value="delete">
                 <input type="hidden" name="id" value="<%= user.getId() %>">
                 <button type="submit" class="btn-delete">Delete</button>
             </form>
         </td>
     </tr>
     <% } %>
     </tbody>
 </table>
 <% } else { %>
 <p>No users found.</p>
 <% } %>
</div>
</div>

<script>
// Auto-hide alerts after 5 seconds
document.addEventListener('DOMContentLoaded', function() {
 const alerts = document.querySelectorAll('.alert');
 alerts.forEach(alert => {
     setTimeout(() => {
         alert.style.opacity = '0';
         setTimeout(() => alert.remove(), 300);
     }, 5000);
 });
});
</script>

</body>
</html>