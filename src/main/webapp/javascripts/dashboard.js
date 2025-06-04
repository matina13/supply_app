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

            if (msg_JSON["data"][0]["suppliers"] != null) {
                updateSuppliersTable(msg_JSON);
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

    // Highlight selected row
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

function updateSuppliersTable(msg_JSON) {
    try {
        const table = document.getElementById("suppliersTable");
        const tbody = table.querySelector('tbody');
        tbody.innerHTML = '';

        const suppliers = msg_JSON["data"]?.[0]?.["suppliers"];
        if (suppliers && Array.isArray(suppliers)) {
            suppliers.forEach(supplier => {
                const row = document.createElement('tr');
                row.className = 'clickable-row';
                row.innerHTML = `
                    <td>${supplier.supplier_name || supplier.supplier_id || 'Unknown Supplier'}</td>
                    <td>$${supplier.price || 0}</td>
                    <td>${supplier.quantity || 0}</td>
                    <td>
                        <button class="select-btn" 
                                onclick="selectSupplier(
                                    ${supplier.supplier_id}, 
                                    ${supplier.material_id},
                                    '${supplier.supplier_name || supplier.supplier_id}',
                                    '${supplier.material_name || supplier.material_id}',
                                    ${supplier.price},
                                    ${supplier.quantity},
                                    event
                                )">
                            Select
                        </button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        } else {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td colspan="4" style="text-align: center; color: #999;">
                    <em>No suppliers found for this material</em>
                </td>
            `;
            tbody.appendChild(row);
        }

        // Don't hide buy section here anymore
    } catch (error) {
        console.error('Error updating suppliers table:', error);
    }
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

    // Reset selection after purchase
    document.getElementById('buySection').style.display = 'none';
    document.getElementById('purchaseMessage').textContent = 'Purchase order sent!';
    setTimeout(() => {
        document.getElementById('purchaseMessage').textContent = '';
    }, 3000);
}

function produceGood() {
    const producableGoodId = 1;
    const quantityToProduce = 1;
    const json = [{
        "produce_good": {
            "producableGoodId": producableGoodId,
            "quantityToProduce": quantityToProduce
        }
    }];
    webSocket.Socket.send(JSON.stringify(json));
}