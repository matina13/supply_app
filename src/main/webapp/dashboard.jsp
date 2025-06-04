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

    <!-- Replace the existing Production Options card in your JSP file with this updated version -->
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

</div>

<!-- Bootstrap JS -->
<!--<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>-->
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