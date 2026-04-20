// ReinitialisationCompteurDTO.java
package com.metereye.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReinitialisationCompteurDTO {

    @NotBlank(message = "Le motif de réinitialisation est obligatoire")
    private String motif;

    private String commentaire;
}
