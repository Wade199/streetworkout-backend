package com.streetworkout.controller;

import com.streetworkout.dto.AuthResponse;
import com.streetworkout.dto.LoginRequest;
import com.streetworkout.dto.RegisterRequest;
import com.streetworkout.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller d'authentification.
 *
 * Routes publiques (pas de JWT requis) :
 *   POST /api/auth/register  → Inscription
 *   POST /api/auth/login     → Connexion
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Inscription d'un nouvel utilisateur.
     *
     * Body JSON :
     * {
     *   "username": "john_doe",
     *   "email": "john@example.com",
     *   "password": "motdepasse123",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "height": 180.5,
     *   "weight": 75.0
     * }
     *
     * Réponse 201 :
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "type": "Bearer",
     *   "user": { "id": 1, "username": "john_doe", ... }
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Connexion d'un utilisateur existant.
     *
     * Body JSON :
     * {
     *   "email": "john@example.com",
     *   "password": "motdepasse123"
     * }
     *
     * Réponse 200 :
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "type": "Bearer",
     *   "user": { "id": 1, "username": "john_doe", ... }
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
