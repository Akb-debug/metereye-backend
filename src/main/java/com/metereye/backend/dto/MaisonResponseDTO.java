// ✅ CRÉÉ — MaisonResponseDTO.java
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
public class MaisonResponseDTO {

    private Long id;
    private String nom;
    private String adresse;
    private String description;
    private Long proprietaireId;
    private String proprietaireNom;
    private Long compteurPrincipalId;
    private String compteurPrincipalReference;
    private String typeCompteur;
    private Integer nombreLocataires;
    private Boolean actif;
    private LocalDateTime dateCreation;
}
