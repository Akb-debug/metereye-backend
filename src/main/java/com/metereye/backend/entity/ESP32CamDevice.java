// ESP32CamDevice.java
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
 * Spécialisation pour les modules ESP32-CAM
 * Mode métier: ESP32_CAM
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "esp32_cam_devices")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ESP32CamDevice extends ModuleDevice {

    @Column(name = "resolution_camera")
    @Builder.Default
    private String resolutionCamera = "2MP";

    @Column(name = "flash_active")
    @Builder.Default
    private Boolean flashActive = true;

    @Column(name = "qualite_image")
    @Builder.Default
    private Integer qualiteImage = 80; // 0-100

    @Column(name = "angle_capture")
    @Builder.Default
    private Integer angleCapture = 90; // degrés

    @Column(name = "format_image")
    @Builder.Default
    private String formatImage = "JPEG";

    @Override
    public boolean peutEnvoyerReleves() {
        return estConfigureEtActif() && compteur != null && 
               compteur.getModeLectureConfigure() != null &&
               compteur.getModeLectureConfigure().name().equals("ESP32_CAM");
    }

    @Override
    public String getModeLectureAssocie() {
        return "ESP32_CAM";
    }

    // Méthodes spécifiques ESP32-CAM
    public void configurerCapture(Integer intervalle, Integer qualite, Boolean flash) {
        this.captureInterval = intervalle;
        this.qualiteImage = qualite;
        this.flashActive = flash;
    }

    public boolean estOptimisePourCapture() {
        return qualiteImage >= 70 && flashActive && captureInterval <= 7200; // 2h max
    }
}
