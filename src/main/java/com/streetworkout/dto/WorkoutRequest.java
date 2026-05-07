package com.streetworkout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @NotNull(message = "La date de la séance est obligatoire")
    private LocalDate workoutDate;

    private Integer duration; // en minutes
    private Integer totalCalories;
    private String notes;

    @NotEmpty(message = "La séance doit contenir au moins un exercice")
    private List<WorkoutExerciseRequest> exercises;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutExerciseRequest {
        @NotNull(message = "L'ID de l'exercice est obligatoire")
        private Long exerciseId;

        @NotNull(message = "Le nombre de séries est obligatoire")
        private Integer sets;

        @NotNull(message = "Le nombre de répétitions est obligatoire")
        private Integer reps;
    }
}
