package com.kluster.endpoints.frontend;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import com.kluster.controller.HTMLServe;
import com.kluster.models.PersonnelRole;

import io.javalin.http.Context;

public class AuthPage extends APIEndpoint {
    private final Kluster kluster;

    public AuthPage(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/auth";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        String page = HTMLServe.getPage("Auth");

        if (page == null) {
            ctx.status(500).result("Error loading auth page");
            return;
        }
        
        ctx.contentType("text/html").result(page);
    }

    @Override
    public PersonnelRole[] allowedRoles() {
        return null; // public endpoint
    }
}
