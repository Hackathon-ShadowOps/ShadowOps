package com.kluster.endpoints.backend.GET;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Info about the product
 */
public class ProductInfo extends APIEndpoint {
    @Override
    public String path() {
        return "/api/v1/productInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
