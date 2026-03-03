package com.kluster;

import java.util.ArrayList;

import com.kluster.controller.APIEndpoint;
import com.kluster.controller.APIRunner;
import com.kluster.controller.AuthService;
import com.kluster.controller.Database;

import io.github.cdimascio.dotenv.Dotenv;

public class Kluster {
    private final APIRunner apiRunner;
    private Database database;
    private AuthService auth;
    private final Dotenv env;

    public Kluster() {
        this((String) null);
    }

    public Kluster(String envPath) {
        System.out.println("\u001B[34mStarting Kluster...\u001B[0m");
        System.out.println("\u001B[34mLoading environment from path: " + envPath + "\u001B[0m");

        if (envPath != null) {
            this.env = Dotenv.configure().directory(envPath).load();
        } else {
            try {
                this.env = Dotenv.load();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load environment variables: " + e.getMessage());
            }
        }

        ensureAPIKeys();

        this.database = new Database(this);
        this.auth = new AuthService(this.database, this);
        this.apiRunner = new APIRunner(this.auth);

        registerEndpoints();

        // Allow tests to opt-out of starting the embedded HTTP server by setting
        // the system property `SKIP_API_RUNNER=true`.
        String skip = System.getProperty("SKIP_API_RUNNER");

        if (skip == null || !skip.equalsIgnoreCase("true")) {
            this.apiRunner.start();
        } else {
            System.out.println("Skipping APIRunner.start() due to SKIP_API_RUNNER=true");
        }
    }

    public void registerEndpoints() {
        ArrayList<APIEndpoint> endpointsGet = new ArrayList<>();

        // Backend
        endpointsGet.add(new com.kluster.endpoints.backend.GET.BaseInfo(this));
        endpointsGet.add(new com.kluster.endpoints.backend.GET.DashboardInfo(this));
        endpointsGet.add(new com.kluster.endpoints.backend.GET.DeliveryInfo(this));
        endpointsGet.add(new com.kluster.endpoints.backend.GET.InventoryCheckInfo(this));
        endpointsGet.add(new com.kluster.endpoints.backend.GET.InventoryInfo(this));
        endpointsGet.add(new com.kluster.endpoints.backend.GET.LogInfo(this));
        endpointsGet.add(new com.kluster.endpoints.backend.GET.PersonnelInfo(this));
        endpointsGet.add(new com.kluster.endpoints.backend.GET.ProductInfo(this));
        endpointsGet.add(new com.kluster.endpoints.backend.GET.SpecificDeliveryInfo(this));
        endpointsGet.add(new com.kluster.endpoints.backend.GET.VehicleInfo(this));

        // Frontend
        endpointsGet.add(new com.kluster.endpoints.frontend.Base(this));
        endpointsGet.add(new com.kluster.endpoints.frontend.Dashboard(this));
        endpointsGet.add(new com.kluster.endpoints.frontend.Deliveries(this));
        endpointsGet.add(new com.kluster.endpoints.frontend.Inventory(this));
        endpointsGet.add(new com.kluster.endpoints.frontend.InventoryCheck(this));
        endpointsGet.add(new com.kluster.endpoints.frontend.Landing(this));
        endpointsGet.add(new com.kluster.endpoints.frontend.Log(this));
        endpointsGet.add(new com.kluster.endpoints.frontend.Personnel(this));
        endpointsGet.add(new com.kluster.endpoints.frontend.ProductInfo(this));
        endpointsGet.add(new com.kluster.endpoints.frontend.Vehicle(this));

        ArrayList<APIEndpoint> endpointsPost = new ArrayList<>();
        endpointsPost.add(new com.kluster.endpoints.backend.DELETE.DeliveryRemoval(this));
        endpointsPost.add(new com.kluster.endpoints.backend.DELETE.PersonnelRemoval(this));
        endpointsPost.add(new com.kluster.endpoints.backend.DELETE.ProductRemoval(this));
        endpointsPost.add(new com.kluster.endpoints.backend.DELETE.VehicleRemoval(this));

        ArrayList<APIEndpoint> endpointsDelete = new ArrayList<>();
        endpointsDelete.add(new com.kluster.endpoints.backend.POST.DeliveryUpdate(this));
        endpointsDelete.add(new com.kluster.endpoints.backend.POST.InventoryUpdate(this));
        endpointsDelete.add(new com.kluster.endpoints.backend.POST.LogUpdate(this));
        endpointsDelete.add(new com.kluster.endpoints.backend.POST.ProductUpdate(this));

        this.apiRunner.registerEndpoints(endpointsGet, endpointsPost, endpointsDelete);
    }

    public void ensureAPIKeys() {
        if (this.env == null) {
            throw new RuntimeException("Environment variables not loaded (.env missing or unreadable)");
        }

        String jwt = this.env.get("JWT_SECRET");
        if (jwt == null || jwt.isBlank()) {
            throw new RuntimeException("JWT Secret missing");
        }
    }

    public Database getDatabase() {
        return this.database;
    }

    public AuthService getAuthService() {
        return this.auth;
    }

    public Dotenv env() {
        return env;
    }
}
