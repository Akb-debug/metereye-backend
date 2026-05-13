// ✅ CRÉÉ — SousCompteurRequestDTO.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SousCompteurRequestDTO {

    @NotBlank
    private String reference;

    private String descriptionLogement;

    @NotNull
    private Double valeurInitiale;

    @NotNull
    private Long maisonId;
}
