package org.example.demo.structs;

import java.sql.Date;

public class Transaction {
    private int transaction_id;
    private String type;
    private int order_id;
    private int supplier_id;
    private int buyer_id;
    private Date date_finished;

    public Transaction(int transaction_id, String type, int order_id, int supplier_id, int buyer_id, Date date_finished) {
        this.transaction_id = transaction_id;
        this.type = type;
        this.order_id = order_id;
        this.supplier_id = supplier_id;
        this.buyer_id = buyer_id;
        this.date_finished = date_finished;
    }

    public int getTransaction_id() {
        return this.transaction_id;
    }

    public String getType() {
        return this.type;
    }

    public int getOrder_id() {
        return this.order_id;
    }

    public int getSupplier_id() {
        return this.supplier_id;
    }

    public int getBuyer_id() {
        return this.buyer_id;
    }

    public Date getDate_finished() {
        return this.date_finished;
    }
}
