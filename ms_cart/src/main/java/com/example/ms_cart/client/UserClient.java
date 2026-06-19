package com.example.ms_cart.client;

import com.example.ms_cart.exception.custom.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
public class UserClient {

    private final WebClient webClient;

    @Autowired
    public UserClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://MS-USERS")
                .build();
    }

    public boolean userExists(String username) {
        log.debug("Consultando existencia de usuario en ms_users: {}", username);
        try {
            Map response = webClient.get()
                    .uri("/api/v1/auth/user-exists/{username}", username)
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
            Map response = webClient.get()
                    .uri("/api/v1/auth/user-id/{username}", username)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return response != null ? ((Number) response.get("userId")).longValue() : null;
        } catch (Exception e) {
            log.error("Error obteniendo userId para {}: {}", username, e.getMessage());
            throw new UserNotFoundException("Usuario no encontrado: " + username);
        }
    }
}