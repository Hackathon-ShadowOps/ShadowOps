package com.kluster.models;

public enum BaseActivityStatus {
    IDLE (1),
    ACTIVE (2),
    UNDER_ATTACK (3),
    DESTROYED (4);

    private final int code;

    BaseActivityStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
