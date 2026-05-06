// HistoriqueConfigurationMode.java
package com.metereye.backend.entity;

import com.metereye.backend.enums.ModeLectureCompteur;
import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Historique des changements de mode de lecture des compteurs
 * Permet de tracer les configurations précédentes
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "historique_configuration_mode")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueConfigurationMode extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compteur_id", nullable = false)
    private Compteur compteur;

    @Enumerated(EnumType.STRING)
    @Column(name = "ancien_mode", nullable = false)
    private ModeLectureCompteur ancienMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "nouveau_mode", nullable = false)
    private ModeLectureCompteur nouveauMode;

    @Column(name = "date_changement", nullable = false)
    private LocalDateTime dateChangement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_par_user_id")
    private User changeParUser;

    @Column(name = "motif_changement", length = 500)
    private String motifChangement;

    @Column(name = "ancien_device_code")
    private String ancienDeviceCode;

    @Column(name = "nouveau_device_code")
    private String nouveauDeviceCode;

    @Column(name = "configuration_desactivee")
    @Builder.Default
    private Boolean configurationDesactivee = false;

    @Column(name = "donnees_migrees")
    @Builder.Default
    private Boolean donneesMigrees = false;

    // Méthodes utilitaires
    public boolean estChangementMode() {
        return ancienMode != nouveauMode;
    }

    public boolean estChangementDevice() {
        return !java.util.Objects.equals(ancienDeviceCode, nouveauDeviceCode);
    }

    public boolean estMigrationComplete() {
        return configurationDesactivee && donneesMigrees;
    }
}
