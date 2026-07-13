package com.example.ms_inventory.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long userId;

    private String username;

    private String email;

    private String address;
    private String phone;
}
