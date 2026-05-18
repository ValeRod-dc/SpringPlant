package com.example.product.security.filter;


import com.example.product.security.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7); // Extrae el token sin 'Bearer ' [cite: 361]

            if (jwtService.isTokenValid(token)) { // [cite: 362]
                String username = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token); // EXTRAE EL ROL (Ej: "ADMIN")

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Convertimos el rol en una autoridad reconocible por Spring.
                    // Usamos "ROLE_" + role para que haga match perfecto con tu hasAnyRole('ADMIN')
                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities); // Pasamos las autoridades reales

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); //
                    SecurityContextHolder.getContext().setAuthentication(authToken); //
                }
            }
        } catch (Exception e) {
            log.warn("JWT inválido o expirado: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}