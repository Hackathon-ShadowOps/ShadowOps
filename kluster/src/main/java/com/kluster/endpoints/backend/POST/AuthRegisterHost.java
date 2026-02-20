package com.kluster.endpoints.backend.POST;

import com.google.gson.Gson;
import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import com.kluster.controller.AuthService;
import com.kluster.controller.ErrorResponse;
import com.kluster.models.PersonnelRole;

import io.javalin.http.Context;

import java.net.InetAddress;

public class AuthRegisterHost extends APIEndpoint {
    private final Kluster kluster;
    private final Gson gson = new Gson();

    public AuthRegisterHost(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/auth/register-host";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        try {
            // Only allow requests from the server host (loopback)
            String remote = ctx.ip();
            try {
                InetAddress addr = InetAddress.getByName(remote);
                if (!addr.isLoopbackAddress()) {
                    ctx.status(403).json(new ErrorResponse("Forbidden: host-only endpoint", ctx.path(), 403));
                    return;
                }
            } catch (Exception e) {
                ctx.status(403).json(new ErrorResponse("Forbidden: cannot verify caller", ctx.path(), 403));
                return;
            }

            RegisterRequest req = gson.fromJson(ctx.body(), RegisterRequest.class);
            if (req == null || req.id <= 0 || req.name == null || req.name.isEmpty() || req.rank == null
                    || req.rank.isEmpty() || req.role == null || req.password == null || req.password.isEmpty()) {
                ctx.status(400).json(new ErrorResponse("Invalid request", ctx.path(), 400));
                return;
            }

            PersonnelRole role;
            try {
                role = PersonnelRole.valueOf(req.role);
            } catch (IllegalArgumentException iae) {
                ctx.status(400).json(new ErrorResponse("Invalid role", ctx.path(), 400));
                return;
            }

            AuthService auth = kluster.getAuthService();
            try {
                auth.register(req.id, req.name, req.rank, role, req.password);
            } catch (IllegalArgumentException iae) {
                ctx.status(400).json(new ErrorResponse("Invalid input: " + iae.getMessage(), ctx.path(), 400));
                return;
            }

            ctx.status(201).json(new RegisterResponse("User created", req.id));
        } catch (Exception e) {
            ctx.status(500).json(new ErrorResponse("Internal server error", ctx.path(), 500));
        }
    }

    @Override
    public com.kluster.models.PersonnelRole[] allowedRoles() {
        // Make this endpoint public so the APIRunner doesn't require a bearer token;
        // we rely on loopback-checking to enforce host-only access.
        return null;
    }

    private static class RegisterRequest {
        public int id;
        public String name;
        public String rank;
        public String role; // must match PersonnelRole name
        public String password;
    }

    private static class RegisterResponse {
        public final String message;
        public final int id;

        public RegisterResponse(String message, int id) {
            this.message = message;
            this.id = id;
        }
    }
}
