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
        //console.log('Info: WebSocket connection opened.');
        webSocket.Socket.send(document.getElementById("email").innerText);

        //initializeInventoryTable();
    };

    webSocket.Socket.onclose = function () {
        console.log('Info: WebSocket closed.');
    };

    webSocket.Socket.onmessage = function (message) {
        const msg_JSON = JSON.parse(message.data);

        updateInventoryTable(msg_JSON);
    };
});

webSocket.initialize = function() {
    if (window.location.protocol == 'http:') {
        webSocket.connect('ws://' + window.location.host + '/appSocket');
    } else {
        webSocket.connect('wss://' + window.location.host + '/appSocket');
    }
};

webSocket.initialize();

/*
function initializeInventoryTable() {
    const table = document.getElementById("inventoryTable");
    let row = table.insertRow();
    let name = row.insertCell(0);
    name.innerHTML = item.name;
    let quantity = row.insertCell(1);
    quantity.innerHTML = item.quantity;
}*/

function updateInventoryTable(msg_JSON) {
    document.getElementById("date").innerText = msg_JSON["date"];
    const table = document.getElementById("inventoryTable");

    const inventory = msg_JSON["data"][0]["inventory"];

    inventory.forEach( item => {
        let row = table.insertRow();
        let name = row.insertCell(0);
        name.innerHTML = item.name;
        let quantity = row.insertCell(1);
        quantity.innerHTML = item.quantity;
    }
    )
}