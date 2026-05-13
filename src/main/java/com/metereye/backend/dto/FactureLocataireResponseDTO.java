// ✅ CRÉÉ — FactureLocataireResponseDTO.java
package com.metereye.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureLocataireResponseDTO {

    private Long id;
    private Integer mois;
    private Integer annee;
    private String nomMois;
    private Long sousCompteurId;
    private String sousCompteurReference;
    private Long locataireId;
    private String locataireNom;
    private String locataireEmail;
    private Double consommationKwh;
    private Double consommationTotaleMaison;
    private Double partPourcentage;
    private Double montantFacturePrincipale;
    private Double montantFcfa;
    private String statut;
    private LocalDateTime dateGeneration;
    private boolean pdfDisponible;
}
