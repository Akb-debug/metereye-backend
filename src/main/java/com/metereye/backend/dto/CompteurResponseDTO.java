// CompteurResponseDTO.java
package com.metereye.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompteurResponseDTO {
    private Long id;
    private String reference;
    private String adresse;
    private String typeCompteur;
    private Double valeurActuelle;
    private String proprietaireNom;
    private Long proprietaireId;
    private LocalDate dateInitialisation;
    private Boolean actif;
    private LocalDateTime dateCreation;
}