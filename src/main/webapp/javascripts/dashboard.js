var webSocket = {};

webSocket.Socket = null;

webSocket.connect = (function(host) {
    if ('WebSocket' in window) {
        webSocket.Socket = new WebSocket(host);
    } else if ('MozWebSocket' in window) {
        webSocket.Socket = new MozWebSocket(host);
    } else {
        console.log('Error: WebSocket is not supported by this browser.');
        return;
    }

    webSocket.Socket.onopen = function () {
        console.log('Info: WebSocket connection opened.');
        const email = [{"email": document.getElementById("email").innerText}];
        webSocket.Socket.send(JSON.stringify(email));
    };

    webSocket.Socket.onclose = function () {
        console.log('Info: WebSocket closed.');
    };

    webSocket.Socket.onerror = function(error) {
        console.error('WebSocket error:', error);
    };

    webSocket.Socket.onmessage = function (message) {
        try {
            const msg_JSON = JSON.parse(message.data);
            console.log(msg_JSON);
            document.getElementById("date").innerText = msg_JSON["date"]; //update date
            document.getElementById("money").innerText = msg_JSON["money"];
            updateInventoryTable(msg_JSON);

            // Production alerts
            if (msg_JSON["data"][0]["alert_message"] != null) {
                alert(msg_JSON["data"][0]["alert_message"]);
            }

            //suppliers for dropdown
            if (msg_JSON["data"][0]["all_suppliers"] != null) {
                console.log('Auto-updating suppliers:', msg_JSON["data"][0]["all_suppliers"]);
                populateSuppliersDropdown(msg_JSON["data"][0]["all_suppliers"]);
            }

            // Materials for selected supplier
            if (msg_JSON["data"][0]["supplier_materials"] != null) {
                console.log('Found supplier_materials:', msg_JSON["data"][0]["supplier_materials"]);
                const supplierId = msg_JSON["data"][0]["selected_supplier_id"];
                updateSupplierMaterialsTable(msg_JSON["data"][0]["supplier_materials"], supplierId);
            }

            //Transactions list
            if (msg_JSON["data"][0]["transactions_list"] != null) {
                console.log('Auto-updating transactions:', msg_JSON["data"][0]["transactions_list"]);
                updateTransactionsTable(msg_JSON["data"][0]["transactions_list"]);
            }

            // Transit list
            if (msg_JSON["data"][0]["transit_list"] != null) {
                console.log('Auto-updating transit:', msg_JSON["data"][0]["transit_list"]);
                updateTransitTable(msg_JSON["data"][0]["transit_list"]);
            }
            if (msg_JSON["data"][0]["bestCalculation"] != null) {
                console.log('Found bestCalculation:', msg_JSON["data"][0]["bestCalculation"]);
                handleBestCalculation(msg_JSON["data"][0]["bestCalculation"], msg_JSON);
            }

        } catch (error) {
            console.error('Error parsing WebSocket message:', error);
        }
    };
});

webSocket.initialize = function() {
    if (window.location.protocol === 'http:') {
        webSocket.connect('ws://' + window.location.host + '/appSocket');
    } else {
        webSocket.connect('wss://' + window.location.host + '/appSocket');
    }
};

// Initialize WebSocket connection
webSocket.initialize();

function updateInventoryTable(msg_JSON) {
    try {
        const table = document.getElementById("inventoryTable");
        const tbody = table.querySelector('tbody');
        tbody.innerHTML = '';

        const inventory = msg_JSON["data"]?.[0]?.["inventory"];
        if (inventory && Array.isArray(inventory)) {
            inventory.forEach(item => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${item.name || 'Unknown Item'}</td>
                    <td>${item.quantity || 0}</td>
                `;
                tbody.appendChild(row);
            });
        } else {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td colspan="2" style="text-align: center; color: #999;">
                    <em>No inventory data available</em>
                </td>
            `;
            tbody.appendChild(row);
        }
    } catch (error) {
        console.error('Error updating inventory table:', error);
    }
}

function getSuppliers() {
    const material_id = document.getElementById("prodGoods").value;
    const json = [{"material_id": material_id}];
    webSocket.Socket.send(JSON.stringify(json));
}

function selectSupplier(supplierId, materialId, supplierName, materialName, price, maxQuantity, event) {
    // Prevent event bubbling
    event.stopPropagation();

    selectedSupplierId = supplierId;
    selectedMaterialId = materialId;
    selectedSupplierName = supplierName;
    selectedMaterialName = materialName;
    selectedPrice = price;
    selectedMaxQuantity = maxQuantity;

    const rows = document.querySelectorAll('#suppliersTable tbody tr');
    rows.forEach(row => {
        row.classList.remove('selected-row');
        row.style.backgroundColor = '';
    });
    event.currentTarget.closest('tr').classList.add('selected-row');

    // Show buy section
    document.getElementById('buySection').style.display = 'block';
    document.getElementById('purchaseMessage').textContent =
        `Selected ${materialName} from ${supplierName} at $${price} each (max: ${maxQuantity})`;
    document.getElementById('quantity').value = 1;
    document.getElementById('quantity').max = maxQuantity;
}

function buyMaterial(supplier_id, material_id, quantity) {
    const json = [{
        "buy_material": {
            "supplier_id": supplier_id,
            "material_id": material_id,
            "quantity": quantity
        }
    }];
    webSocket.Socket.send(JSON.stringify(json));

    // Show success message for new supplier management interface
    const purchaseMessage = document.getElementById('purchaseMessage');
    if (purchaseMessage) {
        purchaseMessage.textContent = 'Purchase order sent!';
        purchaseMessage.style.color = '#28a745';

        // Hide confirmation section after 3 seconds
        setTimeout(() => {
            cancelPurchase();
        }, 3000);
    }
}

function produceGood(producableGoodId , quantityToProduce ) {
    const json = [{
        "produce_good": {
            "producableGoodId": producableGoodId,
            "quantityToProduce": quantityToProduce
        }
    }];
    webSocket.Socket.send(JSON.stringify(json));
}

function produceSpecificGood(producableGoodId) {
    const quantityInput = document.getElementById(`quantity_${producableGoodId}`);
    const quantityToProduce = parseInt(quantityInput.value);

    // Validate quantity
    if (!quantityToProduce || quantityToProduce <= 0) {
        alert('Please enter a valid quantity (greater than 0)');
        return;
    }

    // Confirm production
    if (confirm(`Confirm production of ${quantityToProduce} unit(s)?`)) {
        // Call the existing produceGood function with the parameters
        console.log(producableGoodId, quantityToProduce)
        produceGood(producableGoodId, quantityToProduce);

        // Reset the quantity input to 1 after production
        quantityInput.value = 1;

        // Optional: Show a success message
        showProductionMessage(`Production order for ${quantityToProduce} unit(s) has been sent!`);
    }
}

// Function to show production messages
function showProductionMessage(message) {
    // Create a temporary message element
    const messageDiv = document.createElement('div');
    messageDiv.textContent = message;
    messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background-color: #28a745;
        color: white;
        padding: 1rem;
        border-radius: 4px;
        z-index: 1000;
        box-shadow: 0 2px 8px rgba(0,0,0,0.2);
    `;

    document.body.appendChild(messageDiv);

    // Remove the message after 3 seconds
    setTimeout(() => {
        document.body.removeChild(messageDiv);
    }, 3000);
}

let pendingPurchase = {
    supplier_id: null,
    material_id: null,
    quantity: 0,
    price: 0,
    supplier_name: '',
    material_name: ''
};

// Load all suppliers
function loadSuppliers() {
    // Check if WebSocket is ready before sending
    if (webSocket.Socket && webSocket.Socket.readyState === WebSocket.OPEN) {
        const json = [{"get_suppliers": true}];
        webSocket.Socket.send(JSON.stringify(json));
    } else {
        console.log('WebSocket not ready, will load suppliers when connected');
        // Set a flag to load suppliers when connection is ready
        window.loadSuppliersOnConnect = true;
    }
}

function populateSuppliersDropdown(suppliers) {
    const supplierSelect = document.getElementById('supplierSelect');
    const currentSelection = supplierSelect.value; // Preserve current selection

    // Clear and repopulate
    supplierSelect.innerHTML = '<option value="">-- Choose a Supplier --</option>';

    if (suppliers && Array.isArray(suppliers)) {
        suppliers.forEach(supplier => {
            const option = document.createElement('option');
            option.value = supplier.supplier_id;
            option.textContent = supplier.name || `Supplier ${supplier.supplier_id}`;
            option.dataset.supplierName = supplier.name || `Supplier ${supplier.supplier_id}`;

            // Restore selection if it was previously selected
            if (supplier.supplier_id.toString() === currentSelection) {
                option.selected = true;
            }

            supplierSelect.appendChild(option);
        });
    }
}

// Get materials for selected supplier
function getSupplierMaterials() {
    const supplierSelect = document.getElementById('supplierSelect');
    const supplierId = supplierSelect.value;

    if (!supplierId) {
        // Hide supplier info and reset table
        document.getElementById('supplierInfo').style.display = 'none';
        resetMaterialsTable();
        return;
    }

    // Show supplier info
    const supplierName = supplierSelect.options[supplierSelect.selectedIndex].dataset.supplierName;
    document.getElementById('selectedSupplierName').textContent = supplierName;
    document.getElementById('supplierInfo').style.display = 'block';

    // Check if WebSocket is ready before sending
    if (webSocket.Socket && webSocket.Socket.readyState === WebSocket.OPEN) {
        const json = [{"get_supplier_materials": {"supplier_id": parseInt(supplierId)}}];
        webSocket.Socket.send(JSON.stringify(json));
    } else {
        alert('Connection not ready. Please try again in a moment.');
    }
}

// Update materials table with supplier's available materials
function updateSupplierMaterialsTable(materials, supplierId) {
    const table = document.getElementById("supplierMaterialsTable");
    const tbody = table.querySelector('tbody');

    // Store current input values before clearing the table
    const currentValues = {};
    const existingInputs = tbody.querySelectorAll('input[id^="buyQuantity_"]');
    existingInputs.forEach(input => {
        const materialId = input.id.replace('buyQuantity_', '');
        currentValues[materialId] = input.value;
    });

    tbody.innerHTML = '';

    if (materials && Array.isArray(materials) && materials.length > 0) {
        materials.forEach(material => {
            const row = document.createElement('tr');

            // Use saved value if it exists, otherwise default to 1
            const savedValue = currentValues[material.material_id] || 1;

            row.innerHTML = `
                <td>${material.material_name || material.name || `Material ${material.material_id}`}</td>
                <td>$${material.price || 0}</td>
                <td>${material.quantity || 0}</td>
                <td>
                    <input type="number"
                           id="buyQuantity_${material.material_id}"
                           class="buy-quantity-input"
                           min="1"
                           max="${material.quantity || 0}"
                           value="${savedValue}"
                           style="width: 80px; padding: 0.25rem;">
                </td>
                <td>
                    <button onclick="preparePurchase(${supplierId}, ${material.material_id}, '${material.material_name || material.name || `Material ${material.material_id}`}', ${material.price})"
                            style="padding: 0.5rem 1rem; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer;"
                            ${!material.quantity || material.quantity <= 0 ? 'disabled' : ''}>
                        ${!material.quantity || material.quantity <= 0 ? 'Out of Stock' : 'Buy'}
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    } else {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td colspan="5" style="text-align: center; color: #999; padding: 2rem;">
                <em>No materials available from this supplier</em>
            </td>
        `;
        tbody.appendChild(row);
    }
}

// Reset materials table to default state
function resetMaterialsTable() {
    const table = document.getElementById("supplierMaterialsTable");
    const tbody = table.querySelector('tbody');
    tbody.innerHTML = `
        <tr>
            <td colspan="5" style="text-align: center; color: #999; padding: 2rem;">
                <em>Please select a supplier to view available materials</em>
            </td>
        </tr>
    `;
}

// Prepare purchase (show confirmation)
function preparePurchase(supplierId, materialId, materialName, price) {
    const quantityInput = document.getElementById(`buyQuantity_${materialId}`);
    const quantity = parseInt(quantityInput.value);

    // Validate quantity
    if (!quantity || quantity <= 0) {
        alert('Please enter a valid quantity (greater than 0)');
        return;
    }

    const maxQuantity = parseInt(quantityInput.max);
    if (quantity > maxQuantity) {
        alert(`Cannot buy more than ${maxQuantity} units`);
        return;
    }

    // Store purchase details
    const supplierSelect = document.getElementById('supplierSelect');
    const supplierName = supplierSelect.options[supplierSelect.selectedIndex].dataset.supplierName;

    pendingPurchase = {
        supplier_id: supplierId,
        material_id: materialId,
        quantity: quantity,
        price: price,
        supplier_name: supplierName,
        material_name: materialName
    };

    // Show confirmation section
    const totalCost = quantity * price;
    document.getElementById('purchaseDetails').innerHTML = `
        <strong>Purchase Details:</strong><br>
        Material: ${materialName}<br>
        Supplier: ${supplierName}<br>
        Quantity: ${quantity} units<br>
        Price per unit: $${price}<br>
        <strong>Total Cost: $${totalCost}</strong>
    `;

    document.getElementById('purchaseConfirmSection').style.display = 'block';
    document.getElementById('purchaseMessage').textContent = '';
}

// Confirm and execute purchase
function confirmMaterialPurchase() {
    if (!pendingPurchase.supplier_id || !pendingPurchase.material_id) {
        alert('Invalid purchase data');
        return;
    }

    // Call your buyMaterial function
    buyMaterial(pendingPurchase.supplier_id, pendingPurchase.material_id, pendingPurchase.quantity);

    // Show success message
    document.getElementById('purchaseMessage').textContent =
        `Purchase order sent for ${pendingPurchase.quantity} ${pendingPurchase.material_name}!`;
    document.getElementById('purchaseMessage').style.color = '#28a745';

    // Reset the quantity input
    const quantityInput = document.getElementById(`buyQuantity_${pendingPurchase.material_id}`);
    if (quantityInput) {
        quantityInput.value = 1;
    }

    // Hide confirmation section after 3 seconds
    setTimeout(() => {
        cancelPurchase();
    }, 3000);
}

// Cancel purchase
function cancelPurchase() {
    document.getElementById('purchaseConfirmSection').style.display = 'none';
    pendingPurchase = {
        supplier_id: null,
        material_id: null,
        quantity: 0,
        price: 0,
        supplier_name: '',
        material_name: ''
    };
}

// Initialize suppliers when WebSocket is ready
function initializeSuppliersWhenReady() {
    // Wait for WebSocket to be ready, then load suppliers
    const checkConnection = () => {
        if (webSocket.Socket && webSocket.Socket.readyState === WebSocket.OPEN) {
            loadSuppliers();
        } else {
            // Check again in 100ms
            setTimeout(checkConnection, 100);
        }
    };
    checkConnection();
}

//Update transactions table with data from server
function updateTransactionsTable(transactions) {
    const table = document.getElementById("transactionsTable");
    const tbody = table.querySelector('tbody');
    tbody.innerHTML = '';

    if (transactions && Array.isArray(transactions) && transactions.length > 0) {
        transactions.forEach(transaction => {
            const row = document.createElement('tr');

            // Format the date
            const dateFinished = new Date(transaction.date_finished).toLocaleDateString();

            row.innerHTML = `
                <td>${transaction.transaction_id || 'N/A'}</td>
                <td>${transaction.type || 'N/A'}</td>
                <td>${transaction.order_id || 'N/A'}</td>
                <td>${transaction.supplier_id || 'N/A'}</td>
                <td>${dateFinished}</td>
            `;
            tbody.appendChild(row);
        });
    } else {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td colspan="5" style="text-align: center; color: #999; padding: 2rem;">
                <em>No transactions found</em>
            </td>
        `;
        tbody.appendChild(row);
    }
}

//Missing updateTransitTable function
function updateTransitTable(transitData) {
    const table = document.getElementById("transitTable");
    const tbody = table.querySelector('tbody');
    tbody.innerHTML = '';

    if (transitData && Array.isArray(transitData) && transitData.length > 0) {
        transitData.forEach(transit => {
            const row = document.createElement('tr');

            // Format the dates
            const shipmentDate = new Date(transit.shipment_date).toLocaleDateString();
            const deliveryDate = new Date(transit.delivery_date).toLocaleDateString();

            // Determine status and color
            const isDone = transit.done === 1 || transit.done === true;
            const status = isDone ? 'Delivered' : 'In Transit';
            const statusColor = isDone ? '#28a745' : '#ffc107';

            row.innerHTML = `
                <td>${transit.transaction_id || 'N/A'}</td>
                <td>${transit.supplier_id || 'N/A'}</td>
                <td>${transit.order_id || 'N/A'}</td>
                <td>${shipmentDate}</td>
                <td>${deliveryDate}</td>
                <td style="color: ${statusColor}; font-weight: bold;">
                    ${status}
                </td>
            `;
            tbody.appendChild(row);
        });
    } else {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td colspan="6" style="text-align: center; color: #999; padding: 2rem;">
                <em>No transit data found</em>
            </td>
        `;
        tbody.appendChild(row);
    }
}

function upgradeToPlus() {

}

function startAlgorithm(producableGoodId) {
    // Send the JSON request
    const json = [{"start_algorithm": producableGoodId}];

    if (webSocket.Socket && webSocket.Socket.readyState === WebSocket.OPEN) {
        webSocket.Socket.send(JSON.stringify(json));
        console.log('Sent algorithm request:', json);
    } else {
        alert('Connection not ready. Please try again.');
    }
}

// Global variable to store last algorithm results
let lastBestCalculation = null;

function handleBestCalculation(bestCalculation, msg_JSON) {
    console.log('Received bestCalculation:', bestCalculation);

    // Store the last received data
    lastBestCalculation = bestCalculation;

    // Get the table body
    const tableBody = document.querySelector('#algorithmResultsTable tbody');

    // Clear existing rows
    tableBody.innerHTML = '';

    // Show the table
    document.getElementById('algorithmResultsTable').style.display = 'table';

    // Get suppliers and inventory data for name lookup
    const suppliers = msg_JSON["data"][0]["all_suppliers"] || [];
    const inventory = msg_JSON["data"][0]["inventory"] || [];

    // Create lookup maps
    const supplierNames = {};
    suppliers.forEach(supplier => {
        supplierNames[supplier.supplier_id] = supplier.name || `Supplier ${supplier.supplier_id}`;
    });

    const materialNames = {};
    inventory.forEach(item => {
        materialNames[item.id] = item.name || `Material ${item.id}`;
    });

    // Add rows for each result
    bestCalculation.forEach((result, index) => {
        for (const [materialId, supplierId] of Object.entries(result)) {
            const row = document.createElement('tr');

            // Get names or fallback to IDs
            const materialName = materialNames[materialId] || `Material ${materialId}`;
            const supplierName = supplierNames[supplierId] || `Supplier ${supplierId}`;

            row.innerHTML = `
                <td>Result ${index + 1}</td>
                <td>${materialName}</td>
                <td>${supplierName}</td>
            `;
            tableBody.appendChild(row);
        }
    });

    // Show the confirm button
    showConfirmButton();
}

function showConfirmButton() {
    // Check if button already exists
    let confirmBtn = document.getElementById('confirmAlgorithmBtn');

    if (!confirmBtn) {
        // Create the confirm button
        confirmBtn = document.createElement('div');
        confirmBtn.id = 'confirmAlgorithmBtn';
        confirmBtn.innerHTML = `
            <div style="margin-top: 1rem; text-align: center;">
                <button onclick="confirmAlgorithmPurchase()" 
                        style="padding: 0.75rem 1.5rem; background-color: #28a745; color: white; border: none; border-radius: 4px; font-size: 1rem; cursor: pointer;">
                      Confirm & Buy from Algorithm
                </button>
            </div>
        `;

        // Insert after the algorithm results table
        const table = document.getElementById('algorithmResultsTable');
        table.parentNode.insertBefore(confirmBtn, table.nextSibling);
    }

    confirmBtn.style.display = 'block';
}

function confirmAlgorithmPurchase() {
    if (!lastBestCalculation || lastBestCalculation.length === 0) {
        alert('No algorithm results to confirm');
        return;
    }

    // Create the JSON to send - just use the original bestCalculation data
    const json = [{
        "buy_from_algorithm": lastBestCalculation
    }];

    console.log('Sending buy_from_algorithm:', json);

    if (webSocket.Socket && webSocket.Socket.readyState === WebSocket.OPEN) {
        webSocket.Socket.send(JSON.stringify(json));

        // Show success message
        alert(`Purchasing materials from algorithm recommendations!`);

        // Hide the confirm button after sending
        document.getElementById('confirmAlgorithmBtn').style.display = 'none';
    } else {
        alert('Connection not ready. Please try again.');
    }
}

// Call initializeSuppliersWhenReady when page loads instead of loadSuppliers directly
document.addEventListener('DOMContentLoaded', function() {
    initializeSuppliersWhenReady();
});