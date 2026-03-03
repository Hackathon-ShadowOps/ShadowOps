package com.kluster.endpoints.backend.DELETE;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class VehicleRemoval extends APIEndpoint {
    @Override
    public String path() {
        return "/api/v1/vehicleRemoval";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
