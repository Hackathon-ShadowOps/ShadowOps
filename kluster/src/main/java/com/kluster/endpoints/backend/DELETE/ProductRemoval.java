package com.kluster.endpoints.backend.DELETE;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class ProductRemoval extends APIEndpoint {
    private final Kluster kluster;

    public ProductRemoval(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/productRemoval";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
