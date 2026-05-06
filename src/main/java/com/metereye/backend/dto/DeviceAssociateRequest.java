// DeviceAssociateRequest.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO pour la requête d'association module-compteur
 */
@Data
public class DeviceAssociateRequest {
    
    @NotNull(message = "L'ID du compteur est obligatoire")
    private Long compteurId;
    
    @NotNull(message = "L'intervalle de capture est obligatoire")
    @Positive(message = "L'intervalle doit être positif")
    private Integer captureInterval;
}
