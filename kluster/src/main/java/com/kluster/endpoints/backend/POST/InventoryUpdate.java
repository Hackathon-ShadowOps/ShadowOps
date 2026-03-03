package com.kluster.endpoints.backend.POST;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Endpoint for updating and adding inventory items
 */
public class InventoryUpdate extends APIEndpoint {
    @Override
    public String path() {
        return "/api/v1/inventory/update";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }
    
}
