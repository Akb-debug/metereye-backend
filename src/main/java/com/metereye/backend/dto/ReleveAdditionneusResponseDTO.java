// ✅ CRÉÉ — ReleveAdditionneusResponseDTO.java
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
public class ReleveAdditionneusResponseDTO {

    private Long id;
    private Long sousCompteurId;
    private String sousCompteurReference;
    private Double valeur;
    private Double consommationCalculee;
    private String source;
    private LocalDateTime dateReleve;
    private String commentaire;
}
