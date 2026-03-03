package com.kluster.endpoints.frontend;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class ProductInfo extends APIEndpoint {
    @Override
    public String path() {
        return "/productInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
