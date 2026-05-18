package com.example.ms_users.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private LocalDateTime timestamp;
    private Map<String, String> errors;

    // Constructor útil para errores sin validaciones
    public ErrorResponse(int status, String error) {
        this.status = status;
        this.error = error;
        this.timestamp = LocalDateTime.now();
        this.errors = Map.of();
    }
}