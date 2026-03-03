package com.kluster.endpoints.backend.GET;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * All logs
 */
public class LogInfo extends APIEndpoint {
    private final Kluster kluster;

    public LogInfo(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/logInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
