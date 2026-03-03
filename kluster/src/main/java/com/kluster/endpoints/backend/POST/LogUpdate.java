package com.kluster.endpoints.backend.POST;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Endpoint for adding logs
 */
public class LogUpdate extends APIEndpoint {
    private final Kluster kluster;

    public LogUpdate(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/logs/update";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
