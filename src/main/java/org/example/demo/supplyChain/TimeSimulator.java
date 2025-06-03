package org.example.demo.supplyChain;

import com.google.gson.Gson;
import org.example.demo.DBUtil;
import org.example.demo.structs.Json;
import org.example.demo.supplyChain.Transaction.BuyMaterial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.sql.Date;

import java.time.LocalDate;

public class TimeSimulator {
    private LocalDate date;
    private Connection db;
    private int user_id;
    private int money;

    public TimeSimulator() {
        try {
            this.db = DBUtil.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void init(int user_id) {
        try {
            this.user_id = user_id;
            String sql = "SELECT money, last_date FROM UserData WHERE user_id = ?";
            PreparedStatement stmt = this.db.prepareStatement(sql);
            stmt.setInt(1, this.user_id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Date savedDate = rs.getDate("last_date");
                this.date = Instant.ofEpochMilli(savedDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                this.money = rs.getInt("money");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void incrementDate() {
        this.date = this.date.plusDays(1);
    }
    
    public String getDate() {
        return this.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public LocalDate getLocalDateObject() {
        return this.date;
    }

    public int getMoney() {
        return this.money;
    }

    public void saveDate() {
        try {
            String sql2 = "UPDATE UserData SET last_date = ? WHERE user_id = ?";
            PreparedStatement stmt2 = this.db.prepareStatement(sql2);
            stmt2.setDate(1, Date.valueOf(this.date));
            stmt2.setInt(2, this.user_id);
            stmt2.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveMoney() {
        try {
            String sql2 = "UPDATE UserData SET money = ? WHERE user_id = ?";
            PreparedStatement stmt2 = this.db.prepareStatement(sql2);
            stmt2.setInt(1, this.money);
            stmt2.setInt(2, this.user_id);
            stmt2.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
