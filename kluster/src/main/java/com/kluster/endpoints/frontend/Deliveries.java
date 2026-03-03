package com.kluster.endpoints.frontend;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class Deliveries extends APIEndpoint {
    @Override
    public String path() {
        return "/deliveries";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
