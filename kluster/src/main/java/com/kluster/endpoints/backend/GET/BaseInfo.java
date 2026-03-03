package com.kluster.endpoints.backend.GET;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Info about the base, such as the name, description, and other relevant information.
 */
public class BaseInfo extends APIEndpoint {
    @Override
    public String path() {
        return "/api/v1/baseInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
