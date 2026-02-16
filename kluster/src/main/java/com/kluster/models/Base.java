package com.kluster.models;

public class Base {
    private GpsCords gpsCords;
    private String name;
    private int id;
    private BaseActivityStatus activityStatus;
    private IncidentReport currentIncident;
    private Airplane[] stationedAirplanes;
    private Personnel[] stationedPersonnel;
    private int airportCapacity;
    private int airportRunwayLength;
    private RunwayType runwayType;
    private boolean airportIsOperational;
    private DeliveryType[] possibleDeliveryTypes;

    public Base(GpsCords gpsCords) {
        this.gpsCords = gpsCords;
    }

    public GpsCords getGpsCords() {
        return gpsCords;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public BaseActivityStatus getActivityStatus() {
        return activityStatus;
    }

    public IncidentReport getCurrentIncident() {
        return currentIncident;
    }

    public Airplane[] getStationedAirplanes() {
        return stationedAirplanes;
    }

    public Personnel[] getStationedPersonnel() {
        return stationedPersonnel;
    }

    public int getAirportCapacity() {
        return airportCapacity;
    }

    public int getAirportRunwayLength() {
        return airportRunwayLength;
    }

    public RunwayType getRunwayType() {
        return runwayType;
    }

    public boolean isAirportIsOperational() {
        return airportIsOperational;
    }

    public DeliveryType[] getPossibleDeliveryTypes() {
        return possibleDeliveryTypes;
    }

    public void setGpsCords(GpsCords gpsCords) {
        this.gpsCords = gpsCords;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setActivityStatus(BaseActivityStatus activityStatus) {
        this.activityStatus = activityStatus;
    }

    public void setCurrentIncident(IncidentReport currentIncident) {
        this.currentIncident = currentIncident;
    }

    public void setStationedAirplanes(Airplane[] stationedAirplanes) {
        this.stationedAirplanes = stationedAirplanes;
    }

    public void setStationedPersonnel(Personnel[] stationedPersonnel) {
        this.stationedPersonnel = stationedPersonnel;
    }

    public void setAirportCapacity(int airportCapacity) {
        this.airportCapacity = airportCapacity;
    }

    public void setAirportRunwayLength(int airportRunwayLength) {
        this.airportRunwayLength = airportRunwayLength;
    }

    public void setRunwayType(RunwayType runwayType) {
        this.runwayType = runwayType;
    }

    public void setAirportIsOperational(boolean airportIsOperational) {
        this.airportIsOperational = airportIsOperational;
    }

    public void setPossibleDeliveryTypes(DeliveryType[] possibleDeliveryTypes) {
        this.possibleDeliveryTypes = possibleDeliveryTypes;
    }
}
