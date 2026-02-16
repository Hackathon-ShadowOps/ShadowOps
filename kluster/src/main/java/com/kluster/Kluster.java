package com.kluster;

import java.util.ArrayList;

import com.kluster.controller.APIEndpoint;
import com.kluster.controller.APIRunner;
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

public class Kluster {
    private final APIRunner apiRunner;
    private Database database;

    public Kluster() {
        // Initialize database and auth service, then API runner so it can enforce auth
        this.database = new Database();
        com.kluster.controller.AuthService auth = new com.kluster.controller.AuthService(this.database);
        this.apiRunner = new APIRunner(auth);

        registerEndpoints();
        this.apiRunner.start();
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

    public Database getDatabase() {
        return this.database;
    }
}
