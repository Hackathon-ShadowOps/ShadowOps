package com.kluster.endpoints.frontend;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class Dashboard extends APIEndpoint {
    @Override
    public String path() {
        return "/dashboard";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
