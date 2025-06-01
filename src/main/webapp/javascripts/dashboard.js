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
        webSocket.Socket.send(document.getElementById("email").innerText);
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
            updateInventoryTable(msg_JSON);
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

