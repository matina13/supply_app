package org.example.demo.structs;

public class AlgoStruct {
    private int material_id;
    private int supplier_id;

    public AlgoStruct(int material_id, int supplier_id) {
        this.material_id = material_id;
        this.supplier_id = material_id;
    }

    public int getMaterial_id() {
        return this.material_id;
    }

    public int getSupplier_id() {
        return this.supplier_id;
    }
}
