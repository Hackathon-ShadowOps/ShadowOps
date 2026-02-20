package com.kluster.endpoints.backend.POST;

import com.google.gson.Gson;
import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import com.kluster.controller.ErrorResponse;
import com.kluster.models.PersonnelRole;

import io.javalin.http.Context;

public class AuthLogout extends APIEndpoint {
    private final Kluster kluster;
    private final Gson gson = new Gson();

    public AuthLogout(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/auth/logout";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        try {
            LogoutRequest req = gson.fromJson(ctx.body(), LogoutRequest.class);
            if (req == null || req.userId == null || req.userId.isBlank()) {
                ctx.status(400).json(new ErrorResponse("Invalid request", ctx.path(), 400));
                return;
            }

            kluster.getAuthService().revokeRefreshTokens(req.userId);
            ctx.status(200).json(new java.util.HashMap<String, String>() {
                {
                    put("status", "ok");
                }
            });
        } catch (Exception e) {
            ctx.status(500).json(new ErrorResponse("Internal server error", ctx.path(), 500));
        }
    }

    private static class LogoutRequest {
        public String userId;
    }

    @Override
    public PersonnelRole[] allowedRoles() {
        return new PersonnelRole[0]; // default: any authenticated user allowed
    }
}
