package com.kluster.endpoints.backend.DELETE;

import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class PersonelRemoval extends APIEndpoint {
    @Override
    public String path() {
        return "/api/v1/personelRemoval";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
