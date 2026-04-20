// SensorReadingRequest.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SensorReadingRequest {
    
    @NotNull(message = "L'ID du compteur est obligatoire")
    private Long meterId;
    
    @NotNull(message = "La valeur est obligatoire")
    @Positive(message = "La valeur doit être positive")
    private Double value;
    
    @NotBlank(message = "L'ID du capteur est obligatoire")
    private String sensorId;
}
