package com.kluster.endpoints.backend.GET;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Info about the inventory check, such as the name, description, and other relevant information.
 */
public class InventoryCheckInfo extends APIEndpoint {
    @Override
    public String path() {
        return "/api/v1/inventoryCheckInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
