package com.kluster.endpoints.backend.POST;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Endpoint for updating and adding deliveries
 */
public class DeliveryUpdate extends APIEndpoint {
    private final Kluster kluster;

    public DeliveryUpdate(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/deliveries/update";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
