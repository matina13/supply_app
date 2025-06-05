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
    <style>
        select {
            padding: 8px;
            margin: 5px 0;
            border: 1px solid #ccc;
            border-radius: 4px;
            width: 100%;
            box-sizing: border-box;
            font-size: 14px;
            background-color: white;
            cursor: pointer;
        }
        .continent-info {
            font-size: 12px;
            color: #666;
            margin-top: 5px;
            margin-bottom: 10px;
        }
    </style>
</head>

<body>
<div class="topbar">
    <div class="title">Welcome, <%= username %></div>
    <div class="nav">
        <p id="date"> </p>
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

            <!-- Country Selection - Show for both new users and editing (but required only for new users) -->
            <div class="form-group">
                <label for="country">Country:</label>
                <select id="country" name="country" <%= editUser == null ? "required" : "" %> onchange="updateContinentInfo()">
                    <option value="">-- Select Country --</option>

                    <!-- Popular Countries -->
                    <optgroup label="Popular Countries">
                        <option value="US">🇺🇸 United States</option>
                        <option value="GB">🇬🇧 United Kingdom</option>
                        <option value="CA">🇨🇦 Canada</option>
                        <option value="DE">🇩🇪 Germany</option>
                        <option value="FR">🇫🇷 France</option>
                        <option value="AU">🇦🇺 Australia</option>
                        <option value="JP">🇯🇵 Japan</option>
                        <option value="CN">🇨🇳 China</option>
                        <option value="IN">🇮🇳 India</option>
                        <option value="BR">🇧🇷 Brazil</option>
                    </optgroup>

                    <!-- North America -->
                    <optgroup label="North America">
                        <option value="US">United States</option>
                        <option value="CA">Canada</option>
                        <option value="MX">Mexico</option>
                        <option value="GT">Guatemala</option>
                        <option value="CR">Costa Rica</option>
                        <option value="PA">Panama</option>
                        <option value="CU">Cuba</option>
                        <option value="JM">Jamaica</option>
                    </optgroup>

                    <!-- South America -->
                    <optgroup label="South America">
                        <option value="BR">Brazil</option>
                        <option value="AR">Argentina</option>
                        <option value="CL">Chile</option>
                        <option value="PE">Peru</option>
                        <option value="CO">Colombia</option>
                        <option value="VE">Venezuela</option>
                        <option value="EC">Ecuador</option>
                        <option value="BO">Bolivia</option>
                        <option value="PY">Paraguay</option>
                        <option value="UY">Uruguay</option>
                        <option value="GY">Guyana</option>
                        <option value="SR">Suriname</option>
                    </optgroup>

                    <!-- Europe -->
                    <optgroup label="Europe">
                        <option value="GB">United Kingdom</option>
                        <option value="DE">Germany</option>
                        <option value="FR">France</option>
                        <option value="IT">Italy</option>
                        <option value="ES">Spain</option>
                        <option value="NL">Netherlands</option>
                        <option value="BE">Belgium</option>
                        <option value="CH">Switzerland</option>
                        <option value="AT">Austria</option>
                        <option value="SE">Sweden</option>
                        <option value="NO">Norway</option>
                        <option value="DK">Denmark</option>
                        <option value="FI">Finland</option>
                        <option value="PL">Poland</option>
                        <option value="CZ">Czech Republic</option>
                        <option value="HU">Hungary</option>
                        <option value="PT">Portugal</option>
                        <option value="GR">Greece</option>
                        <option value="IE">Ireland</option>
                        <option value="RO">Romania</option>
                        <option value="BG">Bulgaria</option>
                        <option value="HR">Croatia</option>
                        <option value="SK">Slovakia</option>
                        <option value="SI">Slovenia</option>
                        <option value="EE">Estonia</option>
                        <option value="LV">Latvia</option>
                        <option value="LT">Lithuania</option>
                        <option value="LU">Luxembourg</option>
                        <option value="MT">Malta</option>
                        <option value="CY">Cyprus</option>
                        <option value="RU">Russia</option>
                        <option value="UA">Ukraine</option>
                        <option value="BY">Belarus</option>
                        <option value="RS">Serbia</option>
                        <option value="BA">Bosnia and Herzegovina</option>
                        <option value="ME">Montenegro</option>
                        <option value="MK">North Macedonia</option>
                        <option value="AL">Albania</option>
                        <option value="MD">Moldova</option>
                    </optgroup>

                    <!-- Asia -->
                    <optgroup label="Asia">
                        <option value="CN">China</option>
                        <option value="IN">India</option>
                        <option value="ID">Indonesia</option>
                        <option value="PK">Pakistan</option>
                        <option value="BD">Bangladesh</option>
                        <option value="JP">Japan</option>
                        <option value="PH">Philippines</option>
                        <option value="VN">Vietnam</option>
                        <option value="TR">Turkey</option>
                        <option value="IR">Iran</option>
                        <option value="TH">Thailand</option>
                        <option value="MM">Myanmar</option>
                        <option value="KR">South Korea</option>
                        <option value="IQ">Iraq</option>
                        <option value="AF">Afghanistan</option>
                        <option value="SA">Saudi Arabia</option>
                        <option value="UZ">Uzbekistan</option>
                        <option value="MY">Malaysia</option>
                        <option value="NP">Nepal</option>
                        <option value="YE">Yemen</option>
                        <option value="KP">North Korea</option>
                        <option value="SY">Syria</option>
                        <option value="KH">Cambodia</option>
                        <option value="JO">Jordan</option>
                        <option value="AZ">Azerbaijan</option>
                        <option value="AE">United Arab Emirates</option>
                        <option value="TJ">Tajikistan</option>
                        <option value="IL">Israel</option>
                        <option value="LA">Laos</option>
                        <option value="SG">Singapore</option>
                        <option value="OM">Oman</option>
                        <option value="KW">Kuwait</option>
                        <option value="GE">Georgia</option>
                        <option value="MN">Mongolia</option>
                        <option value="AM">Armenia</option>
                        <option value="QA">Qatar</option>
                        <option value="BH">Bahrain</option>
                        <option value="TL">East Timor</option>
                        <option value="LB">Lebanon</option>
                        <option value="KG">Kyrgyzstan</option>
                        <option value="TM">Turkmenistan</option>
                        <option value="BT">Bhutan</option>
                        <option value="BN">Brunei</option>
                        <option value="MV">Maldives</option>
                    </optgroup>

                    <!-- Africa -->
                    <optgroup label="Africa">
                        <option value="NG">Nigeria</option>
                        <option value="ET">Ethiopia</option>
                        <option value="EG">Egypt</option>
                        <option value="ZA">South Africa</option>
                        <option value="KE">Kenya</option>
                        <option value="UG">Uganda</option>
                        <option value="DZ">Algeria</option>
                        <option value="SD">Sudan</option>
                        <option value="MA">Morocco</option>
                        <option value="AO">Angola</option>
                        <option value="GH">Ghana</option>
                        <option value="MZ">Mozambique</option>
                        <option value="MG">Madagascar</option>
                        <option value="CM">Cameroon</option>
                        <option value="CI">Ivory Coast</option>
                        <option value="NE">Niger</option>
                        <option value="BF">Burkina Faso</option>
                        <option value="ML">Mali</option>
                        <option value="MW">Malawi</option>
                        <option value="ZM">Zambia</option>
                        <option value="SO">Somalia</option>
                        <option value="SN">Senegal</option>
                        <option value="TD">Chad</option>
                        <option value="SL">Sierra Leone</option>
                        <option value="LY">Libya</option>
                        <option value="TN">Tunisia</option>
                        <option value="BW">Botswana</option>
                        <option value="NA">Namibia</option>
                        <option value="ZW">Zimbabwe</option>
                        <option value="TZ">Tanzania</option>
                        <option value="RW">Rwanda</option>
                        <option value="CG">Congo</option>
                        <option value="CD">Democratic Republic of Congo</option>
                        <option value="CF">Central African Republic</option>
                    </optgroup>

                    <!-- Oceania -->
                    <optgroup label="Oceania">
                        <option value="AU">Australia</option>
                        <option value="PG">Papua New Guinea</option>
                        <option value="NZ">New Zealand</option>
                        <option value="FJ">Fiji</option>
                        <option value="SB">Solomon Islands</option>
                        <option value="NC">New Caledonia</option>
                        <option value="PF">French Polynesia</option>
                        <option value="VU">Vanuatu</option>
                        <option value="WS">Samoa</option>
                        <option value="FM">Micronesia</option>
                        <option value="TO">Tonga</option>
                        <option value="KI">Kiribati</option>
                        <option value="PW">Palau</option>
                        <option value="MH">Marshall Islands</option>
                        <option value="TV">Tuvalu</option>
                        <option value="NR">Nauru</option>
                    </optgroup>
                </select>

                <div id="continentInfo" class="continent-info" style="display: none;"></div>
                <% if (editUser != null) { %>
                <small style="color: #666; font-style: italic;">Note: Country selection is only available when creating new users</small>
                <% } %>
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

    // Display current date
    document.getElementById('date').textContent = new Date().toLocaleDateString();
    // Continent mapping for admin form
    const continentMap = {
        // North America
        US: "North America", CA: "North America", MX: "North America", GT: "North America",
        CR: "North America", PA: "North America", CU: "North America", JM: "North America",

        // South America
        BR: "South America", AR: "South America", CL: "South America", PE: "South America",
        CO: "South America", VE: "South America", EC: "South America", BO: "South America",
        PY: "South America", UY: "South America", GY: "South America", SR: "South America",

        // Europe
        GB: "Europe", DE: "Europe", FR: "Europe", IT: "Europe", ES: "Europe", NL: "Europe",
        BE: "Europe", CH: "Europe", AT: "Europe", SE: "Europe", NO: "Europe", DK: "Europe",
        FI: "Europe", PL: "Europe", CZ: "Europe", HU: "Europe", PT: "Europe", GR: "Europe",
        IE: "Europe", RO: "Europe", BG: "Europe", HR: "Europe", SK: "Europe", SI: "Europe",
        EE: "Europe", LV: "Europe", LT: "Europe", LU: "Europe", MT: "Europe", CY: "Europe",
        RU: "Europe", UA: "Europe", BY: "Europe", RS: "Europe", BA: "Europe", ME: "Europe",
        MK: "Europe", AL: "Europe", MD: "Europe",

        // Africa
        NG: "Africa", ET: "Africa", EG: "Africa", ZA: "Africa", KE: "Africa", UG: "Africa",
        DZ: "Africa", SD: "Africa", MA: "Africa", AO: "Africa", GH: "Africa", MZ: "Africa",
        MG: "Africa", CM: "Africa", CI: "Africa", NE: "Africa", BF: "Africa", ML: "Africa",
        MW: "Africa", ZM: "Africa", SO: "Africa", SN: "Africa", TD: "Africa", SL: "Africa",
        LY: "Africa", TN: "Africa", BW: "Africa", NA: "Africa", ZW: "Africa", TZ: "Africa",
        RW: "Africa", CG: "Africa", CD: "Africa", CF: "Africa",

        // Asia
        CN: "Asia", IN: "Asia", ID: "Asia", PK: "Asia", BD: "Asia", JP: "Asia",
        PH: "Asia", VN: "Asia", TR: "Asia", IR: "Asia", TH: "Asia", MM: "Asia",
        KR: "Asia", IQ: "Asia", AF: "Asia", SA: "Asia", UZ: "Asia", MY: "Asia",
        NP: "Asia", YE: "Asia", KP: "Asia", SY: "Asia", KH: "Asia", JO: "Asia",
        AZ: "Asia", AE: "Asia", TJ: "Asia", IL: "Asia", LA: "Asia", SG: "Asia",
        OM: "Asia", KW: "Asia", GE: "Asia", MN: "Asia", AM: "Asia", QA: "Asia",
        BH: "Asia", TL: "Asia", LB: "Asia", KG: "Asia", TM: "Asia", BT: "Asia",
        BN: "Asia", MV: "Asia",

        // Oceania
        AU: "Oceania", PG: "Oceania", NZ: "Oceania", FJ: "Oceania", SB: "Oceania",
        NC: "Oceania", PF: "Oceania", VU: "Oceania", WS: "Oceania", FM: "Oceania",
        TO: "Oceania", KI: "Oceania", PW: "Oceania", MH: "Oceania", TV: "Oceania",
        NR: "Oceania"
    };

    // Update continent display for admin form
    function updateContinentInfo() {
        const countrySelect = document.getElementById('country');
        const continentInfo = document.getElementById('continentInfo');

        if (!countrySelect || !continentInfo) return; // Exit if elements don't exist

        const selectedCountry = countrySelect.value;

        if (selectedCountry && continentMap[selectedCountry]) {
            const continent = continentMap[selectedCountry];
            continentInfo.textContent = `Continent: ${continent}`;
            continentInfo.style.display = 'block';
        } else {
            continentInfo.style.display = 'none';
        }
    }

    // Auto-hide alerts after 5 seconds
    document.addEventListener('DOMContentLoaded', function () {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(alert => {
            setTimeout(() => {
                alert.style.opacity = '0';
                setTimeout(() => alert.remove(), 300);
            }, 5000);
        });

        // Disable country selection when editing existing users
        const editUser = <%= editUser != null ? "true" : "false" %>;
        if (editUser) {
            const countrySelect = document.getElementById('country');
            if (countrySelect) {
                countrySelect.disabled = true;
                countrySelect.style.backgroundColor = '#f5f5f5';
                countrySelect.style.cursor = 'not-allowed';
            }
        }
    });

</script>

</body>
</html>