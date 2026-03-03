package com.kluster.endpoints.backend.DELETE;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class DeliveryRemoval extends APIEndpoint {
    private final Kluster kluster;

    public DeliveryRemoval(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/deliveryRemoval";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
