package com.kluster.endpoints.backend.GET;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Info about items that has to be checked
 */
public class InventoryInfo extends APIEndpoint {
    @Override
    public String path() {
        return "/api/v1/inventoryInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }
    
}
