// ✅ CRÉÉ — SousCompteurResponseDTO.java
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
public class SousCompteurResponseDTO {

    private Long id;
    private String reference;
    private String descriptionLogement;
    private Double valeurInitiale;
    private Double valeurActuelle;
    private Long maisonId;
    private String maisonNom;
    private Long locataireId;
    private String locataireNom;
    private String locataireEmail;
    private Boolean actif;
    private LocalDateTime dateCreation;
    private Double dernierReleve;
    private LocalDateTime dateDernierReleve;
}
