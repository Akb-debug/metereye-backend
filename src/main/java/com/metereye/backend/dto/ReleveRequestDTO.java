// ReleveRequestDTO.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleveRequestDTO {

    @NotNull(message = "L'ID du compteur est obligatoire")
    private Long compteurId;

    @NotNull(message = "La valeur est obligatoire")
    @Positive(message = "La valeur doit être positive")
    private Double valeur;

    private String commentaire;
}