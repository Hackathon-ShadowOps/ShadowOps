package com.kluster.endpoints.backend.GET;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import io.javalin.http.Context;

/**
 * Info about the personnel, such as the name, rank, and other relevant
 * information.
 */
public class PersonnelInfo extends APIEndpoint {
    private final Kluster kluster;

    public PersonnelInfo(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/personnelInfo";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
    }

}
