package com.streetworkout.controller;

import com.streetworkout.dto.ProgressRequest;
import com.streetworkout.dto.ProgressResponse;
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
 * Controller du suivi de progression (poids, notes).
 *
 * Toutes les routes nécessitent un JWT valide.
 *
 * Routes :
 *   GET    /api/progress              → Mon historique
 *   POST   /api/progress              → Ajouter une entrée
 *   PUT    /api/progress/{id}         → Modifier une entrée
 *   DELETE /api/progress/{id}         → Supprimer une entrée
 */
@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    @Autowired
    private ProgressService progressService;

    /**
     * Récupère l'historique de progression de l'utilisateur connecté.
     *
     * Paramètres optionnels :
     * - startDate : date de début (format ISO : 2024-01-01)
     * - endDate   : date de fin   (format ISO : 2024-12-31)
     */
    @GetMapping
    public ResponseEntity<List<ProgressResponse>> getMyProgress(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(progressService.getMyProgress(startDate, endDate));
    }

    /**
     * Ajoute une entrée de progression.
     *
     * Body JSON :
     * {
     *   "progressDate": "2024-01-15",
     *   "weight": 74.5,
     *   "notes": "Je me sens plus léger !"
     * }
     *
     * Contrainte : une seule entrée par date par utilisateur.
     */
    @PostMapping
    public ResponseEntity<ProgressResponse> create(@Valid @RequestBody ProgressRequest request) {
        ProgressResponse response = progressService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Met à jour une entrée de progression existante.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProgressResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody ProgressRequest request
    ) {
        return ResponseEntity.ok(progressService.update(id, request));
    }

    /**
     * Supprime une entrée de progression.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        progressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
