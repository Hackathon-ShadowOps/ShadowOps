package com.kluster.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.javalin.Javalin;
import io.javalin.http.util.NaiveRateLimit;

public class APIRunner {
    private Javalin app;
    private ArrayList<APIEndpoint> getEndpoints = new ArrayList<>();
    private ArrayList<APIEndpoint> postEndpoints = new ArrayList<>();
    private ArrayList<APIEndpoint> deleteEndpoints = new ArrayList<>();
    private List<String> allowedOrigins = new ArrayList<>();
    private boolean allowAnyOrigin = false;

    public APIRunner() {
        app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
        });

        // Comma-separated list, e.g. "https://example.com,https://api.example.com".
        String env = System.getenv("CORS_ALLOWED_ORIGINS");
        if (env == null || env.trim().isEmpty()) {
            allowAnyOrigin = true;
            System.out.println("\u001B[31mWARNING: CORS_ALLOWED_ORIGINS not set. Allowing any origin (development default). Set CORS_ALLOWED_ORIGINS to restrict origins.\u001B[0m");
        } else {
            allowedOrigins = Arrays.asList(env.split("\\s*,\\s*"));
            for (String o : allowedOrigins) {
                if ("*".equals(o)) {
                    allowAnyOrigin = true;
                    break;
                }
            }
        }
    }

    private boolean isOriginAllowed(String origin) {
        if (allowAnyOrigin) return true;
        if (origin == null) return false;
        for (String allowed : allowedOrigins) {
            if (allowed == null) continue;
            if (allowed.equalsIgnoreCase(origin) || allowed.equals("*")) return true;
        }
        return false;
    }

    public void registerEndpoints(ArrayList<APIEndpoint> getEndpoints, ArrayList<APIEndpoint> postEndpoints,
            ArrayList<APIEndpoint> deleteEndpoints) {
        this.getEndpoints.addAll(getEndpoints);
        this.postEndpoints.addAll(postEndpoints);
        this.deleteEndpoints.addAll(deleteEndpoints);
    }

    public APIRunner start() {
        app.before(ctx -> {
            String origin = ctx.header("Origin");
            if (isOriginAllowed(origin)) {
                if (allowAnyOrigin) {
                    ctx.header("Access-Control-Allow-Origin", "*");
                } else {
                    ctx.header("Access-Control-Allow-Origin", origin);
                    ctx.header("Access-Control-Allow-Credentials", "true");
                }
            }

            ctx.header("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        app.options("/*", ctx -> {
            String origin = ctx.header("Origin");
            if (isOriginAllowed(origin)) {
                if (allowAnyOrigin) {
                    ctx.header("Access-Control-Allow-Origin", "*");
                } else {
                    ctx.header("Access-Control-Allow-Origin", origin);
                    ctx.header("Access-Control-Allow-Credentials", "true");
                }
            }

            ctx.header("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        for (APIEndpoint endPoint : getEndpoints) {
            app.get(endPoint.path(), ctx -> {
                try {
                    NaiveRateLimit.requestPerTimeUnit(ctx, 10, TimeUnit.SECONDS);

                    endPoint.handle(ctx);
                } catch (UnsupportedOperationException e) {
                    ctx.status(501).result("Not Implemented: " + endPoint.getClass().getSimpleName());
                }
            });
        }

        getEndpoints.clear();

        for (APIEndpoint endPoint : postEndpoints) {
            app.post(endPoint.path(), ctx -> {
                try {
                    NaiveRateLimit.requestPerTimeUnit(ctx, 1, TimeUnit.SECONDS);

                    endPoint.handle(ctx);
                } catch (UnsupportedOperationException e) {
                    ctx.status(501).result("Not Implemented: " + endPoint.getClass().getSimpleName());
                }
            });
        }

        postEndpoints.clear();

        for (APIEndpoint endPoint : deleteEndpoints) {
            app.delete(endPoint.path(), ctx -> {
                try {
                    NaiveRateLimit.requestPerTimeUnit(ctx, 1, TimeUnit.SECONDS);

                    endPoint.handle(ctx);
                } catch (UnsupportedOperationException e) {
                    ctx.status(501).result("Not Implemented: " + endPoint.getClass().getSimpleName());
                }
            });
        }

        deleteEndpoints.clear();

        // Handle 404 - endpoint not found
        app.error(404, ctx -> {
            if (ctx.result() != null && ctx.result().contains("/.well-known/appspecific/com.chrome.devtools.json")) {
                return;
            }

            ctx.status(404).json(new ErrorResponse("Endpoint not found", ctx.path(), 404));
            System.out.println("404 Not Found: " + ctx.path());
        });

        // Handle other errors
        app.error(500, ctx -> {
            ctx.status(500).json(new ErrorResponse("Internal server error", ctx.path(), 500));
            System.out.println("500 Internal Server Error: " + ctx.path());
        });

        app.start(5000);
        return this;
    }

    public APIRunner stop() {
        app.stop();
        return this;
    }
}
