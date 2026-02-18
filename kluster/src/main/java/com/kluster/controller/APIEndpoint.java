package com.kluster.controller;

import com.kluster.models.PersonnelRole;

import io.javalin.http.Context;

public abstract class APIEndpoint {
    public abstract String path();

    public abstract void handle(Context ctx) throws UnsupportedOperationException;

    /**
     * Define which roles are allowed to access this endpoint.
     * - return null => public endpoint, no auth required
     * - return empty => any authenticated user allowed
     * - return array with roles => only those roles allowed
     */
    public PersonnelRole[] allowedRoles() {
        return new PersonnelRole[0]; // default: any authenticated user allowed
    }
}
