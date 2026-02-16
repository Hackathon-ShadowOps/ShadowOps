package com.kluster.models;

public enum PersonnelRole {
    PILOT (1),
    GROUND_CREW (2),
    COMMANDER (3),
    ENGINEER (4),
    MEDIC (5),
    INTEL_ANALYST (6);

    private final int code;

    PersonnelRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
