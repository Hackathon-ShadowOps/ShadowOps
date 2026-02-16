package com.kluster.controller;

import io.javalin.http.Context;

public abstract class APIEndpoint {
    public abstract String path();

    public abstract void handle(Context ctx) throws UnsupportedOperationException;

    /**
     * Define which roles are allowed to access this endpoint.
     * - return null  => public endpoint, no auth required
     * - return empty => any authenticated user allowed
     * - return array with roles => only those roles allowed
     */
    public com.kluster.models.PersonnelRole[] allowedRoles() {
        return new com.kluster.models.PersonnelRole[0];
    }
}
