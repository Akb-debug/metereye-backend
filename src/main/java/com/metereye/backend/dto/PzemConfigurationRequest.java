// PzemConfigurationRequest.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO pour la configuration spécifique ESP32-PZEM004T
 */
@Data
public class PzemConfigurationRequest {
    
    @NotNull(message = "Le seuil d'alerte est obligatoire")
    @Positive(message = "Le seuil d'alerte doit être positif")
    private Double seuilAlerte;
    
    @NotNull(message = "Le facteur de correction est obligatoire")
    @Positive(message = "Le facteur de correction doit être positif")
    private Double facteurCorrection;
    
    @NotNull(message = "Le mode de calibrage est obligatoire")
    private String modeCalibrage;
    
    // Paramètres optionnels
    private Double tensionMax;
    private Double courantMax;
    private Double puissanceMax;
    private Integer frequenceEchantillonnage;
}
