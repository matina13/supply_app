package org.example.demo;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/appSocket")
public class appWebSocket {
    private static final String GUEST_PREFIX = "Guest";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);
    private static final Set<appWebSocket> connections = new CopyOnWriteArraySet<>();

    private Queue<String> messageBacklog = new ArrayDeque<>();
    private boolean messageInProgress = false;

    //private final String nickname;
    private Session session;

    @OnOpen
    public void start(Session session) {
        this.session = session;
        connections.add(this);
        broadcast("Successfully connected.");
    }

    @OnClose
    public void end() {
        connections.remove(this);
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
        //log.error("Chat Error: " + t.toString(), t);
    }

    private void sendMessage(String msg) throws IOException {

        synchronized (this) {
            if (messageInProgress) {
                messageBacklog.add(msg);
                return;
            } else {
                messageInProgress = true;
            }
        }

        boolean queueHasMessagesToBeSent = true;

        String messageToSend = msg;
        do {
            session.getBasicRemote().sendText(messageToSend);
            synchronized (this) {
                messageToSend = messageBacklog.poll();
                if (messageToSend == null) {
                    messageInProgress = false;
                    queueHasMessagesToBeSent = false;
                }
            }

        } while (queueHasMessagesToBeSent);
    }

    private static void broadcast(String msg) {
        for (appWebSocket client : connections) {
            try {
                client.sendMessage(msg);
            } catch (IOException e) {
                //log.debug("Chat Error: Failed to send message to client", e);
                if (connections.remove(client)) {
                    try {
                        client.session.close();
                    } catch (IOException e1) {
                        // Ignore
                    }
                    //String message = String.format("* %s %s", client.nickname, "has been disconnected.");
                    //broadcast(message);
                }
            }
        }
    }

}
