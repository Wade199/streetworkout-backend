package com.streetworkout.controller;

import com.streetworkout.dto.UpdateProfileRequest;
import com.streetworkout.dto.UserResponse;
import com.streetworkout.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller de gestion du profil utilisateur.
 *
 * Routes :
 *   GET    /api/users/me   → Recuperer son profil
 *   PUT    /api/users/me   → Modifier son profil (poids, taille, nom, mdp)
 *   DELETE /api/users/me   → Supprimer son compte (RGPD)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Recupere le profil de l'utilisateur connecte.
     *
     * GET /api/users/me
     * Authorization: Bearer <token>
     *
     * Reponse 200 :
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
     * Met a jour le profil de l'utilisateur connecte.
     * Seuls les champs envoyes sont modifies (mise a jour partielle).
     *
     * PUT /api/users/me
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
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    /**
     * Supprime le compte de l'utilisateur connecte (RGPD).
     * Supprime en cascade toutes ses donnees (workouts, progress).
     *
     * DELETE /api/users/me
     *
     * Reponse 200 :
     * { "message": "Compte supprime avec succes" }
     */
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteAccount() {
        userService.deleteAccount();
        return ResponseEntity.ok(Map.of("message", "Compte supprime avec succes"));
    }
}
