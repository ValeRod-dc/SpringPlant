package com.example.ms_users.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDTO {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String address;
    private String phone;
    private LocalDateTime createdAt;
}