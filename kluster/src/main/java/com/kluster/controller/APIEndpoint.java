package com.kluster.controller;

import io.javalin.http.Context;

public abstract class APIEndpoint {
    public abstract String path();

    public abstract void handle(Context ctx) throws UnsupportedOperationException;
}
