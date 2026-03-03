package com.kluster.endpoints.backend.GET;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Info about the specific delivery, such as the name, description, and other
 * relevant information.
 */
public class SpecificDeliveryInfo extends APIEndpoint {
    private final Kluster kluster;

    public SpecificDeliveryInfo(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/specificDeliveryInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
