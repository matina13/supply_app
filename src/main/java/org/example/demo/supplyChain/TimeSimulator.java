package org.example.demo.supplyChain;

import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import java.time.LocalDate;

public class TimeSimulator {
    private LocalDate date;
    private Timer t = new Timer();
    public TimeSimulator() {
        
    }
    
    public void init() { //get date of user from db
        this.date = LocalDate.of(2025,5,1);
    }

    public void incrementDate() {
        this.date = this.date.plusDays(1);
    }
    
    public void stop() {
        this.t.cancel();
    }
    
    public String getDate() {
        return this.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
