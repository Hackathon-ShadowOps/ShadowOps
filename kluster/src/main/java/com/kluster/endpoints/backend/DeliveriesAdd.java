package com.kluster.endpoints.backend;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import com.kluster.controller.HTMLServe;

import io.javalin.http.Context;

public class DeliveriesAdd extends APIEndpoint {
    private final Kluster kluster;

    public DeliveriesAdd(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/{baseId}/deliveries";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        String landingPage = HTMLServe.getPage("Landing");

        if (landingPage == null) {
            ctx.status(500).result("Error loading landing page");
            return;
        }

        ctx.contentType("text/json").result(landingPage);
    }

}
