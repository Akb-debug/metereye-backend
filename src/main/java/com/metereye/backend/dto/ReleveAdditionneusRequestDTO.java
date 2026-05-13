// ✅ CRÉÉ — ReleveAdditionneusRequestDTO.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleveAdditionneusRequestDTO {

    @NotNull
    private Long sousCompteurId;

    @NotNull
    private Double valeur;

    @Builder.Default
    private String source = "MANUEL";

    private String commentaire;

    private LocalDateTime dateReleve;
}
