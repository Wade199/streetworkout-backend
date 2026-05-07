package com.streetworkout.service;

import com.streetworkout.dto.ProgressRequest;
import com.streetworkout.dto.ProgressResponse;
import com.streetworkout.entity.Progress;
import com.streetworkout.entity.User;
import com.streetworkout.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service gérant le suivi de progression (poids, notes).
 *
 * Contrainte : une seule entrée par utilisateur par date (UNIQUE constraint en base).
 */
@Service
public class ProgressService {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private UserService userService;

    /**
     * Retourne tout l'historique de progression de l'utilisateur connecté.
     */
    public List<ProgressResponse> getMyProgress(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();

        List<Progress> progressList;
        if (startDate != null && endDate != null) {
            progressList = progressRepository.findByUserIdAndProgressDateBetweenOrderByProgressDateAsc(
                user.getId(), startDate, endDate
            );
        } else {
            progressList = progressRepository.findByUserIdOrderByProgressDateDesc(user.getId());
        }

        return progressList.stream()
            .map(ProgressResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Ajoute une entrée de progression.
     * Vérifie qu'il n'existe pas déjà une entrée pour cette date.
     */
    @Transactional
    public ProgressResponse create(ProgressRequest request) {
        User user = userService.getCurrentUser();

        // Vérifier l'unicité (user + date)
        if (progressRepository.findByUserIdAndProgressDate(user.getId(), request.getProgressDate()).isPresent()) {
            throw new RuntimeException("Une entrée existe déjà pour cette date");
        }

        Progress progress = new Progress();
        progress.setUser(user);
        progress.setProgressDate(request.getProgressDate());
        progress.setWeight(request.getWeight());
        progress.setNotes(request.getNotes());

        return ProgressResponse.fromEntity(progressRepository.save(progress));
    }

    /**
     * Met à jour une entrée de progression existante.
     */
    @Transactional
    public ProgressResponse update(Long id, ProgressRequest request) {
        User user = userService.getCurrentUser();
        Progress progress = progressRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Entrée non trouvée ou accès refusé"));

        progress.setProgressDate(request.getProgressDate());
        progress.setWeight(request.getWeight());
        progress.setNotes(request.getNotes());

        return ProgressResponse.fromEntity(progressRepository.save(progress));
    }

    /**
     * Supprime une entrée de progression.
     */
    @Transactional
    public void delete(Long id) {
        User user = userService.getCurrentUser();
        Progress progress = progressRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Entrée non trouvée ou accès refusé"));
        progressRepository.delete(progress);
    }
}
