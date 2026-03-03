package com.kluster.endpoints.frontend;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class Inventory extends APIEndpoint {
    @Override
    public String path() {
        return "/inventory";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
