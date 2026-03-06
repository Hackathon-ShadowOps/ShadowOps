package com.kluster.endpoints.backend.DELETE;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;

import io.javalin.http.Context;

public class RemoveAirplaneSchedule extends APIEndpoint {
    private final Kluster kluster;

    public RemoveAirplaneSchedule(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/removeAirplaneSchedule";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        String airplaneId = ctx.queryParam("airplaneId");

        boolean removed = kluster.getDatabase().removeAirplaneSchedule(airplaneId);

        if (removed) {
            ctx.status(200).result("Airplane schedule removed successfully");
        } else {
            ctx.status(400).result("Failed to remove airplane schedule");
        }
    }

}
