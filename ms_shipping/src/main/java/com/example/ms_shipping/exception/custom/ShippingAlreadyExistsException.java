package com.example.ms_shipping.exception.custom;

public class ShippingAlreadyExistsException extends RuntimeException {
    public ShippingAlreadyExistsException(String message) {
        super(message);
    }
}
