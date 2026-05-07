package com.streetworkout.dto;

import com.streetworkout.entity.Workout;
import com.streetworkout.entity.WorkoutExercise;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutResponse {

    private Long id;
    private String title;
    private LocalDate workoutDate;
    private Integer duration;
    private Integer totalCalories;
    private String notes;
    private LocalDateTime createdAt;
    private List<WorkoutExerciseResponse> exercises;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutExerciseResponse {
        private Long id;
        private Long exerciseId;
        private String exerciseName;
        private String category;
        private String difficulty;
        private Integer sets;
        private Integer reps;
    }

    public static WorkoutResponse fromEntity(Workout workout) {
        List<WorkoutExerciseResponse> exercises = workout.getWorkoutExercises().stream()
            .map(we -> new WorkoutExerciseResponse(
                we.getId(),
                we.getExercise().getId(),
                we.getExercise().getName(),
                we.getExercise().getCategory().name(),
                we.getExercise().getDifficulty().name(),
                we.getSets(),
                we.getReps()
            ))
            .collect(Collectors.toList());

        return new WorkoutResponse(
            workout.getId(),
            workout.getTitle(),
            workout.getWorkoutDate(),
            workout.getDuration(),
            workout.getTotalCalories(),
            workout.getNotes(),
            workout.getCreatedAt(),
            exercises
        );
    }
}
