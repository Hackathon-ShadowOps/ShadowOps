package com.kluster.endpoints.frontend;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class Personnel extends APIEndpoint {
    private Kluster kluster;

    public Personnel(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/personnel";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
