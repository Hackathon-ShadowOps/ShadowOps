package com.kluster.endpoints.backend.POST;

import com.google.gson.Gson;
import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import com.kluster.controller.AuthService;
import com.kluster.controller.ErrorResponse;
import com.kluster.models.PersonnelRole;

import io.javalin.http.Context;

public class AuthLogin extends APIEndpoint {
    private final Kluster kluster;
    private final Gson gson = new Gson();

    public AuthLogin(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/auth/login";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        try {
            AuthRequest req = gson.fromJson(ctx.body(), AuthRequest.class);
            if (req == null || req.id <= 0 || req.password == null) {
                ctx.status(400).json(new ErrorResponse("Invalid request", ctx.path(), 400));
                return;
            }

            AuthService auth = kluster.getAuthService();
            AuthService.AuthResponse r = auth.authenticateWithRefresh(req.id, req.password, 60, 7);
            if (r == null) {
                ctx.status(401).json(new ErrorResponse("Invalid credentials", ctx.path(), 401));
                return;
            }

            ctx.json(r);
        } catch (Exception e) {
            ctx.status(500).json(new ErrorResponse("Internal server error", ctx.path(), 500));
        }
    }

    private static class AuthRequest {
        public int id;
        public String password;
    }

    @Override
    public PersonnelRole[] allowedRoles() {
        return null; // Public
    }
}
