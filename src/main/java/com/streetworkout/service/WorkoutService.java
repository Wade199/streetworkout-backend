package com.streetworkout.service;

import com.streetworkout.dto.WorkoutRequest;
import com.streetworkout.dto.WorkoutResponse;
import com.streetworkout.entity.Exercise;
import com.streetworkout.entity.User;
import com.streetworkout.entity.Workout;
import com.streetworkout.entity.WorkoutExercise;
import com.streetworkout.repository.ExerciseRepository;
import com.streetworkout.repository.WorkoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service gérant les séances d'entraînement.
 *
 * Chaque séance appartient à un utilisateur.
 * Une séance contient plusieurs exercices (WorkoutExercise).
 */
@Service
public class WorkoutService {

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private UserService userService;

    /**
     * Retourne toutes les séances de l'utilisateur connecté.
     * Filtrage optionnel par plage de dates.
     */
    public List<WorkoutResponse> getMyWorkouts(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();

        List<Workout> workouts;
        if (startDate != null && endDate != null) {
            workouts = workoutRepository.findByUserIdAndWorkoutDateBetweenOrderByWorkoutDateDesc(
                user.getId(), startDate, endDate
            );
        } else {
            workouts = workoutRepository.findByUserIdOrderByWorkoutDateDesc(user.getId());
        }

        return workouts.stream()
            .map(WorkoutResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Retourne une séance spécifique (vérification que l'utilisateur en est le propriétaire).
     */
    public WorkoutResponse getById(Long id) {
        User user = userService.getCurrentUser();
        Workout workout = workoutRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Séance non trouvée ou accès refusé"));
        return WorkoutResponse.fromEntity(workout);
    }

    /**
     * Crée une nouvelle séance avec ses exercices.
     *
     * Processus :
     * 1. Créer l'entité Workout
     * 2. Pour chaque exercice dans la requête, créer un WorkoutExercise
     * 3. Sauvegarder en cascade
     */
    @Transactional
    public WorkoutResponse create(WorkoutRequest request) {
        User user = userService.getCurrentUser();

        Workout workout = new Workout();
        workout.setUser(user);
        workout.setTitle(request.getTitle());
        workout.setWorkoutDate(request.getWorkoutDate());
        workout.setDuration(request.getDuration());
        workout.setTotalCalories(request.getTotalCalories());
        workout.setNotes(request.getNotes());

        // Ajouter les exercices à la séance
        for (WorkoutRequest.WorkoutExerciseRequest exReq : request.getExercises()) {
            Exercise exercise = exerciseRepository.findById(exReq.getExerciseId())
                .orElseThrow(() -> new RuntimeException(
                    "Exercice non trouvé avec l'id : " + exReq.getExerciseId()
                ));

            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkout(workout);
            workoutExercise.setExercise(exercise);
            workoutExercise.setSets(exReq.getSets());
            workoutExercise.setReps(exReq.getReps());

            workout.getWorkoutExercises().add(workoutExercise);
        }

        Workout saved = workoutRepository.save(workout);
        return WorkoutResponse.fromEntity(saved);
    }

    /**
     * Met à jour une séance existante.
     * Remplace complètement les exercices de la séance.
     */
    @Transactional
    public WorkoutResponse update(Long id, WorkoutRequest request) {
        User user = userService.getCurrentUser();
        Workout workout = workoutRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Séance non trouvée ou accès refusé"));

        workout.setTitle(request.getTitle());
        workout.setWorkoutDate(request.getWorkoutDate());
        workout.setDuration(request.getDuration());
        workout.setTotalCalories(request.getTotalCalories());
        workout.setNotes(request.getNotes());

        // Remplacer les exercices (orphanRemoval = true supprime les anciens)
        workout.getWorkoutExercises().clear();

        for (WorkoutRequest.WorkoutExerciseRequest exReq : request.getExercises()) {
            Exercise exercise = exerciseRepository.findById(exReq.getExerciseId())
                .orElseThrow(() -> new RuntimeException(
                    "Exercice non trouvé avec l'id : " + exReq.getExerciseId()
                ));

            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkout(workout);
            workoutExercise.setExercise(exercise);
            workoutExercise.setSets(exReq.getSets());
            workoutExercise.setReps(exReq.getReps());

            workout.getWorkoutExercises().add(workoutExercise);
        }

        return WorkoutResponse.fromEntity(workoutRepository.save(workout));
    }

    /**
     * Supprime une séance (et ses exercices en cascade).
     */
    @Transactional
    public void delete(Long id) {
        User user = userService.getCurrentUser();
        Workout workout = workoutRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Séance non trouvée ou accès refusé"));
        workoutRepository.delete(workout);
    }

    /**
     * Statistiques globales de l'utilisateur.
     */
    public Map<String, Object> getStats() {
        User user = userService.getCurrentUser();
        return Map.of(
            "totalWorkouts", workoutRepository.countByUserId(user.getId()),
            "totalCalories", workoutRepository.sumCaloriesByUserId(user.getId()),
            "totalMinutes", workoutRepository.sumDurationByUserId(user.getId())
        );
    }
}
