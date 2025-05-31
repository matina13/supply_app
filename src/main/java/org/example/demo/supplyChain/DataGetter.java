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
                int id;
                try {
                    id = rs.getInt("material_id");
                } catch (SQLException e) {
                    id = rs.getInt("producableGoodId");
                }
                int quantity = rs.getInt("quantity");
                String type = rs.getString("type");
                inventory.add(new Inventory(id, quantity, getMaterialOrGoodName(type, id)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
        return goods;
    }

    public String getMaterialOrGoodName(String type, int id) {
        String name = "";
        try {
            String sql = "";
            if (type.equals("material")) sql = "SELECT name FROM Materials WHERE material_id = ?";
            else if (type.equals("good")) sql = "SELECT name FROM ProducableGoods WHERE producableGoodId = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                name = rs.getString("name");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return name;
    }

    public class InventoryWrapper {
        ArrayList<Inventory> inventory;

        public InventoryWrapper(ArrayList<Inventory> inv) {
            //this.inventory = inv;
            //Gson g = new Gson();
            //this.inventory = g.toJsonTree(inv).getAsJsonArray();
            this.inventory = inv;
        }

        public JsonArray getInventoryAsJson() {
            Gson g = new Gson();
            JsonArray json = g.toJsonTree(this.inventory).getAsJsonArray();
            return json;
        }

    }
}


