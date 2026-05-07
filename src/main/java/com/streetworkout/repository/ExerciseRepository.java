package com.streetworkout.repository;

import com.streetworkout.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    // Filtrer par catégorie (PUSH, PULL, LEGS, CORE)
    List<Exercise> findByCategory(Exercise.Category category);

    // Filtrer par difficulté
    List<Exercise> findByDifficulty(Exercise.Difficulty difficulty);

    // Filtrer par catégorie ET difficulté
    List<Exercise> findByCategoryAndDifficulty(Exercise.Category category, Exercise.Difficulty difficulty);

    // Recherche par nom (insensible à la casse)
    List<Exercise> findByNameContainingIgnoreCase(String name);
}
