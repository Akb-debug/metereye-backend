// StatutModuleESP32.java
package com.metereye.backend.enums;

public enum StatutModuleESP32 {
    NON_CONFIGURE,      // Nouveau module, QR code scanné seulement
    EN_CONFIGURATION,  // Wi-Fi configuré, en attente de connexion
    ACTIF,             // Connecté et fonctionnel
    HORS_LIGNE,        // Perte de connexion
    ERREUR,            // Erreur de configuration
    MAINTENANCE        // Mode maintenance
}
