package com.example.ms_cart.client;

import com.example.ms_cart.exception.custom.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@Component
public class UserClient {

    private final WebClient webClient;

    @Autowired
    public UserClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://MS-USERS")   // Ajusta el nombre según Eureka
                .build();
    }

    private String getAuthToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("Authorization");
        }
        return null;
    }

    public boolean userExists(String username) {
        log.debug("Consultando existencia de usuario en ms_users: {}", username);
        try {
            String token = getAuthToken();
            WebClient.RequestHeadersSpec<?> request = webClient.get()
                    .uri("/api/v1/auth/user-exists/{username}", username);
            if (token != null && !token.isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, token);
            }

            Map response = request
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            boolean exists = response != null && (Boolean) response.getOrDefault("exists", false);
            log.debug("Usuario {} - Existe: {}", username, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error validando usuario '{}' con ms_users: {}", username, e.getMessage(), e);
            return false;
        }
    }

    public Long getUserIdByUsername(String username) {
        log.debug("Obteniendo ID de usuario desde ms_users: {}", username);
        try {
            String token = getAuthToken();
            WebClient.RequestHeadersSpec<?> request = webClient.get()
                    .uri("/api/v1/auth/user-id/{username}", username);
            if (token != null && !token.isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, token);
            }

            Map response = request
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return response != null ? ((Number) response.get("userId")).longValue() : null;
        } catch (Exception e) {
            log.error("Error obteniendo userId para {}: {}", username, e.getMessage(), e);
            throw new UserNotFoundException("Usuario no encontrado: " + username);
        }
    }
}