package com.kluster.endpoints.backend.POST;

import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;

import io.javalin.http.Context;

public class UpdateAirplaneSchedule extends APIEndpoint {
    private final Kluster kluster;

    public UpdateAirplaneSchedule(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/updateAirplaneSchedule";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        String airplaneId = ctx.queryParam("airplaneId");

        int groundSpace = ctx.queryParam("groundSpace") != null ? Integer.parseInt(ctx.queryParam("groundSpace")) : 0;

        long currentTime = System.currentTimeMillis() / 1000L / 60L; // Get current time in minutes

        long startTime = ctx.queryParam("startTime") != null ? Long.parseLong(ctx.queryParam("startTime"))
                : currentTime;
        long endTime = ctx.queryParam("endTime") != null ? Long.parseLong(ctx.queryParam("endTime")) : currentTime + 60;

        // Update the airplane schedule in the database based on the provided start and
        // end times
        boolean updated = kluster.getDatabase().updateAirplaneSchedule(airplaneId, groundSpace, startTime, endTime);

        if (updated) {
            ctx.status(200).result("Airplane schedule updated successfully");
        } else {
            ctx.status(400).result("Failed to update airplane schedule");
        }
    }

}
