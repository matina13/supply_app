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
            document.getElementById("date").innerText = msg_JSON["date"]; //update date
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

        // Clear existing rows (keep header row if it exists)
        const rowCount = table.rows.length;
        for (let i = rowCount - 1; i > 0; i--) {
            table.deleteRow(i);
        }

        // Check if inventory data exists
        const inventory = msg_JSON["data"]?.[0]?.["inventory"];
        if (inventory && Array.isArray(inventory)) {
            inventory.forEach(item => {
                let row = table.insertRow();
                let name = row.insertCell(0);
                name.innerHTML = item.name || 'Unknown Item';
                let quantity = row.insertCell(1);
                quantity.innerHTML = item.quantity || 0;
            });
        } else {
            // Show message if no inventory data
            let row = table.insertRow();
            let cell = row.insertCell(0);
            cell.colSpan = 2;
            cell.innerHTML = '<em>No inventory data available</em>';
            cell.style.textAlign = 'center';
            cell.style.color = '#999';
        }
    } catch (error) {
        console.error('Error updating inventory table:', error);
    }
}

function getSuppliers() {
    //const e = document.getElementById("prodGoods");
    //const material_id = e.value;

    //const id = e.options[e.selectedIndex].value;
    const material_id = 1;
    const json = [{"material_id": material_id}];

    webSocket.Socket.send(JSON.stringify(json));
}

function updateSuppliersTable(msg_JSON) {
    try {
        const table = document.getElementById("suppliersTable");

        // Clear existing rows (keep header row if it exists)
        const rowCount = table.rows.length;
        for (let i = rowCount - 1; i > 0; i--) {
            table.deleteRow(i);
        }

        // Check if inventory data exists
        const inventory = msg_JSON["data"]?.[0]?.["suppliers"];
        if (inventory && Array.isArray(inventory)) {
            inventory.forEach(item => {
                let row = table.insertRow();
                let sup_name = row.insertCell(0);
                sup_name.innerHTML = item.supplier_id || 'Unknown Item';
                let price = row.insertCell(1);
                price.innerHTML = item.price || 0;
                let quantity = row.insertCell(2);
                quantity.innerHTML = item.quantity || 0;
            });
        } else {
            // Show message if no inventory data
            let row = table.insertRow();
            let cell = row.insertCell(0);
            cell.colSpan = 2;
            cell.innerHTML = '<em>No inventory data available</em>';
            cell.style.textAlign = 'center';
            cell.style.color = '#999';
        }
    } catch (error) {
        console.error('Error updating inventory table:', error);
    }}

function buyMaterial() {
    const supplier_id = 1;
    const material_id = 1;
    const quantity = 1;

    //maybe l8r add way to make multiple orders from multiple suppliers

    const json = [{"buy_material": {"supplier_id": supplier_id, "material_id": material_id, "quantity": quantity}}];
    webSocket.Socket.send(JSON.stringify(json));
}