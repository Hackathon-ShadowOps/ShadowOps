package com.kluster.endpoints.backend.GET;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Most important info about the base
 */
public class DashboardInfo extends APIEndpoint {
    private final Kluster kluster;

    public DashboardInfo(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/dashboardInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
