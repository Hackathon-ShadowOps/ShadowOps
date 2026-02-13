package com.kluster.controller;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.javalin.Javalin;
import io.javalin.http.util.NaiveRateLimit;

public class APIRunner {
    private Javalin app;
    private ArrayList<APIEndpoint> getEndpoints = new ArrayList<>();
    private ArrayList<APIEndpoint> postEndpoints = new ArrayList<>();
    private ArrayList<APIEndpoint> deleteEndpoints = new ArrayList<>();

    public APIRunner() {
        app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
        });
    }

    public void registerEndpoints(ArrayList<APIEndpoint> getEndpoints, ArrayList<APIEndpoint> postEndpoints,
            ArrayList<APIEndpoint> deleteEndpoints) {
        this.getEndpoints.addAll(getEndpoints);
        this.postEndpoints.addAll(postEndpoints);
        this.deleteEndpoints.addAll(deleteEndpoints);
    }

    public APIRunner start() {
        // Add CORS headers FIRST before registering routes
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        // Handle preflight requests
        app.options("/*", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
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
