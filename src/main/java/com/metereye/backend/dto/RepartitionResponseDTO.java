// ✅ CRÉÉ — RepartitionResponseDTO.java
package com.metereye.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepartitionResponseDTO {

    private Long maisonId;
    private String maisonNom;
    private Integer mois;
    private Integer annee;
    private Double consommationTotale;
    private Double montantTotal;
    private List<FactureLocataireResponseDTO> factures;
    private String statut;
}
