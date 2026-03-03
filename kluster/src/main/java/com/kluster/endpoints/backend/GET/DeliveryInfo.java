package com.kluster.endpoints.backend.GET;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Info about the delivery, such as the name, description, and other relevant
 * information.
 */
public class DeliveryInfo extends APIEndpoint {
    private final Kluster kluster;

    public DeliveryInfo(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/deliveryInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
