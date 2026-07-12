package com.example.ms_discount.exception.custom;

public class CouponNotActiveException extends RuntimeException {
    public CouponNotActiveException(String message) {
        super(message);
    }
}
