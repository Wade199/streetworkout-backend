package com.streetworkout.service;

import com.streetworkout.entity.Exercise;
import com.streetworkout.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service gérant le catalogue d'exercices.
 * Les exercices sont en lecture seule pour les utilisateurs (créés par les admins).
 */
@Service
public class ExerciseService {

    @Autowired
    private ExerciseRepository exerciseRepository;

    /**
     * Retourne tous les exercices, avec filtres optionnels.
     */
    public List<Exercise> getAll(String category, String difficulty) {
        if (category != null && difficulty != null) {
            return exerciseRepository.findByCategoryAndDifficulty(
                Exercise.Category.valueOf(category.toUpperCase()),
                Exercise.Difficulty.valueOf(difficulty.toUpperCase())
            );
        }
        if (category != null) {
            return exerciseRepository.findByCategory(
                Exercise.Category.valueOf(category.toUpperCase())
            );
        }
        if (difficulty != null) {
            return exerciseRepository.findByDifficulty(
                Exercise.Difficulty.valueOf(difficulty.toUpperCase())
            );
        }
        return exerciseRepository.findAll();
    }

    /**
     * Retourne un exercice par son ID.
     */
    public Exercise getById(Long id) {
        return exerciseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Exercice non trouvé avec l'id : " + id));
    }

    /**
     * Recherche des exercices par nom.
     */
    public List<Exercise> search(String name) {
        return exerciseRepository.findByNameContainingIgnoreCase(name);
    }
}
