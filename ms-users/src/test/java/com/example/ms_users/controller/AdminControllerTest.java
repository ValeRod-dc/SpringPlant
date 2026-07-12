package com.example.ms_users.controller;

import com.example.ms_users.security.filter.JwtAuthFilter;
import com.example.ms_users.security.jwt.JwtService;
import com.example.ms_users.service.CustomUserDetailsService;
import com.example.ms_users.service.UserService;
import com.example.ms_users.model.Role;
import com.example.ms_users.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AdminController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
})
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private User createTestUser() {
        return User.builder()
                .userId(1L)
                .username("admin")
                .email("admin@example.com")
                .password("encoded")
                .role(Role.ADMIN)
                .phone("123456789")
                .address("Calle Admin")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.findByUsername("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/admin/users/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado: unknown"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetUserById() throws Exception {
        User user = createTestUser();
        when(userService.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.all.href").exists())
                .andExpect(jsonPath("$._links.update.href").exists())
                .andExpect(jsonPath("$._links.delete.href").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenUserByIdNotFound() throws Exception {
        when(userService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/admin/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCheckUserExistsById() throws Exception {
        when(userService.existsById(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/admin/users/exists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnUsersByRole() throws Exception {
        when(userService.findAll()).thenReturn(List.of(createTestUser()));

        mockMvc.perform(get("/api/v1/admin/users/role/ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/role/INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Rol inválido. Use: ADMIN, EMPLOYEE o CLIENT"));
    }
}