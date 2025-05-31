package org.example.demo.structs;

public class ProducableGood {
    private int id;
    private String name;

    public ProducableGood(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
