package com.streetworkout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO pour les statistiques de progression — utilisé pour le graphique d'évolution du poids.
 *
 * Contient :
 * - La liste des entrées (points du graphique)
 * - Les statistiques globales (min, max, actuel, variation)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressStatsResponse {

    private List<ProgressResponse> entries;

    // Statistiques pour le graphique
    private BigDecimal currentWeight;
    private BigDecimal minWeight;
    private BigDecimal maxWeight;
    private BigDecimal weightChange;      // variation depuis la premiere entree
    private BigDecimal weightChangePct;   // variation en pourcentage
    private int totalEntries;
}
