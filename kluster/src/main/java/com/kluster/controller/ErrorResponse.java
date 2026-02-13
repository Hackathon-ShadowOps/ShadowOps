package com.kluster.controller;

public class ErrorResponse {
    public String message;
    public String endpoint;
    public int statusCode;

    public ErrorResponse(String message, String endpoint, int statusCode) {
        this.message = message;
        this.endpoint = endpoint;
        this.statusCode = statusCode;
    }
}
