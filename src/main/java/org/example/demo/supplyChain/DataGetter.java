package org.example.demo.supplyChain;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.example.demo.DBUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.example.demo.structs.*;

public class DataGetter {

    public InventoryWrapper getInventory(int user_id) {
        ArrayList<Inventory> inventory = new ArrayList<Inventory>();
        try {
            String sql = "SELECT material_id, producableGoodId, quantity, type FROM ProducerInventory WHERE user_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, user_id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = 0;
                String type = rs.getString("type");

                // More robust way to get the correct ID based on type
                if ("material".equals(type)) {
                    id = rs.getInt("material_id");
                } else if ("good".equals(type)) {
                    id = rs.getInt("producableGoodId");
                } else {
                    // Handle unknown type - log warning and skip
                    System.out.println("Warning: Unknown inventory type: " + type);
                    continue;
                }

                int quantity = rs.getInt("quantity");
                String name = getMaterialOrGoodName(type, id);

                // Only add if we got a valid name
                if (name != null && !name.isEmpty()) {
                    inventory.add(new Inventory(id, quantity, name, type));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching inventory for user " + user_id + ": " + e.getMessage());
            e.printStackTrace();
            // Return empty inventory instead of crashing
        }

        return new InventoryWrapper(inventory);
    }

    public ArrayList<ProducableGood> getProducableGoods() {
        ArrayList<ProducableGood> goods = new ArrayList<ProducableGood>();
        try {
            String sql = "SELECT * FROM ProducableGoods";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                goods.add(new ProducableGood(id, name));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching producable goods: " + e.getMessage());
            e.printStackTrace();
        }
        return goods;
    }

    public String getMaterialOrGoodName(String type, int id) {
        String name = "";
        try {
            String sql = "";
            if ("material".equals(type)) {
                sql = "SELECT name FROM Materials WHERE material_id = ?";
            } else if ("good".equals(type)) {
                sql = "SELECT name FROM ProducableGoods WHERE producableGoodId = ?";
            } else {
                System.out.println("Warning: Unknown type for name lookup: " + type);
                return "Unknown Item";
            }

            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                name = rs.getString("name");
            } else {
                System.out.println("Warning: No name found for " + type + " with id: " + id);
                name = "Unknown " + type;
            }
        } catch (SQLException e) {
            System.err.println("Error getting name for " + type + " with id " + id + ": " + e.getMessage());
            e.printStackTrace();
            return "Error Loading Name";
        }
        return name;
    }

    public static class InventoryWrapper {
        private ArrayList<Inventory> inventory;

        public InventoryWrapper(ArrayList<Inventory> inv) {
            this.inventory = inv != null ? inv : new ArrayList<>();
        }

        public ArrayList<Inventory> getInventory() {
            return inventory;
        }

        public JsonArray getInventoryAsJson() {
            Gson g = new Gson();
            return g.toJsonTree(this.inventory).getAsJsonArray();
        }

        public int getTotalItems() {
            return inventory.size();
        }

        public int getTotalQuantity() {
            return inventory.stream().mapToInt(Inventory::getQuantity).sum();
        }
    }
}