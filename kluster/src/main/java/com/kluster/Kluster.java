package com.kluster;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.kluster.controller.APIEndpoint;
import com.kluster.controller.APIRunner;
import com.kluster.controller.AuthService;
import com.kluster.controller.Database;
import com.kluster.endpoints.backend.BaseAdd;
import com.kluster.endpoints.backend.BaseGet;
import com.kluster.endpoints.backend.DeliveriesAdd;
import com.kluster.endpoints.backend.DeliveriesGet;
import com.kluster.endpoints.backend.DeliveriesRemove;
import com.kluster.endpoints.backend.IncidentReportAdd;
import com.kluster.endpoints.backend.IncidentReportGet;
import com.kluster.endpoints.backend.IncidentReportRemove;
import com.kluster.endpoints.backend.LogGet;
import com.kluster.endpoints.backend.PersonnelAdd;
import com.kluster.endpoints.backend.PersonnelAssign;
import com.kluster.endpoints.backend.PersonnelDeassign;
import com.kluster.endpoints.backend.PersonnelGet;
import com.kluster.endpoints.backend.PersonnelRemove;
import com.kluster.endpoints.frontend.BaseLanding;
import com.kluster.endpoints.frontend.Deliveries;
import com.kluster.endpoints.frontend.IncidentReport;
import com.kluster.endpoints.frontend.Landing;
import com.kluster.endpoints.frontend.Log;
import com.kluster.endpoints.frontend.Personnel;

import io.github.cdimascio.dotenv.Dotenv;

public class Kluster {
    private final APIRunner apiRunner;
    private Database database;
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
        AuthService auth = new AuthService(this.database, this);
        this.apiRunner = new APIRunner(auth);

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
        endpointsGet.add(new BaseLanding(this));
        endpointsGet.add(new Deliveries(this));
        endpointsGet.add(new IncidentReport(this));
        endpointsGet.add(new Landing(this));
        endpointsGet.add(new Log(this));
        endpointsGet.add(new Personnel(this));

        endpointsGet.add(new BaseGet(this));
        endpointsGet.add(new DeliveriesGet(this));
        endpointsGet.add(new IncidentReportGet(this));
        endpointsGet.add(new LogGet(this));
        endpointsGet.add(new PersonnelGet(this));

        ArrayList<APIEndpoint> endpointsPost = new ArrayList<>();
        endpointsPost.add(new BaseAdd(this));
        endpointsPost.add(new DeliveriesAdd(this));
        endpointsPost.add(new IncidentReportAdd(this));
        endpointsPost.add(new PersonnelAdd(this));
        endpointsPost.add(new PersonnelAssign(this));
        endpointsPost.add(new PersonnelDeassign(this));

        ArrayList<APIEndpoint> endpointsDelete = new ArrayList<>();
        endpointsDelete.add(new DeliveriesRemove(this));
        endpointsDelete.add(new IncidentReportRemove(this));
        endpointsDelete.add(new PersonnelRemove(this));

        this.apiRunner.registerEndpoints(endpointsGet, endpointsPost, endpointsDelete);
    }

    public void ensureAPIKeys() {
        if (this.env == null)
            throw new RuntimeException("Environment variables not loaded (.env missing or unreadable)");

        String jwt = this.env.get("JWT_SECRET");
        if (jwt == null || jwt.isBlank())
            throw new RuntimeException("JWT Secret missing");
    }

    public Database getDatabase() {
        return this.database;
    }

    public Dotenv env() {
        return env;
    }
}
