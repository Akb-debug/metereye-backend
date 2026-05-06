// ModuleDevice.java
package com.metereye.backend.entity;

import com.metereye.backend.enums.StatutModuleDevice;
import com.metereye.backend.enums.TypeModuleDevice;
import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entité générique pour tous les types de modules devices
 * Base pour ESP32-CAM, ESP32-PZEM004T, etc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "module_devices")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ModuleDevice extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String deviceCode;

    @Column(unique = true, nullable = false)
    private String bluetoothAddress;

    @Column
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeModuleDevice typeModule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutModuleDevice statut = StatutModuleDevice.NON_CONFIGURE;

    @Column(name = "configured")
    @Builder.Default
    private Boolean configured = false;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "capture_interval")
    @Builder.Default
    protected Integer captureInterval = 3600;

    @Column(name = "wifi_ssid")
    protected String wifiSsid;

    @Column(name = "ip_address")
    protected String ipAddress;

    // Relations
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compteur_id")
    protected Compteur compteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietaire_id")
    private User proprietaire;

    // Méthodes utilitaires communes
    public boolean estEnLigne() {
        return lastSeenAt != null && 
               lastSeenAt.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    public boolean estConfigureEtActif() {
        return configured && statut == StatutModuleDevice.ACTIF;
    }

    public boolean estConnecte() {
        return statut == StatutModuleDevice.ACTIF &&
                lastSeenAt != null &&
                lastSeenAt.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    public abstract boolean peutEnvoyerReleves();

    public abstract String getModeLectureAssocie();
}
