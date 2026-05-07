package com.streetworkout.controller;

import com.streetworkout.dto.UserResponse;
import com.streetworkout.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller de gestion du profil utilisateur.
 *
 * Toutes les routes nécessitent un JWT valide dans le header :
 *   Authorization: Bearer <token>
 *
 * Routes :
 *   GET  /api/users/me        → Récupérer son profil
 *   PUT  /api/users/me        → Mettre à jour son profil
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Récupère le profil de l'utilisateur connecté.
     *
     * Réponse 200 :
     * {
     *   "id": 1,
     *   "username": "john_doe",
     *   "email": "john@example.com",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "height": 180.5,
     *   "weight": 75.0,
     *   "createdAt": "2024-01-15T10:30:00"
     * }
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile());
    }

    /**
     * Met à jour le profil de l'utilisateur connecté.
     * Seuls les champs envoyés sont modifiés (PATCH partiel via PUT).
     *
     * Body JSON (tous les champs sont optionnels) :
     * {
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "height": 181.0,
     *   "weight": 74.5,
     *   "password": "nouveauMotDePasse"
     * }
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(userService.updateProfile(updates));
    }
}
