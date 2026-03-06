package com.kluster.endpoints.backend.GET;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.kluster.Kluster;
import com.kluster.controller.APIEndpoint;
import com.kluster.models.AirplaneSchedules;

import io.javalin.http.Context;

public class AirplaneLandingSchedule extends APIEndpoint {
    private final Kluster kluster;

    public AirplaneLandingSchedule(Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/airplaneLandingSchedule";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        long currentTime = System.currentTimeMillis() / 1000L / 60L; // Get current time in minutes

        long startTime = ctx.queryParam("startTime") != null ? Long.parseLong(ctx.queryParam("startTime"))
                : currentTime;
        long endTime = ctx.queryParam("endTime") != null ? Long.parseLong(ctx.queryParam("endTime")) : currentTime + 60;

        ArrayList<AirplaneSchedules> schedules = kluster.getDatabase().getAirplaneLandingSchedule(startTime, endTime);

        Gson gson = new Gson();
        String json = gson.toJson(schedules);

        ctx.contentType("application/json");
        ctx.json(json);
    }
}
