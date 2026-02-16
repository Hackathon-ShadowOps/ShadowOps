package com.kluster.models;

public enum Permission {
    VIEW_BASES (1),
    MANAGE_BASES (2),
    VIEW_PERSONNEL (3),
    MANAGE_PERSONNEL (4),
    VIEW_AIRPLANES (5),
    MANAGE_AIRPLANES (6),
    VIEW_INCIDENTS (7),
    MANAGE_INCIDENTS (8);

    private final int code;

    Permission(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
