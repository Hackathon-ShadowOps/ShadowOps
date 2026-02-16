package com.kluster.models;

public class IncidentReport {
    private int id;
    private long timestamp;
    private int signedBy;
    private String description;
    private String category;

    public IncidentReport(int id, String description, String category, int signedBy, long timestamp) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.signedBy = signedBy;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public int getSignedBy() {
        return signedBy;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
