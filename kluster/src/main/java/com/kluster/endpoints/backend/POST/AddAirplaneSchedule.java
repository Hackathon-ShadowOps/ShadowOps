package com.kluster.endpoints.backend.POST;

import com.kluster.controller.APIEndpoint;

import io.javalin.http.Context;

import com.google.gson.Gson;

public class AddAirplaneSchedule extends APIEndpoint {
    private final com.kluster.Kluster kluster;
    private final Gson gson = new Gson();

    public AddAirplaneSchedule(com.kluster.Kluster kluster) {
        this.kluster = kluster;
    }

    @Override
    public String path() {
        return "/api/v1/addAirplaneSchedule";
    }

    @Override
    public void handle(Context ctx) throws UnsupportedOperationException {
        String json = ctx.body();

        System.out.println("Received JSON: " + json);

        AirplaneScheduleRequest request = gson.fromJson(json, AirplaneScheduleRequest.class);

        String airplaneId = request.airplaneId;
        long startTime = request.startTime;
        long endTime = request.endTime;
    int groundSpace = request.groundSpace;

        if (airplaneId == null || startTime == 0 || endTime == 0 || startTime >= endTime || groundSpace <= 0) {
            ctx.status(400).result("Missing required parameters");
            return;
        }

        boolean added = kluster.getDatabase().addAirplaneSchedule(airplaneId, groundSpace, startTime, endTime);

        if (added) {
            ctx.status(200).result("Airplane schedule added successfully");
        } else {
            ctx.status(400).result("Failed to add airplane schedule");
        }
    }

    public static class AirplaneScheduleRequest {
        public String airplaneId;
        public long startTime;
        public long endTime;
        public int groundSpace;
    }
}
