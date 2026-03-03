package com.kluster.endpoints.backend.DELETE;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

public class PersonnelRemoval extends APIEndpoint {
    private final Kluster kluster;

    public PersonnelRemoval(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/personelRemoval";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
