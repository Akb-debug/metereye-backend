// ESP32PzemDevice.java
package com.metereye.backend.entity;

import com.metereye.backend.enums.StatutModuleDevice;
import com.metereye.backend.enums.TypeModuleDevice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Spécialisation pour les modules ESP32-PZEM004T
 * Mode métier: SENSOR
 * Solution technique: ESP32 + PZEM004T (capteur énergie)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "esp32_pzem_devices")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ESP32PzemDevice extends ModuleDevice {

    // Caractéristiques PZEM004T
    @Column(name = "tension_max")
    @Builder.Default
    private Double tensionMax = 500.0; // Volts

    @Column(name = "courant_max")
    @Builder.Default
    private Double courantMax = 100.0; // Ampères

    @Column(name = "puissance_max")
    @Builder.Default
    private Double puissanceMax = 22000.0; // Watts

    @Column(name = "precision")
    @Builder.Default
    private Double precision = 1.0; // Pourcentage

    @Column(name = "frequence_echantillonnage")
    @Builder.Default
    private Integer frequenceEchantillonnage = 1000; // Hz

    // Configuration spécifique
    @Column(name = "mode_calibrage")
    @Builder.Default
    private String modeCalibrage = "AUTO";

    @Column(name = "facteur_correction")
    @Builder.Default
    private Double facteurCorrection = 1.0;

    @Column(name = "seuil_alerte")
    @Builder.Default
    private Double seuilAlerte = 0.1; // 10% de variation

    @Override
    public boolean peutEnvoyerReleves() {
        return estConfigureEtActif() && compteur != null && 
               compteur.getModeLectureConfigure() != null &&
               compteur.getModeLectureConfigure().name().equals("SENSOR");
    }

    @Override
    public String getModeLectureAssocie() {
        return "SENSOR";
    }

    // Méthodes spécifiques ESP32-PZEM004T
    public void configurerCapteur(Double seuilAlerte, Double facteurCorrection, String modeCalibrage) {
        this.seuilAlerte = seuilAlerte;
        this.facteurCorrection = facteurCorrection;
        this.modeCalibrage = modeCalibrage;
    }

    public boolean estCalibreCorrectement() {
        return facteurCorrection > 0.8 && facteurCorrection < 1.2 && 
               modeCalibrage != null && !modeCalibrage.isEmpty();
    }

    public Double calculerPuissanceApparente(Double tension, Double courant) {
        return tension * courant * facteurCorrection;
    }

    public boolean estDansLimites(Double tension, Double courant, Double puissance) {
        return tension <= tensionMax && 
               courant <= courantMax && 
               puissance <= puissanceMax;
    }
}
