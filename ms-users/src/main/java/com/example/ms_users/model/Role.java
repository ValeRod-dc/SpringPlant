package com.example.ms_users.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rol de usuario",
        allowableValues = {"ADMIN", "EMPLOYEE", "CLIENT"})
public enum Role {
    ADMIN,
    EMPLOYEE,
    CLIENT
}
