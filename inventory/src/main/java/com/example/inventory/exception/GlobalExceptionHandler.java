package com.example.inventory.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationError(MethodArgumentNotValidException ex) {
        HashMap<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return new ErrorResponse(400, "Error de validación", errors);
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleInventoryNotFound(InventoryNotFoundException ex) {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("mensaje", ex.getMessage());
        return new ErrorResponse(404, "Inventario no encontrado", errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("mensaje", ex.getMessage());
        return new ErrorResponse(400, "Parámetro inválido", errors);
    }

    @ExceptionHandler(FeignException.NotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleFeignNotFound(FeignException.NotFound ex) {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("mensaje", "El producto solicitado no existe en el inventario");
        return new ErrorResponse(404, "Producto no encontrado", errors);
    }

    @ExceptionHandler(FeignException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleFeignError(FeignException ex) {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("mensaje", "El servicio de productos no está disponible. Intente más tarde.");
        return new ErrorResponse(503, "Servicio no disponible", errors);
    }
}
