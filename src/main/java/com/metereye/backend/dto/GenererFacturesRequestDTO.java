// ✅ CRÉÉ — GenererFacturesRequestDTO.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenererFacturesRequestDTO {

    @NotNull
    private Long maisonId;

    @NotNull
    private Integer mois;

    @NotNull
    private Integer annee;

    @NotNull
    private Double montantFacturePrincipale;
}
