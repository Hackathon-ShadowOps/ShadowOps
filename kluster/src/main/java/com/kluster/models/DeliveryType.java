package com.kluster.models;

public enum DeliveryType {
    GROUND_TRANSPORT (1),
    SEA_TRANSPORT (2),
    DRONE_DELIVERY (3),
    TRAIN_DELIVERY (4),
    AIR_DELIVERY (5),
    OTHER (6);

    private final int code;

    DeliveryType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
