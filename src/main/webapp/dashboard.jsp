<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    if (session == null || session.getAttribute("role") == null ||
            (!session.getAttribute("role").equals("user") && !session.getAttribute("role").equals("plus"))) {
        response.sendRedirect("index.jsp");
        return;
    }
    String username = (String) session.getAttribute("username");
    String email = (String) session.getAttribute("email");
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
<p hidden id="email"><%= email %></p>
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

    <p>Inventory:</p>
    <table id="inventoryTable">
      <th>Name</th>
      <th>Quantity</th>
    </tr>
    </table>
    <br>
    <p>Producable goods:</p>
    <table>
    <tr>
      <th>Name</th>
    </tr>
      <% java.util.ArrayList<org.example.demo.structs.ProducableGood> pgList = org.example.demo.supplyChain.DataGetter.getProducableGoods();%>
          <% for (org.example.demo.structs.ProducableGood pg : pgList) { %>
          <tr><td><%= pg.getName() %></td></tr>
          <% } %>
    </table>

    <br><br><br><p>For testing:</p>
    <label for="prodGoods">Producable goods:</label>
    <select id="prodGoods">
      <% //java.util.ArrayList<org.example.demo.structs.ProducableGood> pgList = org.example.demo.supplyChain.DataGetter.getProducableGoods();%>
          <% for (org.example.demo.structs.ProducableGood pg : pgList) { %>
          <option value=<%= pg.getId() %>><%= pg.getName() %></option>
          <% } %>
    </select>

    <button onclick=getSuppliers()>Click</button>

    <br>
    <p>Suppliers selling X:</p> //on X get name of the material
    <table id="suppliersTable">
        <th>Supplier</th>
        <th>Price</th>
        <th>Quantity available</th>
    </tr>
    </table>
    <br>
    //Quantity to buy box<br> //check in js if input quantity to buy <= quantity of item supplies has<br>
    <button onclick=buyMaterial()>Buy</button>

    <br><br><br>
    <button onclick=produceGood()>Produce Bike</button>

</div>
</body>
</html>