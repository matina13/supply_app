package org.example.demo.supplyChain.Transaction;

import org.example.demo.DBUtil;
import org.example.demo.structs.DoneObject;
import org.example.demo.supplyChain.TimeSimulator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class BuyMaterial {
    private int supplier_id;
    private int material_id;
    private int quantity;

    public void buy(int user_id, TimeSimulator timeSim) {
        LocalDate date = timeSim.getLocalDateObject();
        int money = timeSim.getMoney();
        int order_id = calculatePrice(user_id, money, date);
        watchOrder(user_id, order_id, timeSim);
    }

    private int calculatePrice(int user_id, int money, LocalDate date) {
        try {
            String sql = "SELECT price FROM SuppliersSellPrice WHERE supplier_id = ? AND material_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, this.supplier_id);
            stmt.setInt(2, this.material_id);
            ResultSet rs = stmt.executeQuery();

            int price = 0;
            if (rs.next()) {
                price = rs.getInt("price");
            }

            int moneyToBeSpent = this.quantity * price;

            boolean hasTheMoney = checkAndUpdateMoney(user_id, money, moneyToBeSpent);

            if (hasTheMoney) {
                subtractQuantityFromSupplier();
                int order_id = sendToTransit(user_id, date);
                createOrder(order_id);
                return order_id;
            }

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return 4040404; //not supposed to happen
    }

    private boolean checkAndUpdateMoney(int user_id, int money, int moneyToBeSpent) {
        if (money >= moneyToBeSpent) {
            try {
                String sql = "UPDATE UserData SET money = money - ? WHERE user_id = ?";
                PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
                stmt.setInt(1, moneyToBeSpent);
                stmt.setInt(2, user_id);
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    private void subtractQuantityFromSupplier() {
        try {
            String sql = "UPDATE SuppliersInventory SET quantity = quantity - ? WHERE supplier_id = ? AND material_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, this.quantity);
            stmt.setInt(2, this.supplier_id);
            stmt.setInt(3, this.material_id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int calculateDaysToTravel(int user_id, int supplier_id) {
        try {
            String sql = "SELECT location_x, location_y FROM UserData WHERE user_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, user_id);
            ResultSet rs = stmt.executeQuery();

            int producer_x = -10;
            int producer_y = -10;

            if (rs.next()) {
                producer_x = rs.getInt("location_x");
                producer_y = rs.getInt("location_y");
            }

            String sql2 = "SELECT location_x, location_y FROM Suppliers WHERE supplier_id = ?";
            PreparedStatement stmt2 = DBUtil.getConnection().prepareStatement(sql2);
            stmt2.setInt(1, supplier_id);
            ResultSet rs2 = stmt2.executeQuery();

            int supplier_x = -10;
            int supplier_y = -10;

            if (rs2.next()) {
                supplier_x = rs2.getInt("location_x");
                supplier_y = rs2.getInt("location_y");
            }

            int daysToTravel = TravelTimeCalculator.calculate(producer_x, producer_y, supplier_x, supplier_y);
            return daysToTravel;

        } catch(SQLException e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return 4040404;
    }

    private int sendToTransit(int user_id, LocalDate date) {
        try {
            String sql = "SELECT MAX(transaction_id), MAX(order_id) FROM Transit";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            int transaction_id = 0;
            int order_id = 0;

            if (rs.next()) {
                transaction_id = rs.getInt("MAX(transaction_id)") + 1;
                order_id = rs.getInt("MAX(order_id)") + 1;
            }

            String sql2 = "INSERT INTO Transit (transaction_id, supplier_id, buyer_id, order_id, shipment_date, delivery_date, done) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt2 = DBUtil.getConnection().prepareStatement(sql2);
            stmt2.setInt(1, transaction_id);
            stmt2.setInt(2, this.supplier_id);
            stmt2.setInt(3, user_id);
            stmt2.setInt(4, order_id);
            stmt2.setDate(5, Date.valueOf(date));
            stmt2.setDate(6, Date.valueOf(date.plusDays(calculateDaysToTravel(user_id, this.supplier_id)))); //Don't forget to make it properly l8r!!!!!!!!!!!!!!
            stmt2.setBoolean(7, false);
            stmt2.executeUpdate();

            return order_id;

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return 4040404; //not supposed to happen
    }

    private void createOrder(int order_id) {
        try {
            String sql = "INSERT INTO Orders (order_id, material_id, quantity) VALUES (?, ?, ?)";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, order_id);
            stmt.setInt(2, this.material_id);
            stmt.setInt(3, this.quantity);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTransaction(int transaction_id, String type, int order_id, int user_id, LocalDate date) {
        try {
            String sql = "INSERT INTO Transactions (transaction_id, type, order_id, supplier_id, buyer_id, date_finished, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, transaction_id);
            stmt.setString(2, type);
            stmt.setInt(3, order_id);
            stmt.setInt(4, this.supplier_id);
            stmt.setInt(5, user_id);
            stmt.setDate(6, Date.valueOf(date));
            stmt.setInt(7, user_id);
            stmt.executeUpdate();

            finishOrder(transaction_id);

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void finishOrder(int transaction_id) {
        try {
            String sql = "UPDATE Transit SET done = ? WHERE transaction_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setBoolean(1, true);
            stmt.setInt(2, transaction_id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addMaterialToInventory(int user_id) {
        try {
            // Check if user already has this material in inventory
            String checkSql = "SELECT COUNT(*) FROM ProducerInventory WHERE user_id = ? AND material_id = ? AND type = 'material'";
            PreparedStatement checkStmt = DBUtil.getConnection().prepareStatement(checkSql);
            checkStmt.setInt(1, user_id);
            checkStmt.setInt(2, this.material_id);
            ResultSet rs = checkStmt.executeQuery();

            int exists = 0;
            if (rs.next()) {
                exists = rs.getInt(1);
            }

            if (exists == 0) { // doesn't exist - add new material entry
                String insertSql = "INSERT INTO ProducerInventory (user_id, material_id, producableGoodId, quantity, type) VALUES (?, ?, NULL, ?, 'material')";
                PreparedStatement insertStmt = DBUtil.getConnection().prepareStatement(insertSql);
                insertStmt.setInt(1, user_id);
                insertStmt.setInt(2, this.material_id);
                insertStmt.setInt(3, this.quantity);
                insertStmt.executeUpdate();
            } else { // exists - update quantity
                String updateSql = "UPDATE ProducerInventory SET quantity = quantity + ? WHERE user_id = ? AND material_id = ? AND type = 'material'";
                PreparedStatement updateStmt = DBUtil.getConnection().prepareStatement(updateSql);
                updateStmt.setInt(1, this.quantity);
                updateStmt.setInt(2, user_id);
                updateStmt.setInt(3, this.material_id);
                updateStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ArrayList<Object> getOrderDetails(int order_id) {
        try {
            LocalDate delivery_date_LocalDate;
            int transaction_id;
            ArrayList<Object> date_N_transaction_id = new ArrayList<>();
            String sql = "SELECT transaction_id, delivery_date FROM Transit WHERE order_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, order_id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Date delivery_date = rs.getDate("delivery_date");
                delivery_date_LocalDate = Instant.ofEpochMilli(delivery_date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                transaction_id = rs.getInt("transaction_id");
                date_N_transaction_id.add(delivery_date_LocalDate);
                date_N_transaction_id.add(transaction_id);

                return date_N_transaction_id;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void watchOrder(int user_id, int order_id, TimeSimulator timeSim) {
        ArrayList<Object> details = getOrderDetails(order_id);
        LocalDate delivery_date = (LocalDate) details.get(0);
        int transaction_id = (int) details.get(1);

        Timer t = new Timer();

        DoneObject done = new DoneObject(false);

        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (delivery_date.isEqual(timeSim.getLocalDateObject())) {
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
                    createTransaction(transaction_id, "inbound", order_id, user_id, timeSim.getLocalDateObject());
                    addMaterialToInventory(user_id);
                    awaitTimer.cancel();
                    awaitTimer.purge();
                }
            }
        }, 0, 1000); // every 1 second
    }

}
