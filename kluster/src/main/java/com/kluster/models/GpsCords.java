package com.kluster.models;

public class GpsCords {
    private double latitude;
    private double longitude;

    public GpsCords(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
