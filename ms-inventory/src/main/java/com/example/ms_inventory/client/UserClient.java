package com.example.ms_inventory.client;

import com.example.ms_inventory.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-users")
public interface UserClient {

    @GetMapping("/api/v1/auth/user-exists/{username}")
    ResponseEntity<UserDTO> getUserByUsername(@PathVariable("username") String username);

}

