// DeviceScanRequest.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO pour la requête de scan de module device
 */
@Data
public class DeviceScanRequest {
    
    @NotBlank(message = "Le QR code est obligatoire")
    private String qrCode;
    
    @NotNull(message = "L'ID utilisateur est obligatoire")
    private Long userId;
}
