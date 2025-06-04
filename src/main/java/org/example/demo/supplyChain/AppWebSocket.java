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
import org.example.demo.structs.Supplier;
import org.example.demo.structs.SupplierMaterialInfo;
import org.example.demo.structs.Transaction;
import org.example.demo.supplyChain.Transaction.BuyMaterial;
import org.example.demo.structs.Json;
import org.example.demo.supplyChain.Transaction.Produce;

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
    private HashMap<String, Object> dataToBeSent;
    private ArrayList<String> dTBS_Clear_List;
    private RandomnessSimulator randSim;

    @OnOpen
    public void start(Session session) {
        this.session = session;
        connections.add(this);
        this.dataGetter = new DataGetter();
        this.dataToBeSent = new HashMap<String, Object>();
        this.dTBS_Clear_List = new ArrayList<String>();
        this.randSim = new RandomnessSimulator();
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
            int days = 0;
            @Override
            public void run() {
                timeSim.incrementDate();
                if (days == 5) {
                    randSim.simulate();
                    days = 0;
                }
                else days++;

                Gson g = new Gson();

                dataToBeSent.put("inventory", dataGetter.getInventory(user_id));
                dataToBeSent.put("transactions", dataGetter.getTransactions(user_id));
                String json = g.toJson(new Json(timeSim.getDate(), timeSim.getMoney(), dataToBeSent));

                broadcast(json);

                for (String toBeCleared : dTBS_Clear_List) {
                    dataToBeSent.remove(toBeCleared);
                }
                dTBS_Clear_List.clear();
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
            this.dataToBeSent.put("suppliers_that_sell_material", this.dataGetter.getSuppliersThatSellMaterial(material_id));
        }
        else if (j.get("buy_material") != null) {
            BuyMaterial buyMaterials = new Gson().fromJson(j.get("buy_material"), BuyMaterial.class);
            buyMaterials.buy(this.user_id, this.timeSim);
        }
        else if (j.get("produce_good") != null) {
            try {
                Produce produce = new Gson().fromJson(j.get("produce_good"), Produce.class);
                produce.produce(this.user_id, this.timeSim);
                // If we get here, production was successful
                this.dataToBeSent.put("alert_message", "Production started successfully!");
            } catch (RuntimeException e) {
                // If exception thrown, production failed
                this.dataToBeSent.put("alert_message", e.getMessage());
            }

            this.dTBS_Clear_List.add("alert_message");
        }

        else if (j.get("get_suppliers") != null) {
            ArrayList<Supplier> suppliers = dataGetter.getSuppliers();
            ArrayList<HashMap<String, Object>> supplierData = new ArrayList<>();

            for (Supplier s : suppliers) {
                HashMap<String, Object> supplier = new HashMap<>();
                supplier.put("supplier_id", s.getSupplier_id());
                supplier.put("name", s.getName());
                supplier.put("country", s.getCountry());
                supplierData.add(supplier);
            }

            this.dataToBeSent.put("all_suppliers", supplierData);
            this.dTBS_Clear_List.add("all_suppliers");
        }
        else if (j.get("get_supplier_materials") != null) {
            JsonObject supplierRequest = j.get("get_supplier_materials").getAsJsonObject();
            int supplier_id = supplierRequest.get("supplier_id").getAsInt();

            ArrayList<SupplierMaterialInfo> catalogue = dataGetter.getSupplierCatalogue(supplier_id);
            ArrayList<HashMap<String, Object>> materials = new ArrayList<>();

            for (SupplierMaterialInfo info : catalogue) {
                HashMap<String, Object> material = new HashMap<>();
                material.put("material_id", info.getMaterial_id());
                material.put("material_name", dataGetter.getMaterialOrGoodName("material", info.getMaterial_id()));
                material.put("quantity", info.getQuantity());
                material.put("price", info.getPrice());
                material.put("supplier_id", supplier_id);
                materials.add(material);
            }

            this.dataToBeSent.put("supplier_materials", materials);
            this.dataToBeSent.put("selected_supplier_id", supplier_id);
            this.dTBS_Clear_List.add("supplier_materials");
            this.dTBS_Clear_List.add("selected_supplier_id");
        }
        else if (j.get("get_transactions") != null) {
            ArrayList<Transaction> transactions = dataGetter.getTransactions(this.user_id);
            ArrayList<HashMap<String, Object>> transactionData = new ArrayList<>();

            for (Transaction t : transactions) {
                HashMap<String, Object> transaction = new HashMap<>();
                transaction.put("transaction_id", t.getTransaction_id());
                transaction.put("type", t.getType());
                transaction.put("order_id", t.getOrder_id());
                transaction.put("supplier_id", t.getSupplier_id());
                transaction.put("buyer_id", t.getBuyer_id());
                transaction.put("date_finished", t.getDate_finished().toString());
                transactionData.add(transaction);
            }

            this.dataToBeSent.put("transactions_list", transactionData);
            this.dTBS_Clear_List.add("transactions_list");
        }
        else if (j.get("get_transit") != null) {
            ArrayList<HashMap<String, Object>> transitData = dataGetter.getTransit(this.user_id);
            this.dataToBeSent.put("transit_list", transitData);
            this.dTBS_Clear_List.add("transit_list");
        }

    }

}
