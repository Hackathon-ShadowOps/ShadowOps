package com.kluster.endpoints.backend.POST;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Endpoint for adding logs
 */
public class LogUpdate extends APIEndpoint {
    @Override
    public String path() {
        return "/api/v1/logs/update";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }
    
}
