// ManualReadingRequest.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ManualReadingRequest {
    
    @NotNull(message = "L'ID du compteur est obligatoire")
    private Long meterId;
    
    @NotNull(message = "La valeur est obligatoire")
    @Positive(message = "La valeur doit être positive")
    private Double value;
    
    private String comment;
}
