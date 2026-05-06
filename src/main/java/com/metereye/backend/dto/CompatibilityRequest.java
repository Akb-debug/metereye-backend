// CompatibilityRequest.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO pour la requête de vérification de compatibilité
 */
@Data
public class CompatibilityRequest {
    
    @NotBlank(message = "L'adresse Bluetooth est obligatoire")
    private String bluetoothAddress;
    
    @NotNull(message = "L'ID du compteur est obligatoire")
    private Long compteurId;
}
