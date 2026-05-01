// ChangementModeRequest.java
package com.metereye.backend.dto;

import com.metereye.backend.enums.ModeLectureCompteur;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO pour la requête de changement de mode de lecture
 */
@Data
public class ChangementModeRequest {
    
    @NotNull(message = "Le nouveau mode est obligatoire")
    private ModeLectureCompteur nouveauMode;
    
    @NotBlank(message = "Le motif du changement est obligatoire")
    private String motif;
}
