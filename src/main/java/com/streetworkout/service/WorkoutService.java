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

@Service
public class WorkoutService {

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private UserService userService;

    @Transactional(readOnly = true)
    public List<WorkoutResponse> getMyWorkouts(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();
        List<Workout> workouts;
        if (startDate != null && endDate != null) {
            workouts = workoutRepository.findByUserIdAndWorkoutDateBetweenOrderByWorkoutDateDesc(
                user.getId(), startDate, endDate);
        } else {
            workouts = workoutRepository.findByUserIdOrderByWorkoutDateDesc(user.getId());
        }
        return workouts.stream().map(WorkoutResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkoutResponse getById(Long id) {
        User user = userService.getCurrentUser();
        Workout workout = workoutRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Seance non trouvee ou acces refuse"));
        return WorkoutResponse.fromEntity(workout);
    }

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
        for (WorkoutRequest.WorkoutExerciseRequest exReq : request.getExercises()) {
            Exercise exercise = exerciseRepository.findById(exReq.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercice non trouve : " + exReq.getExerciseId()));
            WorkoutExercise we = new WorkoutExercise();
            we.setWorkout(workout);
            we.setExercise(exercise);
            we.setSets(exReq.getSets());
            we.setReps(exReq.getReps());
            workout.getWorkoutExercises().add(we);
        }
        return WorkoutResponse.fromEntity(workoutRepository.save(workout));
    }

    @Transactional
    public WorkoutResponse update(Long id, WorkoutRequest request) {
        User user = userService.getCurrentUser();
        Workout workout = workoutRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Seance non trouvee ou acces refuse"));
        workout.setTitle(request.getTitle());
        workout.setWorkoutDate(request.getWorkoutDate());
        workout.setDuration(request.getDuration());
        workout.setTotalCalories(request.getTotalCalories());
        workout.setNotes(request.getNotes());
        workout.getWorkoutExercises().clear();
        for (WorkoutRequest.WorkoutExerciseRequest exReq : request.getExercises()) {
            Exercise exercise = exerciseRepository.findById(exReq.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercice non trouve : " + exReq.getExerciseId()));
            WorkoutExercise we = new WorkoutExercise();
            we.setWorkout(workout);
            we.setExercise(exercise);
            we.setSets(exReq.getSets());
            we.setReps(exReq.getReps());
            workout.getWorkoutExercises().add(we);
        }
        return WorkoutResponse.fromEntity(workoutRepository.save(workout));
    }

    @Transactional
    public void delete(Long id) {
        User user = userService.getCurrentUser();
        Workout workout = workoutRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Seance non trouvee ou acces refuse"));
        workoutRepository.delete(workout);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        User user = userService.getCurrentUser();
        return Map.of(
            "totalWorkouts", workoutRepository.countByUserId(user.getId()),
            "totalCalories", workoutRepository.sumCaloriesByUserId(user.getId()),
            "totalMinutes", workoutRepository.sumDurationByUserId(user.getId())
        );
    }
}