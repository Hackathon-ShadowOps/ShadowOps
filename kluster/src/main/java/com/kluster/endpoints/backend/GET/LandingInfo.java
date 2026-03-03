package com.kluster.endpoints.backend.GET;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Redirect page
 */
public class LandingInfo extends APIEndpoint {
    @Override
    public String path() {
        return "/api/v1/landingInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }
    
}
