package com.kluster.endpoints.backend.POST;

import com.google.gson.Gson;
import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import com.kluster.controller.AuthService;
import com.kluster.controller.ErrorResponse;
import com.kluster.models.PersonnelRole;

import io.javalin.http.Context;

public class AuthRefresh extends APIEndpoint {
    private final Kluster kluster;
    private final Gson gson = new Gson();

    public AuthRefresh(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/auth/refresh";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        try {
            RefreshRequest req = gson.fromJson(ctx.body(), RefreshRequest.class);
            if (req == null || req.refreshToken == null || req.refreshToken.isBlank()) {
                ctx.status(400).json(new ErrorResponse("Invalid request", ctx.path(), 400));
                return;
            }

            AuthService auth = kluster.getAuthService();
            AuthService.AuthResponse r = auth.refreshWithToken(req.refreshToken, 60, 7);
            if (r == null) {
                ctx.status(401).json(new ErrorResponse("Invalid refresh token", ctx.path(), 401));
                return;
            }

            ctx.json(r);
        } catch (Exception e) {
            ctx.status(500).json(new ErrorResponse("Internal server error", ctx.path(), 500));
        }
    }

    private static class RefreshRequest {
        public String refreshToken;
    }

    @Override
    public PersonnelRole[] allowedRoles() {
        return new PersonnelRole[0]; // default: any authenticated user allowed
    }
}
