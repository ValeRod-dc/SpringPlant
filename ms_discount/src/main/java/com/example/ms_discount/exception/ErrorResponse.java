package com.example.ms_discount.exception;

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
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> errors;

    // Constructor para errores simples (3 parámetros)
    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = Map.of();
    }

    // Constructor para errores de validación (con timestamp)
    public ErrorResponse(int status, String error, LocalDateTime timestamp, Map<String, String> errors) {
        this.status = status;
        this.error = error;
        this.timestamp = timestamp;
        this.errors = errors;
        this.message = "Error de validación";
    }
}