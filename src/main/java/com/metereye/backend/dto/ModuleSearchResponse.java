// ModuleSearchResponse.java
package com.metereye.backend.dto;

import com.metereye.backend.enums.TypeModuleDevice;
import com.metereye.backend.enums.StatutModuleDevice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse de recherche de module
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleSearchResponse {
    
    private String deviceCode;
    private String bluetoothAddress;
    private TypeModuleDevice typeModule;
    private StatutModuleDevice statut;
    private Boolean configured;
    private String moduleName;
    private String firmwareVersion;
    private LocalDateTime lastSeenAt;
    private Long proprietaireId;
    private Long compteurId;
    private String compteurReference;
    private String modeLectureAssocie;
    private boolean exists; // Si le module existe dans le système
    private boolean belongsToUser; // Si le module appartient à l'utilisateur
    private boolean canConfigure; // Si le module peut être configuré
    
    // Méthodes utilitaires
    public boolean isAvailable() {
        return !configured && belongsToUser;
    }
    
    public String getStatusMessage() {
        if (!exists) {
            return "Module non trouvé dans le système";
        } else if (!belongsToUser) {
            return "Module déjà associé à un autre utilisateur";
        } else if (configured) {
            return "Module déjà configuré";
        } else {
            return "Module disponible pour configuration";
        }
    }
}
