// BluetoothDirectRequest.java
package com.metereye.backend.dto;

import com.metereye.backend.enums.TypeModuleDevice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO pour la configuration directe d'un module via adresse Bluetooth
 * Utilisé quand l'utilisateur entre manuellement l'adresse Bluetooth
 */
@Data
public class BluetoothDirectRequest {
    
    @NotBlank(message = "L'adresse Bluetooth est obligatoire")
    private String bluetoothAddress;
    
    @NotNull(message = "Le type de module est obligatoire")
    private TypeModuleDevice typeModule;
    
    @NotBlank(message = "Le nom du module est obligatoire")
    private String moduleName;
    
    @NotBlank(message = "La version du firmware est obligatoire")
    private String firmwareVersion;
    
    @NotNull(message = "L'ID utilisateur est obligatoire")
    private Long userId;
    
    // Informations optionnelles
    private String serialNumber;
    private String localisation;
    
    // Configuration WiFi
    @NotBlank(message = "Le SSID WiFi est obligatoire")
    private String wifiSsid;
    
    @NotBlank(message = "Le mot de passe WiFi est obligatoire")
    private String wifiPassword;
    
    // Configuration compteur
    @NotNull(message = "L'ID du compteur est obligatoire")
    private Long compteurId;
    
    @NotNull(message = "L'intervalle de capture est obligatoire")
    private Integer captureInterval;
    
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
