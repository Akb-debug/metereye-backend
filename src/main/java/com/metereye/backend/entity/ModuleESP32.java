// ModuleESP32.java
package com.metereye.backend.entity;

import com.metereye.backend.enums.StatutModuleESP32;
import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "modules_esp32")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleESP32 extends BaseEntity {

    // Champs pour l'onboarding
    @Column(unique = true, nullable = false)
    private String deviceCode;

    @Column(unique = true, nullable = false)
    private String qrCodeValue;

    @Column
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutModuleESP32 statut = StatutModuleESP32.NON_CONFIGURE;

    @Column(name = "configured")
    @Builder.Default
    private Boolean configured = false;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "capture_interval")
    @Builder.Default
    private Integer captureInterval = 3600;

    @Column(name = "wifi_ssid")
    private String wifiSsid;

    @Column(name = "ip_address")
    private String ipAddress;

    // Relations
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compteur_id")
    private Compteur compteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietaire_id")
    private User proprietaire;

    // Anciens champs (compatibilité)
    @Column(name = "address_mac", unique = true)
    private String addressMAC;

    @Column(name = "address_ip")
    private String addressIP;

    @Column(name = "ssid_wifi")
    private String ssidWiFi;

    @Column(name = "url_server")
    private String urlServer;

    @Column(name = "intervalle_capture_secondes")
    private Integer intervalleCaptureSecondes;

    @Column(name = "etat_signal_led")
    @Builder.Default
    private Boolean etatSignalLED = false;

    @Column(name = "dernier_ping")
    private LocalDateTime dernierPing;

    // Méthodes utilitaires
    public boolean estEnLigne() {
        return lastSeenAt != null && 
               lastSeenAt.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    public boolean estConfigureEtActif() {
        return configured && statut == StatutModuleESP32.ACTIF;
    }

    public boolean estConnecte() {
        return statut == StatutModuleESP32.ACTIF &&
                (dernierPing != null || lastSeenAt != null) &&
                (dernierPing != null ? dernierPing : lastSeenAt).isAfter(LocalDateTime.now().minusMinutes(5));
    }
}