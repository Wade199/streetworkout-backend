package com.streetworkout.controller;

import com.streetworkout.dto.PageResponse;
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
import java.util.Map;

/**
 * Controller des seances d'entrainement.
 *
 * Routes :
 *   GET    /api/workouts              → Mes seances (paginées)
 *   GET    /api/workouts/stats        → Mes statistiques
 *   GET    /api/workouts/{id}         → Une seance
 *   POST   /api/workouts              → Creer une seance
 *   PUT    /api/workouts/{id}         → Modifier une seance
 *   DELETE /api/workouts/{id}         → Supprimer une seance
 */
@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    @Autowired
    private WorkoutService workoutService;

    /**
     * Recupere les seances de l'utilisateur avec pagination.
     *
     * GET /api/workouts?page=0&size=10
     * GET /api/workouts?page=0&size=10&startDate=2024-01-01&endDate=2024-12-31
     *
     * Parametres :
     * - page      : numero de page (defaut 0)
     * - size      : nombre d'elements par page (defaut 10, max 50)
     * - startDate : filtre date debut (optionnel)
     * - endDate   : filtre date fin (optionnel)
     *
     * Reponse :
     * {
     *   "content": [...],
     *   "page": 0,
     *   "size": 10,
     *   "totalElements": 42,
     *   "totalPages": 5,
     *   "first": true,
     *   "last": false
     * }
     */
    @GetMapping
    public ResponseEntity<PageResponse<WorkoutResponse>> getMyWorkouts(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Limiter la taille max a 50 pour eviter les surcharges
        size = Math.min(size, 50);
        return ResponseEntity.ok(workoutService.getMyWorkouts(page, size, startDate, endDate));
    }

    /**
     * Statistiques globales de l'utilisateur.
     *
     * GET /api/workouts/stats
     * Reponse : { "totalWorkouts": 42, "totalCalories": 15000, "totalMinutes": 2100 }
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(workoutService.getStats());
    }

    /**
     * Recupere une seance par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutService.getById(id));
    }

    /**
     * Cree une nouvelle seance.
     *
     * POST /api/workouts
     * Body :
     * {
     *   "title": "Seance Push du lundi",
     *   "workoutDate": "2024-01-15",
     *   "duration": 45,
     *   "totalCalories": 300,
     *   "notes": "Bonne seance !",
     *   "exercises": [
     *     { "exerciseId": 1, "sets": 3, "reps": 15 },
     *     { "exerciseId": 4, "sets": 3, "reps": 10 }
     *   ]
     * }
     */
    @PostMapping
    public ResponseEntity<WorkoutResponse> create(@Valid @RequestBody WorkoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutService.create(request));
    }

    /**
     * Met a jour une seance existante.
     */
    @PutMapping("/{id}")
    public ResponseEntity<WorkoutResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody WorkoutRequest request
    ) {
        return ResponseEntity.ok(workoutService.update(id, request));
    }

    /**
     * Supprime une seance et ses exercices en cascade.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workoutService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
