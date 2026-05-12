package com.streetworkout.service;

import com.streetworkout.dto.PageResponse;
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

    /**
     * Retourne les seances de l utilisateur avec pagination.
     * Strategie "IDs first" : recupere les IDs pagines, puis charge les entites completes.
     * Evite le probleme HHH90003004 (JOIN FETCH incompatible avec LIMIT en JPQL).
     */
    @Transactional(readOnly = true)
    public PageResponse<WorkoutResponse> getMyWorkouts(int page, int size,
                                                        LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();
        int offset = page * size;

        List<Long> ids;
        long total;

        if (startDate != null && endDate != null) {
            ids   = workoutRepository.findIdsByUserIdAndDateRangePaged(user.getId(), startDate, endDate, size, offset);
            total = workoutRepository.countByUserIdAndDateRange(user.getId(), startDate, endDate);
        } else {
            ids   = workoutRepository.findIdsByUserIdPaged(user.getId(), size, offset);
            total = workoutRepository.countByUserId(user.getId());
        }

        List<WorkoutResponse> content = ids.isEmpty()
            ? List.of()
            : workoutRepository.findByIdsWithExercises(ids).stream()
                .map(WorkoutResponse::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.of(content, page, size, total);
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