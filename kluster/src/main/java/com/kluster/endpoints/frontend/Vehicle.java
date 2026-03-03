package com.kluster.endpoints.frontend;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class Vehicle extends APIEndpoint {
    private Kluster kluster;

    public Vehicle(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/vehicle";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
