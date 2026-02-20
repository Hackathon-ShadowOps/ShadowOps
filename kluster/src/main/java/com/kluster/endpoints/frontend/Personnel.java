package com.kluster.endpoints.frontend;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import com.kluster.controller.HTMLServe;

import io.javalin.http.Context;

public class Personnel extends APIEndpoint {
    private final Kluster kluster;

    public Personnel(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/personnel";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        String baseId = ctx.queryParam("baseId");
        if (baseId == null || baseId.isEmpty()) {
            ctx.status(400).result("Missing required query parameter: baseId");
            return;
        }
        String landingPage = HTMLServe.getPage("Landing");

        if (landingPage == null) {
            ctx.status(500).result("Error loading landing page");
            return;
        }

        ctx.contentType("text/html").result(landingPage);
    }

}
