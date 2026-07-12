package com.example.ms_discount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MsDiscountApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsDiscountApplication.class, args);
    }
}