package org.example.demo.supplyChain.Transaction;

public class TravelTimeCalculator {
    public static int calculate(int producer_x, int producer_y, int supplier_x, int supplier_y) {
        int x = supplier_x - producer_x;
        int y = supplier_y - producer_y;
        int tilesToTravel = Math.abs(x) + Math.abs(y);

        if (tilesToTravel == 0) return 2; //if in the same tile, delivery day is 2 days
        int daysToDeliver = tilesToTravel * 5; //if in different tile, each tile to travel is +5 days
        return daysToDeliver;
    }
}
