package org.example.demo.structs;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.ArrayList;

public class Json<T> {
    private String date;
    private int money;
    private ArrayList<T> data;
    public Json(String date, int money, T data) {
        this.date = date;
        this.money = money;
        this.data = new ArrayList<T>();
        this.data.add(data);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public void addData(T data) {
        this.data.add(data);
    }


}
