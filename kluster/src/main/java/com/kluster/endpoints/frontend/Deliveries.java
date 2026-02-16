package com.kluster.endpoints.frontend;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import com.kluster.controller.HTMLServe;

import io.javalin.http.Context;

public class Deliveries extends APIEndpoint {
    private final Kluster kluster;

    public Deliveries(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/{baseId}/deliveries";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        String landingPage = HTMLServe.getPage("Landing");

        if (landingPage == null) {
            ctx.status(500).result("Error loading landing page");
            return;
        }

        ctx.contentType("text/html").result(landingPage);
    }

}
