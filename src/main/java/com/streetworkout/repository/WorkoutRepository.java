package com.streetworkout.repository;

import com.streetworkout.entity.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    // Toutes les séances d'un utilisateur, triées par date décroissante
    List<Workout> findByUserIdOrderByWorkoutDateDesc(Long userId);

    // Séances d'un utilisateur entre deux dates
    List<Workout> findByUserIdAndWorkoutDateBetweenOrderByWorkoutDateDesc(
        Long userId, LocalDate startDate, LocalDate endDate
    );

    // Vérifier qu'une séance appartient bien à un utilisateur
    Optional<Workout> findByIdAndUserId(Long id, Long userId);

    // Compter les séances d'un utilisateur
    long countByUserId(Long userId);

    // Statistiques : total des calories brûlées
    @Query("SELECT COALESCE(SUM(w.totalCalories), 0) FROM Workout w WHERE w.user.id = :userId")
    Integer sumCaloriesByUserId(@Param("userId") Long userId);

    // Statistiques : total des minutes d'entraînement
    @Query("SELECT COALESCE(SUM(w.duration), 0) FROM Workout w WHERE w.user.id = :userId")
    Integer sumDurationByUserId(@Param("userId") Long userId);
}
