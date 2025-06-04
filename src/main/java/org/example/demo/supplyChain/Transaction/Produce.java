package org.example.demo.supplyChain.Transaction;

import org.example.demo.DBUtil;
import org.example.demo.structs.DoneObject;
import org.example.demo.supplyChain.TimeSimulator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Produce {
    private int producableGoodId;
    private int quantityToProduce;

    // Keep your produce method as void, just throw an exception when materials are insufficient
    public void produce(int user_id, TimeSimulator timeSim) {
        // First, check if user has enough materials for the total quantity
        HashMap<Integer, Integer> allMaterials = getAllMaterialsNeededToProduce();

        // Validate materials before starting production
        for (HashMap.Entry<Integer, Integer> entry : allMaterials.entrySet()) {
            int material_id = entry.getKey();
            int requiredPerUnit = entry.getValue();
            int totalRequired = requiredPerUnit * quantityToProduce;

            // Check current inventory for this material
            int availableQuantity = getCurrentMaterialQuantity(user_id, material_id);

            if (availableQuantity < totalRequired) {
                throw new RuntimeException("Not enough materials! Please buy more materials from suppliers.");
            }
        }

        // If we reach here, user has enough materials for all units
        for (int i = 0; i < quantityToProduce; i++) {
            subtractMaterialsFromInventory(user_id, allMaterials);
            LocalDate end_date = addGoodToProduction(producableGoodId, timeSim);
            watchProduction(user_id, end_date, timeSim);
        }
    }

    private HashMap<Integer, Integer> getAllMaterialsNeededToProduce() {
        HashMap<Integer, Integer> allMaterials = new HashMap<>();
        try {
            String sql = "SELECT material_id, quantity FROM MaterialsNeededToProduceGood WHERE producable_good_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, this.producableGoodId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int material_id = rs.getInt("material_id");
                int quantity = rs.getInt("quantity");
                allMaterials.put(material_id, quantity);
            }

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return allMaterials;
    }

    private void subtractMaterialsFromInventory(int user_id, HashMap<Integer, Integer> materials) {
        materials.forEach( (material_id, quantity) -> {
            try {
                String sql = "UPDATE ProducerInventory SET quantity = quantity - ? WHERE material_id = ? AND user_id = ?";
                PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
                stmt.setInt(1, quantity);
                stmt.setInt(2, material_id);
                stmt.setInt(3, user_id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private LocalDate addGoodToProduction(int producableGoodId, TimeSimulator timeSim) {
        try {
            String sql = "SELECT daysToProduce FROM ProducableGoods WHERE producableGoodId = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, producableGoodId);
            ResultSet rs = stmt.executeQuery();

            int daysToProduce = 0;
            if (rs.next()) {
                daysToProduce = rs.getInt("daysToProduce");
            }

            String sql2 = "INSERT INTO GoodInProduction (producable_good_id, start_date, end_date) VALUES (?, ?, ?)";
            PreparedStatement stmt2 = DBUtil.getConnection().prepareStatement(sql2);
            stmt2.setInt(1, producableGoodId);
            LocalDate today = timeSim.getLocalDateObject();
            LocalDate end_date = today.plusDays(daysToProduce);
            stmt2.setDate(2, Date.valueOf(today));
            stmt2.setDate(3, Date.valueOf(end_date));
            stmt2.executeUpdate();

            return end_date;

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null; //should not happen
    }

    private void watchProduction(int user_id, LocalDate end_date, TimeSimulator timeSim) {
        Timer t = new Timer();

        DoneObject done = new DoneObject(false);

        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (end_date.isEqual(timeSim.getLocalDateObject())) {
                    done.setDone(true);
                    t.cancel();
                    t.purge();
                }
            }
        }, 0, 1000); // every 1 second

        Timer awaitTimer = new Timer();

        awaitTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (done.getDone()) {
                    addProducedGoodToInventory(user_id);
                    awaitTimer.cancel();
                    awaitTimer.purge();
                }
            }
        }, 0, 1000); // every 1 second
    }

    private void addProducedGoodToInventory(int user_id) {
        try {
            String sql = "SELECT producableGoodId FROM ProducerInventory WHERE user_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, user_id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) { //if produced good exists in the table
                String sql2 = "UPDATE ProducerInventory SET quantity = quantity + 1 WHERE producableGoodId = ? AND user_id = ?";
                PreparedStatement stmt2 = DBUtil.getConnection().prepareStatement(sql2);
                stmt2.setInt(1, producableGoodId);
                stmt2.setInt(2, user_id);
                stmt2.executeUpdate();
            }
            else { //if produced good doesn't exist in the table
                String sql2 = "INSERT INTO ProducerInventory (user_id, producableGoodId, quantity, type) VALUES (?, ?, ?, ?) ";
                PreparedStatement stmt2 = DBUtil.getConnection().prepareStatement(sql2);
                stmt2.setInt(1, user_id);
                stmt2.setInt(2, producableGoodId);
                stmt2.setInt(3, 1);
                stmt2.setString(4, "good");
                stmt2.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Add this helper method to check current material quantity
    private int getCurrentMaterialQuantity(int user_id, int material_id) {
        try {
            String sql = "SELECT quantity FROM ProducerInventory WHERE user_id = ? AND material_id = ? AND type = 'material'";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, user_id);
            stmt.setInt(2, material_id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("quantity");
            }
        } catch (SQLException e) {
            System.err.println("Error checking material quantity: " + e.getMessage());
            e.printStackTrace();
        }
        return 0; // Return 0 if material not found or error occurred
    }

}

