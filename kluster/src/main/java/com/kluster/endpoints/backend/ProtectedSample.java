package com.kluster.endpoints.backend;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import com.kluster.controller.ErrorResponse;
import com.kluster.models.Personnel;

import io.javalin.http.Context;

public class ProtectedSample extends APIEndpoint {
    private final Kluster kluster;

    public ProtectedSample(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/protected/sample";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        try {
            String auth = ctx.header("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                ctx.status(401).json(new ErrorResponse("Unauthorized", ctx.path(), 401));
                return;
            }
            String token = auth.substring(7).trim();
            Personnel p = kluster.getAuthService().validateToken(token);
            if (p == null) {
                ctx.status(401).json(new ErrorResponse("Unauthorized", ctx.path(), 401));
                return;
            }

            ctx.json(new java.util.HashMap<String, Object>() {{
                put("message", "This is protected data");
                put("user", new java.util.HashMap<String, Object>() {{
                    put("id", p.getId());
                    put("name", p.getName());
                    put("role", p.getRole() != null ? p.getRole().name() : "");
                }});
            }});
        } catch (Exception e) {
            ctx.status(500).json(new ErrorResponse("Internal server error", ctx.path(), 500));
        }
    }
}
