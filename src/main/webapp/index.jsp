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
        input[type="password"],
        select {
            padding: 10px;
            margin: 8px 0;
            border: 1px solid #ccc;
            border-radius: 4px;
            width: 100%;
            box-sizing: border-box;
            font-size: 14px;
        }
        select {
            background-color: white;
            cursor: pointer;
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
        .continent-info {
            font-size: 12px;
            color: #666;
            margin-top: 5px;
            text-align: center;
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
            FI: "Europe", PL: "Europe", CZ: "Europe", HU: "Europe", PT: "Portugal", GR: "Europe",
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
        <input type="text" name="username" placeholder="Username" required />
        <input type="email" name="email" placeholder="Email" required />

        <!-- Country Selection -->
        <select id="country" name="country" required>
            <option value="">-- Select Your Country --</option>

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

        <input type="password" id="password" name="password" placeholder="Password" required autocomplete="off" />
        <input type="password" id="confirmPassword" placeholder="Confirm Password" required autocomplete="off" />
        <input type="submit" value="Register" />
    </form>
</div>
<% if (request.getAttribute("error") != null) { %>
<div class="error-message"><%= request.getAttribute("error") %></div>
<% } %>
</body>

</html>