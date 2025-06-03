package org.example.demo.supplyChain;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.example.demo.DBUtil;

import com.google.gson.*;
import org.example.demo.supplyChain.Transaction.BuyMaterial;
import org.example.demo.structs.Json;

@ServerEndpoint(value = "/appSocket")
public class AppWebSocket {
    private static final String GUEST_PREFIX = "Guest";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);
    private static final Set<AppWebSocket> connections = new CopyOnWriteArraySet<>();

    private Queue<String> messageBacklog = new ArrayDeque<>();
    private boolean messageInProgress = false;

    //private final String nickname;
    private Session session;

    private TimeSimulator timeSim;
    private Timer t;
    private int user_id;
    private DataGetter dataGetter;
    private HashMap<String, Object> dataToBeSent = new HashMap<String, Object>();

    @OnOpen
    public void start(Session session) {
        this.session = session;
        connections.add(this);
        this.dataGetter = new DataGetter();
        //broadcast("Successfully connected.");
    }

    @OnClose
    public void end() {
        stopTimeSimulation();
        this.timeSim.saveDate();
        connections.remove(this);
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
        System.out.println("Websocket error: " + t.getMessage());
        t.printStackTrace();
    }

    @OnMessage
    public void incoming(String message) {
        JsonArray messageJson = JsonParser.parseString(message).getAsJsonArray();
        for (JsonElement j : messageJson) {
            JsonObject obj = j.getAsJsonObject();
            cases(obj);
        }
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
        for (AppWebSocket client : connections) {
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

    private void startTimeSimulation(int user_id) {
        this.timeSim = new TimeSimulator();
        this.timeSim.init(user_id);

        this.t = new Timer();

        this.t.schedule(new TimerTask() {
            @Override
            public void run() {
                timeSim.incrementDate();

                Gson g = new Gson();

                dataToBeSent.put("inventory", dataGetter.getInventory(user_id));
                String json = g.toJson(new Json(timeSim.getDate(), timeSim.getMoney(), dataToBeSent));

                broadcast(json);
            }
        }, 0, 1000); // every 1 second
    }

    private void stopTimeSimulation() {
        this.t.cancel();
    }

    private void cases(JsonObject j) {
        if (j.get("email") != null) {
            try {
                String email = j.get("email").getAsString();
                this.user_id = DBUtil.getRegisteredUserId(DBUtil.getConnection(), email);
                startTimeSimulation(this.user_id);
            } catch (SQLException e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        else if (j.get("material_id") != null) {
            String material_id_string = j.get("material_id").getAsString();
            int material_id = Integer.parseInt(material_id_string);
            this.dataToBeSent.put("suppliers", dataGetter.getSuppliersThatSellMaterial(material_id));
        }
        else if (j.get("buy_material") != null) {
            BuyMaterial buyMaterials = new Gson().fromJson(j.get("buy_material"), BuyMaterial.class);
            buyMaterials.buy(this.user_id, this.timeSim);
        }

    }

}
