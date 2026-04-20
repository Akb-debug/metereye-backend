// CompteurRequestDTO.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotBlank;
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
public class CompteurRequestDTO {

    @NotBlank(message = "La référence est obligatoire")
    private String reference;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @NotBlank(message = "Le type de compteur est obligatoire")
    private String typeCompteur;  // CASH_POWER, CLASSIQUE

    @NotNull(message = "La valeur initiale est obligatoire")
    @Positive(message = "La valeur initiale doit être positive")
    private Double valeurInitiale;
}