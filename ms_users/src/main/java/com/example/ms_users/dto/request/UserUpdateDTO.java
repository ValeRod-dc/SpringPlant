package com.example.ms_users.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @Email(message = "Formato de email inválido")
    private String email;

    private String address;

    private String phone;
}
