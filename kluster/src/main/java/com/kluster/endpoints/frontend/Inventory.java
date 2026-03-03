package com.kluster.endpoints.frontend;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class Inventory extends APIEndpoint {
    private Kluster kluster;

    public Inventory(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/inventory";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
