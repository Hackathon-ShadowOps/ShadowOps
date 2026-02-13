package com.kluster.endpoints;

import com.kluster.controller.APIEndpoint;

import io.javalin.http.Context;

public class Landing extends APIEndpoint {
    @Override
    public String path() {
        return "/";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        // Här kan du implementera logiken för att hantera förfrågningar till landningssidan. För närvarande kastar den ett undantag eftersom metoden inte är implementerad.
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }
    
}
