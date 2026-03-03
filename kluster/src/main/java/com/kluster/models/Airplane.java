package com.kluster.models;

public class Airplane {
    private int id;
    private String name;
    private String fuelType;
    private AirplaneStatus status; // e.g., "en route", "landing", "taking off"
    private String mission;
    private GpsCords gpsCords;

    public Airplane(int id, String name, String fuelType) {
        this.id = id;
        this.name = name;
        this.fuelType = fuelType;
        this.status = AirplaneStatus.STANDBY;
        this.mission = "None";
        this.gpsCords = new GpsCords(0.0, 0.0);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFuelType() {
        return fuelType;
    }

    public AirplaneStatus getStatus() {
        return status;
    }

    public String getMission() {
        return mission;
    }

    public GpsCords getGpsCords() {
        return gpsCords;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public void setStatus(AirplaneStatus status) {
        this.status = status;
    }

    public void setMission(String mission) {
        this.mission = mission;
    }

    public void setGpsCords(GpsCords gpsCords) {
        this.gpsCords = gpsCords;
    }
}
