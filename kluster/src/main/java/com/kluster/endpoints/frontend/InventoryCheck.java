package com.kluster.endpoints.frontend;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class InventoryCheck extends APIEndpoint {
    @Override
    public String path() {
        return "/inventoryCheck";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
