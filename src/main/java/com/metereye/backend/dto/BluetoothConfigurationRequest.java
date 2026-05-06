// BluetoothConfigurationRequest.java
package com.metereye.backend.dto;

import com.metereye.backend.enums.TypeModuleDevice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO pour la configuration d'un module via Bluetooth
 */
@Data
public class BluetoothConfigurationRequest {
    
    @NotBlank(message = "L'adresse Bluetooth est obligatoire")
    private String bluetoothAddress;
    
    @NotNull(message = "L'ID du compteur est obligatoire")
    private Long compteurId;
    
    @NotNull(message = "L'intervalle de capture est obligatoire")
    @Positive(message = "L'intervalle doit être positif")
    private Integer captureInterval;
    
    // Configuration WiFi
    @NotBlank(message = "Le SSID WiFi est obligatoire")
    private String wifiSsid;
    
    @NotBlank(message = "Le mot de passe WiFi est obligatoire")
    private String wifiPassword;
    
    // Configuration spécifique selon type
    private String nomModule;
    private String localisation;
    
    // Configuration ESP32-CAM spécifique
    private String resolutionCamera;
    private Boolean flashActive;
    private Integer qualiteImage;
    private Integer angleCapture;
    
    // Configuration ESP32-PZEM004T spécifique
    private Double seuilAlerte;
    private Double facteurCorrection;
    private String modeCalibrage;
    private Double tensionMax;
    private Double courantMax;
    private Double puissanceMax;
}
