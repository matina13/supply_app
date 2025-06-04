package org.example.demo.structs;

public class Supplier {
    private int supplier_id;
    private String name;
    private String country;

    public Supplier(int supplier_id, String name, String country) {
        this.supplier_id = supplier_id;
        this.name = name;
        this.country = country;
    }

    public int getSupplier_id() {
        return this.supplier_id;
    }

    public String getName() {
        return this.name;
    }

    public String getCountry() {
        return this.country;
    }
}
