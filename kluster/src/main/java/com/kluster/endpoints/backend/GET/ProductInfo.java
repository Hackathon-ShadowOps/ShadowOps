package com.kluster.endpoints.backend.GET;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Info about the product
 */
public class ProductInfo extends APIEndpoint {
    private final Kluster kluster;

    public ProductInfo(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/productInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
