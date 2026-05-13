package com.streetworkout.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Renvoie 401 Unauthorized au lieu de 403 Forbidden quand le token est absent ou invalide.
 * Sans ce composant, Spring Security renvoie 403 ce qui empeche l intercepteur
 * Angular de detecter l expiration et de rediriger vers /login.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 401);
        body.put("error", "Unauthorized");
        body.put("message", "Token JWT absent, invalide ou expire. Reconnectez-vous.");
        body.put("timestamp", LocalDateTime.now().toString());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}