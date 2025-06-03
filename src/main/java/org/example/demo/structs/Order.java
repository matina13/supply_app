package org.example.demo.structs;

public class Order {
    private int order_id;
    private int material_id;
    private int quantity;

    public Order(int order_id, int material_id, int quantity) {
        this.order_id = order_id;
        this.material_id = material_id;
        this.quantity = quantity;
    }

    public int getOrder_id() {
        return this.order_id;
    }

    public int getMaterial_id() {
        return this.material_id;
    }

    public int getQuantity() {
        return this.quantity;
    }
}
