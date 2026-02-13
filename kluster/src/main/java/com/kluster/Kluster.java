package com.kluster;

import java.util.ArrayList;

import com.kluster.controller.APIEndpoint;
import com.kluster.controller.APIRunner;
import com.kluster.controller.Database;


public class Kluster {
    private final APIRunner apiRunner;
    private Database database;

    public Kluster() {
        this.apiRunner = new APIRunner();
        this.database = new Database();

        registerEndpoints();
        this.apiRunner.start();
    }

    public void registerEndpoints() {
        ArrayList<APIEndpoint> endpointsGet = new ArrayList<>();

        ArrayList<APIEndpoint> endpointsPost = new ArrayList<>();

        ArrayList<APIEndpoint> endpointsDelete = new ArrayList<>();

        this.apiRunner.registerEndpoints(endpointsGet, endpointsPost, endpointsDelete);
    }

    public Database getDatabase() {
        return this.database;
    }
}
