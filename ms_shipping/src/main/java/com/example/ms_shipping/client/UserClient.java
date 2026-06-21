package com.example.ms_shipping.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class UserClient {

    private final WebClient webClient;

    @Autowired
    public UserClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://ms-users")
                .build();
    }

    private String getAuthToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest().getHeader("Authorization");
        }
        return null;
    }

    public boolean userExistsById(Long userId) {
        log.debug("Consultando existencia de usuario por ID: {} en ms-users", userId);
        try {
            String token = getAuthToken();
            log.debug("Token presente: {}", token != null ? "Sí" : "No");

            WebClient.RequestHeadersSpec<?> request = webClient.get()
                    .uri("/api/v1/admin/users/exists/{id}", userId);
            if (token != null && !token.isBlank()) {
                request.header(HttpHeaders.AUTHORIZATION, token);
            }

            // Para depuración, obtenemos el status y el cuerpo
            Map response = request
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                log.error("Código de estado recibido: {}", clientResponse.statusCode());
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(body -> {
                                            log.error("Cuerpo de error: {}", body);
                                            return Mono.error(new RuntimeException("Error en ms-users: " + body));
                                        });
                            })
                    .bodyToMono(Map.class)
                    .block();

            boolean exists = response != null && (Boolean) response.getOrDefault("exists", false);
            log.debug("Usuario ID {} - Existe: {}", userId, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error validando usuario ID {} con ms-users: {}", userId, e.getMessage(), e);
            return false;
        }
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
}