package com.example.ms_users.controller;

import com.example.ms_users.dto.request.AuthRequestDTO;
import com.example.ms_users.dto.request.UserRequestDTO;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authManager;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldRegisterUser() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("newUser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setRole("CLIENT");
        request.setAddress("Calle Nueva 123");
        request.setPhone("987654321");

        User savedUser = User.builder()
                .userId(1L)
                .username("newUser")
                .email("new@example.com")
                .role(Role.CLIENT)
                .phone("987654321")
                .build();

        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(userService.register(anyString(), anyString(), any(Role.class), anyString(), anyString(), anyString()))
                .thenReturn(savedUser);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$._links.profile.href").exists());
    }

    @Test
    void shouldReturnConflictWhenUsernameExists() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("existingUser");
        request.setEmail("new@example.com");
        request.setPassword("pass");

        when(userService.existsByUsername("existingUser")).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("admin");
        request.setPassword("password");

        Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "password");
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        User user = User.builder()
                .userId(1L)
                .username("admin")
                .role(Role.ADMIN)
                .email("admin@example.com")
                .build();
        when(userService.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginFails() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("wrong");
        request.setPassword("wrong");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario o contraseña incorrectos"));
    }

    @Test
    @WithMockUser
    void shouldGetMyProfile() throws Exception {
        User user = User.builder()
                .userId(1L)
                .username("testUser")
                .email("test@example.com")
                .role(Role.CLIENT)
                .phone("123456789")
                .build();

        when(userService.findByUsername("testUser")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/auth/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("testUser"))
                .andExpect(jsonPath("$.user._links.profile.href").exists());
    }

    @Test
    void shouldCheckUserExists() throws Exception {
        when(userService.existsByUsername("existing")).thenReturn(true);

        mockMvc.perform(get("/api/v1/auth/user-exists/existing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }
}