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

    @Query("SELECT DISTINCT w FROM Workout w " +
           "LEFT JOIN FETCH w.workoutExercises we " +
           "LEFT JOIN FETCH we.exercise " +
           "WHERE w.user.id = :userId " +
           "ORDER BY w.workoutDate DESC")
    List<Workout> findByUserIdOrderByWorkoutDateDesc(@Param("userId") Long userId);

    /**
     * Pagination : IDs des seances d'un utilisateur (evite le probleme HHH90003004
     * avec JOIN FETCH + LIMIT).
     * On recupere d'abord les IDs pagines, puis on charge les entites completes.
     */
    @Query(value = "SELECT w.id FROM workouts w WHERE w.user_id = :userId ORDER BY w.workout_date DESC LIMIT :size OFFSET :offset",
           nativeQuery = true)
    List<Long> findIdsByUserIdPaged(@Param("userId") Long userId,
                                    @Param("size") int size,
                                    @Param("offset") int offset);

    /**
     * Compte total des seances d'un utilisateur (pour la pagination).
     */
    @Query("SELECT COUNT(w) FROM Workout w WHERE w.user.id = :userId")
    long countByUserIdQuery(@Param("userId") Long userId);

    /**
     * Pagination avec filtre de dates.
     */
    @Query(value = "SELECT w.id FROM workouts w WHERE w.user_id = :userId AND w.workout_date BETWEEN :startDate AND :endDate ORDER BY w.workout_date DESC LIMIT :size OFFSET :offset",
           nativeQuery = true)
    List<Long> findIdsByUserIdAndDateRangePaged(@Param("userId") Long userId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("size") int size,
                                                @Param("offset") int offset);

    @Query("SELECT COUNT(w) FROM Workout w WHERE w.user.id = :userId AND w.workoutDate BETWEEN :startDate AND :endDate")
    long countByUserIdAndDateRange(@Param("userId") Long userId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * Charge les seances completes (avec exercices) par liste d'IDs.
     */
    @Query("SELECT DISTINCT w FROM Workout w " +
           "LEFT JOIN FETCH w.workoutExercises we " +
           "LEFT JOIN FETCH we.exercise " +
           "WHERE w.id IN :ids " +
           "ORDER BY w.workoutDate DESC")
    List<Workout> findByIdsWithExercises(@Param("ids") List<Long> ids);

    @Query("SELECT DISTINCT w FROM Workout w " +
           "LEFT JOIN FETCH w.workoutExercises we " +
           "LEFT JOIN FETCH we.exercise " +
           "WHERE w.user.id = :userId " +
           "AND w.workoutDate BETWEEN :startDate AND :endDate " +
           "ORDER BY w.workoutDate DESC")
    List<Workout> findByUserIdAndWorkoutDateBetweenOrderByWorkoutDateDesc(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT w FROM Workout w " +
           "LEFT JOIN FETCH w.workoutExercises we " +
           "LEFT JOIN FETCH we.exercise " +
           "WHERE w.id = :id AND w.user.id = :userId")
    Optional<Workout> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(w.totalCalories), 0) FROM Workout w WHERE w.user.id = :userId")
    Integer sumCaloriesByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(w.duration), 0) FROM Workout w WHERE w.user.id = :userId")
    Integer sumDurationByUserId(@Param("userId") Long userId);
}