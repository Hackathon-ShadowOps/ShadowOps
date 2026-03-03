package com.kluster.endpoints.backend.POST;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Endpoint for updating and adding products
 */
public class ProductUpdate extends APIEndpoint {
    @Override
    public String path() {
        return "/api/v1/products/update";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
