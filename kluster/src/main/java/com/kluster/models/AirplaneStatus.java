package com.kluster.models;

public enum AirplaneStatus {
    STANDBY (1),
    READY (2),
    ACTIVE (3),
    LANDING (4),
    TAKING_OFF (5),
    UNDER_ATTACK (6),
    DESTROYED (7),
    BROKEN_DOWN (8);

    private final int code;

    AirplaneStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
