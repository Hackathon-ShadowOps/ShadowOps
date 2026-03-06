package com.kluster.models;

public class AirplaneSchedules {
    private String airplaneId;
    private int groundSpace;
    private long landingTimeStart;
    private long landingTimeEnd;

    public AirplaneSchedules(String airplaneId, int groundSpace, long landingTimeStart, long landingTimeEnd) {
        this.airplaneId = airplaneId;
        this.landingTimeStart = landingTimeStart;
        this.landingTimeEnd = landingTimeEnd;
    }

    public String getAirplaneId() {
        return airplaneId;
    }

    public int getGroundSpace() {
        return groundSpace;
    }

    public long getLandingTimeStart() {
        return landingTimeStart;
    }

    public long getLandingTimeEnd() {
        return landingTimeEnd;
    }

    public void setGroundSpace(int groundSpace) {
        this.groundSpace = groundSpace;
    }

    public void setLandingTimeStart(long landingTimeStart) {
        this.landingTimeStart = landingTimeStart;
    }

    public void setLandingTimeEnd(long landingTimeEnd) {
        this.landingTimeEnd = landingTimeEnd;
    }
}
