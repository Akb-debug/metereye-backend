// StatutModuleDevice.java
package com.metereye.backend.enums;

/**
 * Statuts possibles pour les modules devices
 */
public enum StatutModuleDevice {
    NON_CONFIGURE("Non configuré", "Module scanné mais non configuré"),
    EN_CONFIGURATION("En configuration", "Module en cours de configuration"),
    ACTIF("Actif", "Module opérationnel et connecté"),
    HORS_LIGNE("Hors ligne", "Perte de connexion"),
    ERREUR("Erreur", "Erreur de configuration ou fonctionnement"),
    MAINTENANCE("Maintenance", "Module en mode maintenance"),
    DESACTIVE("Désactivé", "Module désactivé suite à changement de mode");

    private final String libelle;
    private final String description;

    StatutModuleDevice(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }

    public boolean estOperationnel() {
        return this == ACTIF;
    }

    public boolean peutEtreConfigure() {
        return this == NON_CONFIGURE || this == ERREUR;
    }

    public boolean estConnecte() {
        return this == ACTIF || this == EN_CONFIGURATION;
    }
}
