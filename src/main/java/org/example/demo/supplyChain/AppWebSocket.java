package org.example.demo.supplyChain;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.example.demo.Algorithm;
import org.example.demo.DBUtil;

import com.google.gson.*;
import org.example.demo.structs.*;
import org.example.demo.supplyChain.Transaction.BuyMaterial;
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
    private Algorithm algorithm;
    private boolean algorithmTrigger;

    @OnOpen
    public void start(Session session) {
        this.session = session;
        connections.add(this);
        this.dataGetter = new DataGetter();
        this.dataToBeSent = new HashMap<String, Object>();
        this.dTBS_Clear_List = new ArrayList<String>();
        this.randSim = new RandomnessSimulator();
        this.algorithmTrigger = false;
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
                if (connections.remove(client)) {
                    try {
                        client.session.close();
                    } catch (IOException e1) {
                        // Ignore
                    }
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
                    randSim.simulatePrices();
                    randSim.simulateSupplierRestocking();
                    days = 0;
                }
                else days++;

                Gson g = new Gson();

                dataToBeSent.put("inventory", dataGetter.getInventory(user_id));
                dataToBeSent.put("transactions_list", dataGetter.getTransactions(user_id));
                dataToBeSent.put("all_suppliers", dataGetter.getSuppliers()); //why?
                dataToBeSent.put("transit_list", dataGetter.getTransit(user_id));
                String json = g.toJson(new Json(timeSim.getDate(), timeSim.getMoney(), dataToBeSent));

                if (algorithmTrigger) {
                    ArrayList<HashMap<Integer, Integer>> bestCalculation = algorithm.start();
                    dataToBeSent.put("bestCalculation", bestCalculation);
                }

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

            this.dataToBeSent.put("all_suppliers", suppliers);
            this.dTBS_Clear_List.add("all_suppliers");
        }

        else if (j.get("get_supplier_materials") != null) {
            JsonObject supplierRequest = j.get("get_supplier_materials").getAsJsonObject();
            int supplier_id = supplierRequest.get("supplier_id").getAsInt();
            ArrayList<SupplierMaterialInfo> supplierCatalogue = dataGetter.getSupplierCatalogue(supplier_id);

            this.dataToBeSent.put("supplier_materials", supplierCatalogue);
            this.dataToBeSent.put("selected_supplier_id", supplier_id);
        }

        else if (j.get("start_algorithm") != null) {
            JsonElement producableGoodId = j.get("start_algorithm");
            ArrayList<Integer> materials_ids = dataGetter.getMaterialsNeededToProduceItem(producableGoodId.getAsInt());
            this.algorithm = new Algorithm(this.user_id, this.dataGetter, materials_ids);
            this.algorithmTrigger = true;
        }

        else if (j.get("buy_from_algorithm") != null) {
            JsonArray algo_data = j.get("buy_from_algorithm").getAsJsonArray();
            for (JsonElement je : algo_data) {
                System.out.println(je.getAsJsonObject());
                AlgoStruct[] mat_idAnd_sup_id = new Gson().fromJson(je, AlgoStruct[].class);

                //order with these, we couldn't finish last step on time
            }

        }
    }

}