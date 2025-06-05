package org.example.demo.structs;

public class SupplierMaterialInfo {
    private int supplier_id;
    private int material_id;
    private String material_name;
    private int price;
    private int quantity;

    public SupplierMaterialInfo(int supplier_id, int material_id, String material_name, int price, int quantity) {
        this.supplier_id = supplier_id;
        this.material_id = material_id;
        this.material_name = material_name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getSupplier_id() {
        return this.supplier_id;
    }

    public int getMaterial_id() {
        return this.material_id;
    }

    private String getMaterial_name() {
        return this.material_name;
    }

    public int getPrice() {
        return this.price;
    }

    public int getQuantity() {
        return this.quantity;
    }
}
