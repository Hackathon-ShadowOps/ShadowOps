package com.kluster.models;

public enum RunwayType {
    ASPHALT (1),
    CONCRETE (2),
    GRAVEL (3),
    GRASS (4),
    WATER (5),
    HIGHWAY (6),
    OTHER (7);

    private final int code;

    RunwayType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
