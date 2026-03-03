package com.kluster.endpoints.frontend;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class Dashboard extends APIEndpoint {
    private Kluster kluster;

    public Dashboard(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/dashboard";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
