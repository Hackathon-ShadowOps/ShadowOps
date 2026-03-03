package com.kluster.endpoints.frontend;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class Base extends APIEndpoint {
    private Kluster kluster;

    public Base(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/base";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
