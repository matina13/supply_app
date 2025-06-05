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
    <!-- Bootstrap CSS -->
    <!-- <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">-->
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
                <th>Quantity to Produce</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <% java.util.ArrayList<org.example.demo.structs.ProducableGood> pgList = org.example.demo.supplyChain.DataGetter.getProducableGoods();%>
            <% for (org.example.demo.structs.ProducableGood pg : pgList) { %>
            <tr>
                <td><%= pg.getName() %></td>
                <td>
                    <input type="number"
                           id="quantity_<%= pg.getId() %>"
                           class="production-quantity-input"
                           min="1"
                           value="1"
                           style="width: 80px; padding: 0.25rem;">
                </td>
                <td>
                    <button onclick="produceSpecificGood(<%= pg.getId() %>)"
                            style="padding: 0.5rem 1rem; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer;">
                        Produce
                    </button>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>



    <!-- Replace the existing Supplier Management card in your JSP file with this updated version -->
    <div class="card">
        <h3>Supplier Management</h3>

        <!-- Step 1: Select Supplier -->
        <div style="margin-bottom: 1rem;">
            <label for="supplierSelect">Select Supplier: </label>
            <select id="supplierSelect" onchange="getSupplierMaterials()">
                <option value="">-- Choose a Supplier --</option>
                <!-- Suppliers will be populated by JavaScript -->
            </select>
            <button onclick="loadSuppliers()" style="margin-left: 0.5rem;">Refresh Suppliers</button>
        </div>

        <!-- Step 2: Show Selected Supplier's Materials -->
        <div id="supplierInfo" style="display: none; margin-bottom: 1rem; padding: 0.5rem; background-color: #f8f9fa; border-radius: 4px;">
            <strong>Selected Supplier: </strong><span id="selectedSupplierName"></span>
        </div>

        <!-- Step 3: Materials Table -->
        <table id="supplierMaterialsTable">
            <thead>
            <tr>
                <th>Material</th>
                <th>Price per Unit</th>
                <th>Available Quantity</th>
                <th>Quantity to Buy</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="5" style="text-align: center; color: #999; padding: 2rem;">
                    <em>Please select a supplier to view available materials</em>
                </td>
            </tr>
            </tbody>
        </table>

        <!-- Purchase Confirmation Section -->
        <div id="purchaseConfirmSection" style="display: none; margin-top: 1rem; padding: 1rem; background-color: #e8f5e8; border-radius: 4px;">
            <h4>Purchase Confirmation</h4>
            <div id="purchaseDetails"></div>
            <div style="margin-top: 0.5rem;">
                <button onclick="confirmMaterialPurchase()" style="background-color: #28a745; color: white; padding: 0.5rem 1rem; border: none; border-radius: 4px; margin-right: 0.5rem;">
                    Confirm Purchase
                </button>
                <button onclick="cancelPurchase()" style="background-color: #6c757d; color: white; padding: 0.5rem 1rem; border: none; border-radius: 4px;">
                    Cancel
                </button>
            </div>
            <div id="purchaseMessage" style="margin-top: 0.5rem; font-weight: bold;"></div>
        </div>
    </div>

    <div class="card">
        <h3>Transaction History</h3>

        <!-- Refresh button -->
        <div style="margin-bottom: 1rem;">
            <button onclick="loadTransactions()" style="padding: 0.5rem 1rem; background-color: #17a2b8; color: white; border: none; border-radius: 4px; cursor: pointer;">
                Refresh Transactions
            </button>
        </div>

        <!-- Transactions Table -->
        <table id="transactionsTable">
            <thead>
            <tr>
                <th>Transaction ID</th>
                <th>Type</th>
                <th>Order ID</th>
                <th>Supplier ID</th>
                <th>Date Finished</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="5" style="text-align: center; color: #999; padding: 2rem;">
                    <em>Click "Refresh Transactions" to load transaction history</em>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

    <!-- Transit -->
    <div class="card">
        <h3>Transit Status</h3>

        <!-- Refresh button -->
        <div style="margin-bottom: 1rem;">
            <button onclick="loadTransit()" style="padding: 0.5rem 1rem; background-color: #6f42c1; color: white; border: none; border-radius: 4px; cursor: pointer;">
                Refresh Transit
            </button>
        </div>

        <!-- Transit Table -->
        <table id="transitTable">
            <thead>
            <tr>
                <th>Transaction ID</th>
                <th>Supplier ID</th>
                <th>Order ID</th>
                <th>Shipment Date</th>
                <th>Delivery Date</th>
                <th>Status</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="6" style="text-align: center; color: #999; padding: 2rem;">
                    <em>Click "Refresh Transit" to load shipment status</em>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>

<!--update to VIP -->
<% if ("user".equals(role)) { %>
<div class="card">
    <h3>Upgrade to Plus</h3>

    <div style="background-color: #f8f9fa; padding: 1rem; border-radius: 4px; margin-bottom: 1rem;">
        <h4 style="margin-top: 0;">Plus Features:</h4>
        <ul style="margin-bottom: 0;">
            <li>AI Supplier Optimization</li>
            <li>Advanced Analytics</li>
            <li>Priority Support</li>
        </ul>
    </div>

    <div style="text-align: center; margin-bottom: 1rem;">
        <div style="font-size: 1.5rem; font-weight: bold; color: #28a745;">$500</div>
        <div style="color: #6c757d;">One-time payment</div>
    </div>

    <div style="text-align: center;">
        <button onclick="upgradeToPlus()"
                style="padding: 0.75rem 1.5rem; background-color: #28a745; color: white; border: none; border-radius: 4px; font-size: 1rem; cursor: pointer;">
            Upgrade to Plus
        </button>
    </div>
</div>
<% } %>

<!--only for plus users -->
<% if ("plus".equals(role)) { %>

<h3 align="center">Vip features</h3>


    <div class="card">
        <h3>Production Options</h3>
        <table>
            <thead>
            <tr>
                <th>Product Name</th>
                <th>Quantity to Produce</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <% for (org.example.demo.structs.ProducableGood pg : pgList) { %>
            <tr>
                <td><%= pg.getName() %></td>
                <td>
                    <input type="number"
                           id="quantity_<%= pg.getId() %>"
                           class="production-quantity-input"
                           min="1"
                           value="1"
                           style="width: 80px; padding: 0.25rem;">
                </td>
                <td>
                    <button onclick="startAlgorithm(<%= pg.getId() %>)"
                            style="padding: 0.5rem 1rem; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer;">
                         Find Best Supplier
                    </button>
                </td>

            </tr>
            <% } %>
            </tbody>
        </table>
    </div>

<% } %>



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