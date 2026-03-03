package com.kluster.endpoints.backend.GET;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Info about the vehicle, such as the name, type, and other relevant information.
 */
public class VehicleInfo extends APIEndpoint {
    @Override
    public String path() {
        return "/api/v1/vehicleInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
