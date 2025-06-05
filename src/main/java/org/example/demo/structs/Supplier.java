package org.example.demo.structs;

public class Supplier {
    private int supplier_id;
    private String name;
    private String country;
    private int location_x;
    private int location_y;

    public Supplier(int supplier_id, String name, String country, int location_x, int location_y) {
        this.supplier_id = supplier_id;
        this.name = name;
        this.country = country;
        this.location_x = location_x;
        this.location_y = location_y;
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

    public int getLocation_x() {
        return this.location_x;
    }

    public int getLocation_y() {
        return this.location_y;
    }
}
