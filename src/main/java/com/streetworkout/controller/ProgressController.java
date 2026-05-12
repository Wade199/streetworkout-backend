package com.streetworkout.controller;

import com.streetworkout.dto.ProgressRequest;
import com.streetworkout.dto.ProgressResponse;
import com.streetworkout.dto.ProgressStatsResponse;
import com.streetworkout.service.ProgressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller du suivi de progression.
 *
 * Routes :
 *   GET    /api/progress              → Historique (liste)
 *   GET    /api/progress/stats        → Statistiques pour graphique
 *   POST   /api/progress              → Ajouter une entree
 *   PUT    /api/progress/{id}         → Modifier une entree
 *   DELETE /api/progress/{id}         → Supprimer une entree
 */
@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    @Autowired
    private ProgressService progressService;

    /**
     * Historique de progression (liste brute).
     *
     * GET /api/progress
     * GET /api/progress?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping
    public ResponseEntity<List<ProgressResponse>> getMyProgress(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(progressService.getMyProgress(startDate, endDate));
    }

    /**
     * Statistiques de progression pour le graphique d'evolution du poids.
     *
     * GET /api/progress/stats
     * GET /api/progress/stats?startDate=2024-01-01&endDate=2024-12-31
     *
     * Reponse :
     * {
     *   "entries": [
     *     { "id": 1, "progressDate": "2024-01-01", "weight": 80.0, "notes": "..." },
     *     { "id": 2, "progressDate": "2024-01-15", "weight": 79.2, "notes": "..." }
     *   ],
     *   "currentWeight": 79.2,
     *   "minWeight": 79.2,
     *   "maxWeight": 80.0,
     *   "weightChange": -0.80,
     *   "weightChangePct": -1.00,
     *   "totalEntries": 2
     * }
     *
     * Les entries sont triees par date ASC pour l'axe X du graphique.
     */
    @GetMapping("/stats")
    public ResponseEntity<ProgressStatsResponse> getStats(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(progressService.getStats(startDate, endDate));
    }

    /**
     * Ajoute une entree de progression.
     *
     * POST /api/progress
     * Body : { "progressDate": "2024-01-15", "weight": 74.5, "notes": "..." }
     */
    @PostMapping
    public ResponseEntity<ProgressResponse> create(@Valid @RequestBody ProgressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(progressService.create(request));
    }

    /**
     * Met a jour une entree de progression.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProgressResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody ProgressRequest request
    ) {
        return ResponseEntity.ok(progressService.update(id, request));
    }

    /**
     * Supprime une entree de progression.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        progressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
