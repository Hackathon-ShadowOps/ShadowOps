package com.kluster.endpoints.backend.DELETE;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import com.kluster.controller.HTMLServe;

import io.javalin.http.Context;

public class IncidentReportRemove extends APIEndpoint {
    private final Kluster kluster;

    public IncidentReportRemove(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/report";
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

        ctx.contentType("text/json").result(landingPage);
    }

}
