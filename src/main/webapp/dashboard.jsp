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
    <title><%= username %>'s Dashboard</title>
    <link rel="stylesheet" type="text/css" href="css/dashboard.css">
    <style>
        .clickable-row {
            cursor: pointer;
        }
        .clickable-row:hover {
            background-color: #f5f5f5;
        }
        .selected-row {
            background-color: #e3f2fd !important;
        }
        #buySection {
            display: none;
            margin-top: 1rem;
            padding: 1rem;
            background-color: #f8f9fa;
            border-radius: 4px;
        }
        .select-btn {
            pointer-events: auto !important;
        }
    </style>
</head>
<body>
<p hidden id="email"><%= email %></p>

<div class="topbar">
    <div class="title">Welcome, <%= username %></div>
    <div class="nav">
        <p>Money: <span id="money"></span> $ </p>
        <p id="date"></p>
        <a href="LogoutServlet">Logout</a>
    </div>
</div>

<div class="content">
    <h2>Your Dashboard</h2>

    <div class="card">
        <h3>Current Inventory</h3>
        <table id="inventoryTable">
            <thead>
            <tr>
                <th>Name</th>
                <th>Quantity</th>
            </tr>
            </thead>
            <tbody>
            <!-- Will be populated by JavaScript -->
            </tbody>
        </table>
    </div>

    <div class="card">
        <h3>Production Options</h3>
        <table>
            <thead>
            <tr>
                <th>Product Name</th>
            </tr>
            </thead>
            <tbody>
            <% java.util.ArrayList<org.example.demo.structs.ProducableGood> pgList = org.example.demo.supplyChain.DataGetter.getProducableGoods();%>
            <% for (org.example.demo.structs.ProducableGood pg : pgList) { %>
            <tr>
                <td><%= pg.getName() %></td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>

    <div class="card">
        <h3>Supplier Management</h3>
        <div style="margin-bottom: 1rem;">
            <label for="prodGoods">Select product: </label>
            <select id="prodGoods">
                <% for (org.example.demo.structs.ProducableGood pg : pgList) { %>
                <option value="<%= pg.getId() %>"><%= pg.getName() %></option>
                <% } %>
            </select>
            <button onclick="getSuppliers()">Find Suppliers</button>
        </div>

        <table id="suppliersTable">
            <thead>
            <tr>
                <th>Supplier</th>
                <th>Price</th>
                <th>Available</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <!-- Will be populated by JavaScript -->
            </tbody>
        </table>

        <div id="buySection">
            <h4>Purchase Materials</h4>
            <div>
                <label for="quantity">Quantity: </label>
                <input type="number" id="quantity" min="1" value="1">
                <button onclick="confirmPurchase()">Purchase</button>
            </div>
            <div id="purchaseMessage"></div>
        </div>
    </div>

    <div class="card" style="text-align: center;">
        <button onclick="produceGood()" style="padding: 0.75rem 1.5rem; font-size: 1.1rem;">
            Produce Selected Good
        </button>
    </div>
</div>

<script src="javascripts/dashboard.js"></script>
<script>
    // Global variables to store selected supplier and material
    let selectedSupplierId = null;
    let selectedMaterialId = null;
    let selectedSupplierName = null;
    let selectedMaterialName = null;
    let selectedPrice = null;
    let selectedMaxQuantity = null;

    // Display current date
    document.getElementById('date').textContent = new Date().toLocaleDateString();

    // Function to handle supplier selection
    function selectSupplier(supplierId, materialId, supplierName, materialName, price, maxQuantity, event) {
        event = event || window.event; // For IE compatibility
        event.stopPropagation();

        selectedSupplierId = supplierId;
        selectedMaterialId = materialId;
        selectedSupplierName = supplierName;
        selectedMaterialName = materialName;
        selectedPrice = price;
        selectedMaxQuantity = maxQuantity;

        // Highlight selected row
        const rows = document.querySelectorAll('#suppliersTable tbody tr');
        rows.forEach(row => row.classList.remove('selected-row'));
        event.currentTarget.closest('tr').classList.add('selected-row');

        // Show buy section
        document.getElementById('buySection').style.display = 'block';
        document.getElementById('purchaseMessage').innerHTML =
            'Selected ' + materialName + ' from ' + supplierName +
            ' at $' + price + ' each (max: ' + maxQuantity + ')';
        document.getElementById('quantity').value = 1;
        document.getElementById('quantity').max = maxQuantity;
    }

    // Function to confirm purchase
    function confirmPurchase() {
        const quantity = parseInt(document.getElementById('quantity').value);
        const totalPrice = quantity * selectedPrice;

        if (!quantity || quantity <= 0) {
            alert('Please enter a valid quantity');
            return;
        }

        if (quantity > selectedMaxQuantity) {
            alert('Cannot order more than ' + selectedMaxQuantity + ' units');
            return;
        }

        if (confirm('Confirm purchase of ' + quantity + ' ' + selectedMaterialName +
            ' from ' + selectedSupplierName + ' for $' + totalPrice + '?')) {
            console.log(selectedSupplierId, selectedMaterialId, quantity)
            // Call buyMaterial with the selected parameters
            buyMaterial(selectedSupplierId, selectedMaterialId, quantity);

        }
    }
</script>
</body>
</html>