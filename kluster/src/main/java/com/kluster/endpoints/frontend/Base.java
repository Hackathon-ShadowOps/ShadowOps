package com.kluster.endpoints.frontend;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class Base extends APIEndpoint {
    @Override
    public String path() {
        return "/base";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
