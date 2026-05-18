package com.example.ms_shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MsShippingApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsShippingApplication.class, args);
    }
}