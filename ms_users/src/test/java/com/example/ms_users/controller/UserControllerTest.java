package com.example.ms_users.controller;

import com.example.ms_users.dto.request.UserUpdateDTO;
import com.example.ms_users.model.Role;
import com.example.ms_users.model.User;
import com.example.ms_users.security.filter.JwtAuthFilter;
import com.example.ms_users.security.jwt.JwtService;
import com.example.ms_users.service.CustomUserDetailsService;
import com.example.ms_users.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UserController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

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
                .username("testUser")
                .email("test@example.com")
                .role(Role.CLIENT)
                .phone("123456789")
                .address("Calle Test")
                .build();
    }

    @Test
    @WithMockUser(username = "testUser")
    void shouldGetMyProfile() throws Exception {
        when(userService.findByUsername("testUser")).thenReturn(Optional.of(createTestUser()));

        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("testUser"))
                .andExpect(jsonPath("$.user._links.self.href").exists())
                .andExpect(jsonPath("$.user._links.update.href").exists());
    }

    @Test
    @WithMockUser(username = "testUser")
    void shouldUpdateMyProfile() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("new@example.com");
        updateDTO.setPhone("987654321");
        updateDTO.setAddress("Nueva Dirección");

        User updatedUser = createTestUser();
        updatedUser.setEmail("new@example.com");
        updatedUser.setPhone("987654321");
        updatedUser.setAddress("Nueva Dirección");

        when(userService.updateUserProfile(anyString(), any(UserUpdateDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("new@example.com"))
                .andExpect(jsonPath("$.user._links.self.href").exists());
    }

    @Test
    @WithMockUser(username = "testUser")
    void shouldDeleteOwnAccount() throws Exception {
        when(userService.findByUsername("testUser")).thenReturn(Optional.of(createTestUser()));
        doNothing().when(userService).deleteByUsername("testUser");

        mockMvc.perform(delete("/api/v1/user/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tu cuenta ha sido eliminada correctamente"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.findByUsername("testUser")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado: testUser"));
    }
}