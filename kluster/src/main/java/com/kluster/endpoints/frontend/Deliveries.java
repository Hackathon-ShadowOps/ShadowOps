package com.kluster.endpoints.frontend;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class Deliveries extends APIEndpoint {
    private Kluster kluster;

    public Deliveries(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/deliveries";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
