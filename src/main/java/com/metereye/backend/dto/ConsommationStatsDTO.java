// ConsommationStatsDTO.java
package com.metereye.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsommationStatsDTO {
    private Double consommationJour;
    private Double consommationSemaine;
    private Double consommationMois;
    private Double consommationMoyenneJour;
    private Double creditRestant;
    private LocalDateTime dateEstimationEpuisement;
    private Map<String, Double> consommationParJour;
}