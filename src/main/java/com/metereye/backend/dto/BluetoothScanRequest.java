// BluetoothScanRequest.java
package com.metereye.backend.dto;

import com.metereye.backend.enums.TypeModuleDevice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO pour la requête de scan et configuration via Bluetooth
 */
@Data
public class BluetoothScanRequest {
    
    @NotBlank(message = "L'adresse Bluetooth est obligatoire")
    private String bluetoothAddress;
    
    @NotNull(message = "Le type de module est obligatoire")
    private TypeModuleDevice typeModule;
    
    @NotBlank(message = "Le nom du module est obligatoire")
    private String moduleName;
    
    @NotBlank(message = "Le firmware version est obligatoire")
    private String firmwareVersion;
    
    @NotNull(message = "L'ID utilisateur est obligatoire")
    private Long userId;
    
    // Informations optionnelles pour la configuration
    private String serialNumber;
    private Double signalStrength; // RSSI
    private String manufacturerData;
}
