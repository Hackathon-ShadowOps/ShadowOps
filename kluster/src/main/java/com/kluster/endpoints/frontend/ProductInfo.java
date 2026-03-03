package com.kluster.endpoints.frontend;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class ProductInfo extends APIEndpoint {
    private Kluster kluster;

    public ProductInfo(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/productInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
