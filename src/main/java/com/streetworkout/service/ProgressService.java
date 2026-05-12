package com.streetworkout.service;

import com.streetworkout.dto.ProgressRequest;
import com.streetworkout.dto.ProgressResponse;
import com.streetworkout.dto.ProgressStatsResponse;
import com.streetworkout.entity.Progress;
import com.streetworkout.entity.User;
import com.streetworkout.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private UserService userService;

    /**
     * Retourne l'historique de progression avec filtres optionnels.
     */
    @Transactional(readOnly = true)
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
     * Retourne les statistiques de progression pour le graphique d'evolution du poids.
     *
     * Calcule : poids actuel, min, max, variation totale, variation en %.
     * Les entrees sont triees par date ASC pour le graphique (axe X = temps).
     */
    @Transactional(readOnly = true)
    public ProgressStatsResponse getStats(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();

        List<Progress> progressList;
        if (startDate != null && endDate != null) {
            progressList = progressRepository.findByUserIdAndProgressDateBetweenOrderByProgressDateAsc(
                user.getId(), startDate, endDate
            );
        } else {
            progressList = progressRepository.findByUserIdOrderByProgressDateAsc(user.getId());
        }

        List<ProgressResponse> entries = progressList.stream()
            .map(ProgressResponse::fromEntity)
            .collect(Collectors.toList());

        if (entries.isEmpty()) {
            return new ProgressStatsResponse(entries, null, null, null, null, null, 0);
        }

        // Filtrer les entrees avec un poids renseigne
        List<BigDecimal> weights = progressList.stream()
            .map(Progress::getWeight)
            .filter(w -> w != null)
            .collect(Collectors.toList());

        if (weights.isEmpty()) {
            return new ProgressStatsResponse(entries, null, null, null, null, null, entries.size());
        }

        BigDecimal currentWeight = weights.get(weights.size() - 1);
        BigDecimal firstWeight   = weights.get(0);
        BigDecimal minWeight     = weights.stream().min(BigDecimal::compareTo).orElse(null);
        BigDecimal maxWeight     = weights.stream().max(BigDecimal::compareTo).orElse(null);

        // Variation absolue : poids actuel - premier poids
        BigDecimal weightChange = currentWeight.subtract(firstWeight).setScale(2, RoundingMode.HALF_UP);

        // Variation en % : (variation / premier poids) * 100
        BigDecimal weightChangePct = null;
        if (firstWeight.compareTo(BigDecimal.ZERO) != 0) {
            weightChangePct = weightChange
                .divide(firstWeight, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        }

        return new ProgressStatsResponse(
            entries,
            currentWeight,
            minWeight,
            maxWeight,
            weightChange,
            weightChangePct,
            entries.size()
        );
    }

    /**
     * Ajoute une entree de progression.
     */
    @Transactional
    public ProgressResponse create(ProgressRequest request) {
        User user = userService.getCurrentUser();

        if (progressRepository.findByUserIdAndProgressDate(user.getId(), request.getProgressDate()).isPresent()) {
            throw new RuntimeException("Une entree existe deja pour cette date");
        }

        Progress progress = new Progress();
        progress.setUser(user);
        progress.setProgressDate(request.getProgressDate());
        progress.setWeight(request.getWeight());
        progress.setNotes(request.getNotes());

        return ProgressResponse.fromEntity(progressRepository.save(progress));
    }

    /**
     * Met a jour une entree de progression.
     */
    @Transactional
    public ProgressResponse update(Long id, ProgressRequest request) {
        User user = userService.getCurrentUser();
        Progress progress = progressRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Entree non trouvee ou acces refuse"));

        progress.setProgressDate(request.getProgressDate());
        progress.setWeight(request.getWeight());
        progress.setNotes(request.getNotes());

        return ProgressResponse.fromEntity(progressRepository.save(progress));
    }

    /**
     * Supprime une entree de progression.
     */
    @Transactional
    public void delete(Long id) {
        User user = userService.getCurrentUser();
        Progress progress = progressRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Entree non trouvee ou acces refuse"));
        progressRepository.delete(progress);
    }
}
