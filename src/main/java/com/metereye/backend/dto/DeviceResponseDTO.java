// DeviceResponseDTO.java
package com.metereye.backend.dto;

import com.metereye.backend.enums.StatutModuleESP32;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponseDTO {

    private String deviceCode;
    
    private String qrCodeValue;
    
    private String serialNumber;
    
    private StatutModuleESP32 statut;
    
    private Boolean configured;
    
    private LocalDateTime lastSeenAt;
    
    private String firmwareVersion;
    
    private Integer captureInterval;
    
    private String ipAddress;
    
    private String wifiSsid;
    
    private Long proprietaireId;
    
    private Long compteurId;
    
    private String compteurReference;
    
    private String message;
}
