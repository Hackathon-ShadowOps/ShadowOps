package com.kluster.endpoints.frontend;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class Vehicle extends APIEndpoint {
    @Override
    public String path() {
        return "/vehicle";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
