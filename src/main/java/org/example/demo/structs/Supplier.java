package org.example.demo.structs;

public class Supplier {
    private int supplier_id;
    private int material_id;
    private int price;
    private int quantity;

    public Supplier(int supplier_id, int material_id, int price, int quantity) {
        this.supplier_id = supplier_id;
        this.material_id = material_id;
        this.price = price;
        this.quantity = price;
    }

    public int getSupplier_id() {
        return this.supplier_id;
    }

    public int getMaterial_id() {
        return this.material_id;
    }

    public int getPrice() {
        return this.price;
    }

    public int getQuantity() {
        return this.quantity;
    }
}
