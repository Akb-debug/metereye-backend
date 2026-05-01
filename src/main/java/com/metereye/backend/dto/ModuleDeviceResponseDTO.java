// ModuleDeviceResponseDTO.java
package com.metereye.backend.dto;

import com.metereye.backend.enums.StatutModuleDevice;
import com.metereye.backend.enums.TypeModuleDevice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse des informations de module device
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDeviceResponseDTO {
    
    private String deviceCode;
    private String bluetoothAddress;
    private TypeModuleDevice typeModule;
    private StatutModuleDevice statut;
    private Boolean configured;
    private LocalDateTime lastSeenAt;
    private String firmwareVersion;
    private Integer captureInterval;
    private String wifiSsid;
    private String ipAddress;
    private Long proprietaireId;
    private Long compteurId;
    private String compteurReference;
    private String modeLectureAssocie;
    
    // Champs spécifiques ESP32-CAM
    private String resolutionCamera;
    private Boolean flashActive;
    private Integer qualiteImage;
    private Integer angleCapture;
    private String formatImage;
    
    // Champs spécifiques ESP32-PZEM004T
    private Double tensionMax;
    private Double courantMax;
    private Double puissanceMax;
    private Double precision;
    private String modeCalibrage;
    private Double facteurCorrection;
    private Double seuilAlerte;
    
    // Méthodes utilitaires
    public boolean estEnLigne() {
        return lastSeenAt != null && 
               lastSeenAt.isAfter(LocalDateTime.now().minusMinutes(5));
    }
    
    public boolean estOperationnel() {
        return configured && statut == StatutModuleDevice.ACTIF;
    }
    
    public String getLibelleStatut() {
        return statut != null ? statut.getLibelle() : "Inconnu";
    }
    
    public String getDescriptionType() {
        return typeModule != null ? typeModule.getDescription() : "Inconnu";
    }
}
