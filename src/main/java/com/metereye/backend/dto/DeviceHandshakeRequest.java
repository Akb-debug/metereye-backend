// DeviceHandshakeRequest.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO pour la requête de handshake de module device
 */
@Data
public class DeviceHandshakeRequest {
    
    @NotBlank(message = "La version du firmware est obligatoire")
    private String firmwareVersion;
    
    @NotBlank(message = "L'adresse IP est obligatoire")
    private String ipAddress;
    
    @NotBlank(message = "Le SSID WiFi est obligatoire")
    private String wifiSsid;
}
