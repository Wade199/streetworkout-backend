package com.streetworkout.controller;

import com.streetworkout.dto.WorkoutRequest;
import com.streetworkout.dto.WorkoutResponse;
import com.streetworkout.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller des séances d'entraînement.
 *
 * Toutes les routes nécessitent un JWT valide.
 * Chaque utilisateur ne voit que SES propres séances.
 *
 * Routes :
 *   GET    /api/workouts              → Mes séances
 *   GET    /api/workouts/{id}         → Une séance
 *   POST   /api/workouts              → Créer une séance
 *   PUT    /api/workouts/{id}         → Modifier une séance
 *   DELETE /api/workouts/{id}         → Supprimer une séance
 *   GET    /api/workouts/stats        → Mes statistiques
 */
@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    @Autowired
    private WorkoutService workoutService;

    /**
     * Récupère toutes les séances de l'utilisateur connecté.
     *
     * Paramètres optionnels :
     * - startDate : date de début (format ISO : 2024-01-01)
     * - endDate   : date de fin   (format ISO : 2024-12-31)
     *
     * Exemple : GET /api/workouts?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping
    public ResponseEntity<List<WorkoutResponse>> getMyWorkouts(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(workoutService.getMyWorkouts(startDate, endDate));
    }

    /**
     * Récupère les statistiques globales de l'utilisateur.
     *
     * Réponse :
     * {
     *   "totalWorkouts": 42,
     *   "totalCalories": 15000,
     *   "totalMinutes": 2100
     * }
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(workoutService.getStats());
    }

    /**
     * Récupère une séance par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutService.getById(id));
    }

    /**
     * Crée une nouvelle séance.
     *
     * Body JSON :
     * {
     *   "title": "Séance Push du lundi",
     *   "workoutDate": "2024-01-15",
     *   "duration": 45,
     *   "totalCalories": 300,
     *   "notes": "Bonne séance !",
     *   "exercises": [
     *     { "exerciseId": 1, "sets": 3, "reps": 15 },
     *     { "exerciseId": 4, "sets": 3, "reps": 10 }
     *   ]
     * }
     */
    @PostMapping
    public ResponseEntity<WorkoutResponse> create(@Valid @RequestBody WorkoutRequest request) {
        WorkoutResponse response = workoutService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Met à jour une séance existante.
     * Remplace complètement les exercices de la séance.
     */
    @PutMapping("/{id}")
    public ResponseEntity<WorkoutResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody WorkoutRequest request
    ) {
        return ResponseEntity.ok(workoutService.update(id, request));
    }

    /**
     * Supprime une séance et tous ses exercices associés.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workoutService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
