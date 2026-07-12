package com.example.ms_review.client;

import com.example.ms_review.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-order")
public interface OrderClient {

    @GetMapping("/api/v1/orders/{id}")
    ResponseEntity<OrderDto> getOrderById(@PathVariable("id") Long id);
}
