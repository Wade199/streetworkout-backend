package com.streetworkout.controller;

import com.streetworkout.dto.AuthResponse;
import com.streetworkout.dto.LoginRequest;
import com.streetworkout.dto.RegisterRequest;
import com.streetworkout.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller d'authentification avec rate limiting intégré.
 *
 * Rate limiting : max 10 tentatives de login par IP par fenêtre de 15 minutes.
 * Protège contre les attaques brute-force sur les mots de passe.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Compteur de tentatives par IP : IP → [count, windowStart]
    private final Map<String, long[]> loginAttempts = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MS = 15 * 60 * 1000L; // 15 minutes

    /**
     * Inscription d'un nouvel utilisateur.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Connexion avec rate limiting par IP.
     * Bloque après 10 tentatives échouées en 15 minutes.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);

        // Vérifier le rate limit
        if (isRateLimited(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                Map.of(
                    "error", "Too Many Requests",
                    "message", "Trop de tentatives de connexion. Réessayez dans 15 minutes.",
                    "status", 429
                )
            );
        }

        try {
            AuthResponse response = authService.login(request);
            // Succès — réinitialiser le compteur
            loginAttempts.remove(clientIp);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Échec — incrémenter le compteur
            incrementAttempts(clientIp);
            throw e;
        }
    }

    /**
     * Extrait l'IP réelle du client (gère les proxies).
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Vérifie si l'IP a dépassé le nombre de tentatives autorisées.
     */
    private boolean isRateLimited(String ip) {
        long[] data = loginAttempts.get(ip);
        if (data == null) return false;

        long count = data[0];
        long windowStart = data[1];
        long now = Instant.now().toEpochMilli();

        // Fenêtre expirée → réinitialiser
        if (now - windowStart > WINDOW_MS) {
            loginAttempts.remove(ip);
            return false;
        }

        return count >= MAX_ATTEMPTS;
    }

    /**
     * Incrémente le compteur de tentatives pour une IP.
     */
    private void incrementAttempts(String ip) {
        long now = Instant.now().toEpochMilli();
        loginAttempts.compute(ip, (key, existing) -> {
            if (existing == null || now - existing[1] > WINDOW_MS) {
                return new long[]{1, now};
            }
            existing[0]++;
            return existing;
        });
    }
}
