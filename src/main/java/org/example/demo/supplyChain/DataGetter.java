package org.example.demo.supplyChain;

import org.example.demo.DBUtil;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.example.demo.structs.*;

public class DataGetter {

    public ArrayList<Inventory> getInventory(int user_id) {
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

        //return new InventoryWrapper(inventory);
        return inventory;
    }

    public static ArrayList<ProducableGood> getProducableGoods() {
        ArrayList<ProducableGood> goods = new ArrayList<ProducableGood>();
        try {
            String sql = "SELECT * FROM ProducableGoods";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("producableGoodId");
                String name = rs.getString("name");
                goods.add(new ProducableGood(id, name));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching producable goods: " + e.getMessage());
            e.printStackTrace();
        }
        return goods;
    }

    public ArrayList<Supplier> getSuppliers() {
        ArrayList<Supplier> suppliers = new ArrayList<Supplier>();
        try {
            String sql = "SELECT supplier_id, name, country FROM Suppliers";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int supplier_id = rs.getInt("supplier_id");
                String name = rs.getString("name");
                String country = rs.getString("country");
                suppliers.add(new Supplier(supplier_id, name, country));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return suppliers;
    }

    public ArrayList<SupplierMaterialInfo> getSupplierCatalogue(int supplier_id) {
        ArrayList<SupplierMaterialInfo> catalogue = new ArrayList<SupplierMaterialInfo>();
        try {
            String sql = "SELECT si.material_id, si.quantity, ssp.price FROM SuppliersInventory si JOIN SuppliersSellPrice ssp ON si.material_id = ssp.material_id WHERE si.supplier_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, supplier_id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int material_id = rs.getInt("material_id");
                int quantity = rs.getInt("quantity");
                int price = rs.getInt("price");
                catalogue.add(new SupplierMaterialInfo(supplier_id, material_id, price, quantity));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return catalogue;
    }

    public ArrayList<SupplierMaterialInfo> getSuppliersThatSellMaterial(int material_id) {
        ArrayList<SupplierMaterialInfo> suppliers = new ArrayList<SupplierMaterialInfo>();
        try {
            String sql = "SELECT supplier_id, quantity FROM SuppliersInventory WHERE material_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, material_id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int supplier_id = rs.getInt("supplier_id");
                int price = getSupplierSellPrice(supplier_id, material_id);
                int quantity = rs.getInt("quantity");
                suppliers.add(new SupplierMaterialInfo(supplier_id, material_id, price, quantity));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return suppliers;
    }

    private int getSupplierSellPrice(int supplier_id, int material_id) {
        try {
            String sql = "SELECT price FROM SuppliersSellPrice WHERE supplier_id = ? AND material_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, supplier_id);
            stmt.setInt(2, material_id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("price");
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return 404040404; //should not happen
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

    public ArrayList<Transaction> getTransactions(int user_id) {
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        try {
            String sql = "SELECT * FROM Transactions WHERE user_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, user_id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int transaction_id = rs.getInt("transaction_id");
                String type = rs.getString("type");
                int order_id = rs.getInt("order_id");
                int supplier_id = rs.getInt("supplier_id");
                int buyer_id = rs.getInt("buyer_id");
                Date date_finished = rs.getDate("date_finished");
                transactions.add(new Transaction(transaction_id, type, order_id, supplier_id, buyer_id, date_finished));
            }

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    public ArrayList<Order> getOrders(int order_id) {
        ArrayList<Order> orders = new ArrayList<Order>();
        try {
            String sql = "SELECT * FROM Orders WHERE order_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, order_id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int material_id = rs.getInt("material_id");
                int quantity = rs.getInt("quantity");
                orders.add(new Order(order_id, material_id, quantity));
            }

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

}