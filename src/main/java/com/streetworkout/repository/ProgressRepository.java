package com.streetworkout.repository;

import com.streetworkout.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    // Tout l'historique de progression d'un utilisateur, trié par date DESC
    List<Progress> findByUserIdOrderByProgressDateDesc(Long userId);

    // Tout l'historique trié par date ASC (pour le graphique — axe X = temps)
    List<Progress> findByUserIdOrderByProgressDateAsc(Long userId);

    // Progression entre deux dates
    List<Progress> findByUserIdAndProgressDateBetweenOrderByProgressDateAsc(
        Long userId, LocalDate startDate, LocalDate endDate
    );

    // Trouver une entrée spécifique par utilisateur et date
    Optional<Progress> findByUserIdAndProgressDate(Long userId, LocalDate progressDate);

    // Vérifier qu'une entrée appartient à un utilisateur
    Optional<Progress> findByIdAndUserId(Long id, Long userId);
}
