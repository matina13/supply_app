var webSocket = {};

webSocket.socket = null;

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
    };

    webSocket.Socket.onclose = function () {
        console.log('Info: WebSocket closed.');
    };

    webSocket.Socket.onmessage = function (message) {
        //console.log(message.data);
        document.getElementById("date").innerText = message.data
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