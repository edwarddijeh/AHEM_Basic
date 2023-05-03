package com.example.ahem_basic;

import java.util.List;

public class PolygonDetails {
    private double northernLatitude;
    private double southernLatitude;
    private double westernLongitude;
    private double easternLongitude;
    private List<Measurements> measurements;

    public double getEasternLongitude() {
        return easternLongitude;
    }

    public void setEasternLongitude(double easternLongitude) {
        this.easternLongitude = easternLongitude;
    }

    public double getNorthernLatitude() {
        return northernLatitude;
    }

    public void setNorthernLatitude(double northernLatitude) {
        this.northernLatitude = northernLatitude;
    }

    public double getSouthernLatitude() {
        return southernLatitude;
    }

    public void setSouthernLatitude(double southernLatitude) {
        this.southernLatitude = southernLatitude;
    }

    public double getWesternLongitude() {
        return westernLongitude;
    }

    public void setWesternLongitude(double westernLongitude) {
        this.westernLongitude = westernLongitude;
    }

    public List<Measurements> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurements> measurements) {
        this.measurements = measurements;
    }
}
