package com.kluster.endpoints.backend.POST;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Endpoint for updating and adding products
 */
public class ProductUpdate extends APIEndpoint {
    private final Kluster kluster;

    public ProductUpdate(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/products/update";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
