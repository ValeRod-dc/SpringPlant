package com.example.inventory.exception;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;

@Getter
public class ErrorResponse {
    private final int status;
    private final String error;
    private final LocalDateTime timestamp;
    private final HashMap<String, String> errors;

    public ErrorResponse(int status, String error, HashMap<String, String> errors) {
        this.status = status;
        this.error = error;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }
}
