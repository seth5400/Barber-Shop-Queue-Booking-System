package com.example.demo.model;

import jakarta.persistence.*;

@Entity
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isBookingEnabled;
    private String openTime;
    private String closeTime;

    // Getters และ Setters
    public boolean isBookingEnabled() {
        return isBookingEnabled;
    }

    public void setBookingEnabled(boolean isBookingEnabled) {
        this.isBookingEnabled = isBookingEnabled;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }
}