package com.kluster.endpoints.backend.GET;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * All logs
 */
public class LogInfo extends APIEndpoint {
    @Override
    public String path() {
        return "/api/v1/logInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
