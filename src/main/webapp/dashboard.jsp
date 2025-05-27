<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    if (session == null || session.getAttribute("role") == null ||
            (!session.getAttribute("role").equals("user") && !session.getAttribute("role").equals("plus"))) {
        response.sendRedirect("index.jsp");
        return;
    }
    String username = (String) session.getAttribute("username");
    String role = (String) session.getAttribute("role");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard</title>
     <script src="javascripts/dashboard.js"></script>
    <link rel="stylesheet" type="text/css" href="css/dashboard.css">
</head>
<body>
<div class="topbar">
    <div class="title">Welcome, <%= username %></div>
    <div class="nav">
        <p id="date"></p>
        <a href="LogoutServlet">Logout</a>
    </div>
</div>

<div class="content">
    <h2>Dashboard</h2>
    <p>
        User dashboard
    </p>
</div>
</body>
</html>