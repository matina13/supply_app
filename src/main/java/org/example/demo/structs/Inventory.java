package org.example.demo.structs;

public class Inventory {
    private int id;
    private int quantity;
    private String name;

    public Inventory(int id, int quantity, String name) {
        this.id = id;
        this.quantity = quantity;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public String getName() {
        return this.name;
    }
}
