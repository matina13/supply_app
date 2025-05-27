<html>
<head>
    <title>Apache Tomcat WebSocket Examples: Chat</title>

    <script type="text/javascript">
    var Chat = {};

    Chat.socket = null;

    Chat.connect = (function(host) {
        if ('WebSocket' in window) {
            Chat.Socket = new WebSocket(host);
        } else if ('MozWebSocket' in window) {
            Chat.Socket = new MozWebSocket(host);
        } else {
            console.log('Error: WebSocket is not supported by this browser.');
            return;
        }

        Chat.Socket.onopen = function () {
            //console.log('Info: WebSocket connection opened.');
        };

        Chat.Socket.onclose = function () {
            console.log('Info: WebSocket closed.');
        };

        Chat.Socket.onmessage = function (message) {
            console.log(message.data);
        };
    });

    Chat.initialize = function() {
        if (window.location.protocol == 'http:') {
            Chat.connect('ws://' + window.location.host + '/appSocket');
        } else {
            Chat.connect('wss://' + window.location.host + '/appSocket');
        }
    };

    Chat.initialize();

    </script>

</head>

<body>

<div>
    <p>Test. Check console.</p>
</div>

</body>
</html>