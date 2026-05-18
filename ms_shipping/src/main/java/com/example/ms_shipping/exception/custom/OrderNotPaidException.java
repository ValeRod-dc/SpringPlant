package com.example.ms_shipping.exception.custom;

public class OrderNotPaidException extends RuntimeException {
    public OrderNotPaidException(String message) {
        super(message);
    }
}