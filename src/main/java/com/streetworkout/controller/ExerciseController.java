package com.streetworkout.controller;

import com.streetworkout.entity.Exercise;
import com.streetworkout.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller du catalogue d'exercices.
 *
 * Routes GET accessibles SANS JWT (configuré dans SecurityConfig).
 * Utile pour afficher le catalogue avant connexion.
 *
 * Routes :
 *   GET /api/exercises                          → Tous les exercices
 *   GET /api/exercises?category=PUSH            → Filtrer par catégorie
 *   GET /api/exercises?difficulty=BEGINNER      → Filtrer par difficulté
 *   GET /api/exercises?category=PUSH&difficulty=BEGINNER → Double filtre
 *   GET /api/exercises/search?name=push         → Recherche par nom
 *   GET /api/exercises/{id}                     → Un exercice par ID
 */
@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    @Autowired
    private ExerciseService exerciseService;

    /**
     * Liste tous les exercices avec filtres optionnels.
     *
     * Paramètres de requête (query params) :
     * - category : PUSH | PULL | LEGS | CORE
     * - difficulty : BEGINNER | INTERMEDIATE | ADVANCED
     *
     * Exemple : GET /api/exercises?category=PUSH&difficulty=BEGINNER
     */
    @GetMapping
    public ResponseEntity<List<Exercise>> getAll(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String difficulty
    ) {
        return ResponseEntity.ok(exerciseService.getAll(category, difficulty));
    }

    /**
     * Recherche des exercices par nom.
     * Exemple : GET /api/exercises/search?name=push
     */
    @GetMapping("/search")
    public ResponseEntity<List<Exercise>> search(@RequestParam String name) {
        return ResponseEntity.ok(exerciseService.search(name));
    }

    /**
     * Récupère un exercice par son ID.
     * Exemple : GET /api/exercises/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Exercise> getById(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseService.getById(id));
    }
}
