package org.example.demo.supplyChain;

import org.example.demo.DBUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


public class RandomnessSimulator {
    private ArrayList<Integer> suppliersList;
    private ArrayList<Integer> materialsList;

    public RandomnessSimulator() {
        this.suppliersList = new ArrayList<Integer>();
        this.materialsList = new ArrayList<Integer>();
        try {
            String sql = "SELECT supplier_id FROM Suppliers";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                this.suppliersList.add(rs.getInt("supplier_id"));
            }

            String sql2 = "SELECT material_id FROM Materials";
            PreparedStatement stmt2 = DBUtil.getConnection().prepareStatement(sql2);
            ResultSet rs2 = stmt2.executeQuery();

            while (rs2.next()) {
                this.materialsList.add(rs2.getInt("material_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void simulate() {
        int priceFluctuation = ThreadLocalRandom.current().nextInt(-5,6); //price fluctuation from -5 to 5
        int randomSupplier = ThreadLocalRandom.current().nextInt(0, this.suppliersList.size() + 1);
        int randomMaterial = ThreadLocalRandom.current().nextInt(0, this.materialsList.size() + 1);

        updateSupplierPrices(priceFluctuation, randomSupplier, randomMaterial);
    }

    private void updateSupplierPrices(int priceFluctuation, int supplier_id, int material_id) {
        try {
            String sql = "UPDATE SuppliersSellPrice SET price = price + ? WHERE supplier_id = ? AND material_id = ?";
            PreparedStatement stmt = DBUtil.getConnection().prepareStatement(sql);
            stmt.setInt(1, priceFluctuation);
            stmt.setInt(2, supplier_id);
            stmt.setInt(3, material_id);
            stmt.executeUpdate();


        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }




}
