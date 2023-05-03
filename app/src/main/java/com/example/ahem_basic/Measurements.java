package com.example.ahem_basic;

public class Measurements {
    private String pollutantId;//PM25, PM10, O3, SO2, CO, NO2
    private int value;
    private String timestamp;

    public void setPollutantId(String pollutantId) {
        this.pollutantId = pollutantId;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getPollutantId() {
        return pollutantId;
    }

    public int getValue() {
        return value;
    }
}
