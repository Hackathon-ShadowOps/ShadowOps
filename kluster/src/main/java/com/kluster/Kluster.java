package com.kluster;

import java.util.ArrayList;

import com.kluster.controller.APIEndpoint;
import com.kluster.controller.APIRunner;
import com.kluster.controller.AuthService;
import com.kluster.controller.Database;
import com.kluster.endpoints.backend.*;
import com.kluster.endpoints.backend.DELETE.DeliveriesRemove;
import com.kluster.endpoints.backend.DELETE.IncidentReportRemove;
import com.kluster.endpoints.backend.DELETE.PersonnelRemove;
import com.kluster.endpoints.backend.GET.BaseGet;
import com.kluster.endpoints.backend.GET.DeliveriesGet;
import com.kluster.endpoints.backend.GET.IncidentReportGet;
import com.kluster.endpoints.backend.GET.LogGet;
import com.kluster.endpoints.backend.GET.PersonnelGet;
import com.kluster.endpoints.backend.POST.AuthLogin;
import com.kluster.endpoints.backend.POST.AuthRegisterHost;
import com.kluster.endpoints.backend.POST.AuthLogout;
import com.kluster.endpoints.backend.POST.AuthRefresh;
import com.kluster.endpoints.backend.POST.BaseAdd;
import com.kluster.endpoints.backend.POST.DeliveriesAdd;
import com.kluster.endpoints.backend.POST.IncidentReportAdd;
import com.kluster.endpoints.backend.POST.PersonnelAdd;
import com.kluster.endpoints.backend.POST.PersonnelAssign;
import com.kluster.endpoints.backend.POST.PersonnelDeassign;
import com.kluster.endpoints.frontend.*;

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
        endpointsGet.add(new BaseGet(this));
        endpointsGet.add(new AuthPage(this));
        endpointsGet.add(new Landing(this));
        endpointsGet.add(new ProtectedSample(this));
        
        // {{baseId}} endpoints
        endpointsGet.add(new PersonnelGet(this));
        endpointsGet.add(new DeliveriesGet(this));
        endpointsGet.add(new IncidentReportGet(this));
        endpointsGet.add(new LogGet(this));
        endpointsGet.add(new BaseLanding(this));
        endpointsGet.add(new Deliveries(this));
        endpointsGet.add(new IncidentReport(this));
        endpointsGet.add(new Log(this));
        endpointsGet.add(new Personnel(this));


        ArrayList<APIEndpoint> endpointsPost = new ArrayList<>();
        endpointsPost.add(new AuthRegisterHost(this));
        endpointsPost.add(new AuthLogin(this));
        endpointsPost.add(new AuthRefresh(this));
        endpointsPost.add(new AuthLogout(this));
        endpointsPost.add(new BaseAdd(this));

        // {{baseId}} endpoints
        endpointsPost.add(new DeliveriesAdd(this));
        endpointsPost.add(new PersonnelDeassign(this));
        endpointsPost.add(new PersonnelAssign(this));
        endpointsPost.add(new PersonnelAdd(this));
        endpointsPost.add(new IncidentReportAdd(this));

        ArrayList<APIEndpoint> endpointsDelete = new ArrayList<>();
        // {{baseId}} endpoints
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

    public AuthService getAuthService() {
        return this.auth;
    }

    public Dotenv env() {
        return env;
    }
}
