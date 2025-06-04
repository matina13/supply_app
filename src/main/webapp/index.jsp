<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
 if (session != null && session.getAttribute("role") != null) org.example.demo.LoginServlet.alreadyLoggedIn(request, response, session);
%>

<!DOCTYPE html>
<html>
<head>
    <title>Login / Register</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f2f2f2;
            display: flex;
            min-height: 100vh;
            justify-content: center;
            align-items: center;
            margin: 0;
            padding: 20px;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 0 10px #aaa;
            width: 100%;
            max-width: 350px;
        }
        h2 {
            text-align: center;
            margin-top: 0;
        }
        form {
            display: none;
            flex-direction: column;
        }
        form.active {
            display: flex;
        }
        input[type="text"],
        input[type="email"],
        input[type="password"] {
            padding: 10px;
            margin: 8px 0;
            border: 1px solid #ccc;
            border-radius: 4px;
            width: 100%;
            box-sizing: border-box;
        }
        input[type="submit"] {
            background-color: #28a745;
            color: white;
            border: none;
            padding: 12px;
            margin-top: 15px;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            width: 100%;
        }
        .tab-buttons {
            display: flex;
            justify-content: space-around;
            margin-bottom: 20px;
            border-bottom: 1px solid #ddd;
        }
        .tab-buttons button {
            background: none;
            border: none;
            font-weight: bold;
            cursor: pointer;
            font-size: 16px;
            padding: 10px 20px;
            color: #666;
        }
        .tab-buttons button.active {
            color: #28a745;
            border-bottom: 2px solid #28a745;
        }
        .message {
            padding: 10px;
            margin-bottom: 15px;
            border-radius: 4px;
            text-align: center;
        }
        .error {
            background-color: #ffebee;
            color: #d32f2f;
        }
        .success {
            background-color: #e8f5e9;
            color: #388e3c;
        }
    </style>
    <script>
        function showForm(id) {
            // Update active tab
            document.querySelectorAll('.tab-buttons button').forEach(btn => {
                btn.classList.remove('active');
            });
            event.currentTarget.classList.add('active');

            // Show selected form
            document.getElementById("loginForm").classList.remove("active");
            document.getElementById("registerForm").classList.remove("active");
            document.getElementById(id).classList.add("active");
        }

        // Initialize with login form active
        window.onload = () => {
            document.querySelector('.tab-buttons button:first-child').classList.add('active');
            document.getElementById('loginForm').classList.add('active');
        };

        // Simple password match validation
        function validatePassword() {
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (password !== confirmPassword) {
                alert('Passwords do not match!');
                return false;
            }
            return true;
        }
    </script>
</head>
<body>
<div class="container">
    <!-- Message display area -->
    <% if (request.getAttribute("error") != null) { %>
    <div class="message error"><%= request.getAttribute("error") %></div>
    <% } %>
    <% if (request.getAttribute("success") != null) { %>
    <div class="message success"><%= request.getAttribute("success") %></div>
    <% } %>

    <div class="tab-buttons">
        <button onclick="showForm('loginForm')">Login</button>
        <button onclick="showForm('registerForm')">Register</button>
    </div>

    <form id="loginForm" action="LoginServlet" method="post">
        <h2>Login</h2>
        <input type="email" name="email" placeholder="Email" required />
        <input type="password" name="password" placeholder="Password" required autocomplete="off" />
        <input type="submit" value="Login" />
    </form>

    <form id="registerForm" action="RegisterServlet" method="post" onsubmit="return validatePassword()">
        <h2>Register</h2>
        <label>
            <input type="text" name="username" placeholder="Username" required />
        </label>
        <label>
            <input type="email" name="email" placeholder="email"${Email != null ? email : ''}">

        </label>
        <label for="password"></label><input type="password" id="password" name="password" placeholder="Password" required autocomplete="off" />
        <label for="confirmPassword"></label><input type="password" id="confirmPassword" placeholder="Confirm Password" required autocomplete="off" />
        <input type="submit" value="Register" />
    </form>
</div>
<% if (request.getAttribute("error") != null) { %>
<div class="error-message"><%= request.getAttribute("error") %></div>
<% } %>
</body>

</html>