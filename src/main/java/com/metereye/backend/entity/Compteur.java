// Compteur.java
package com.metereye.backend.entity;

import com.metereye.backend.enums.*;
import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "compteurs")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Compteur extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String reference;

    @Column(nullable = false)
    private String adresse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCompteur typeCompteur;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    @Builder.Default
    private StatutCompteur statut = StatutCompteur.EN_ATTENTE_CONFIGURATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_lecture")
    private ModeLectureCompteur modeLectureConfigure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private User proprietaire;

    @Column(name = "date_initialisation")
    private LocalDate dateInitialisation;

    @Column(name = "date_finalisation")
    private LocalDate dateFinalisation;

    @Column(name = "actif")
    @Builder.Default
    private Boolean actif = true;

    @OneToMany(mappedBy = "compteur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Releve> releves = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "module_esp32_id")
    private ModuleESP32 moduleESP32;

    // Attributs pour Compteur CLASSIQUE
    @Column(name = "index_actuel")
    private Double indexActuel;

    @Column(name = "index_precedent")
    private Double indexPrecedent;

    @Column(name = "index_initial")
    private Double indexInitial;

    @Column(name = "date_derniere_reinitialisation")
    private LocalDateTime dateDerniereReinitialisation;

    @Column(name = "motif_reinitialisation")
    private String motifReinitialisation;

    @Column(name = "taux_consommation_mensuel")
    private Double tauxConsommationMensuel;

    // Attributs pour Compteur CASH_POWER
    @Column(name = "credit_actuel")
    private Double creditActuel;

    @Column(name = "credit_initial")
    private Double creditInitial;

    @Column(name = "date_derniere_recharge")
    private LocalDateTime dateDerniereRecharge;

    @Column(name = "taux_consommation_journalier")
    private Double tauxConsommationJournalier;

    @Column(name = "dernier_code_recharge")
    private String dernierCodeRecharge;

    // Méthodes utilitaires selon le type
    public Double getValeurActuelle() {
        return switch (typeCompteur) {
            case CLASSIQUE -> indexActuel != null ? indexActuel : 0.0;
            case CASH_POWER -> creditActuel != null ? creditActuel : 0.0;
            default -> 0.0;
        };
    }

    public void mettreAJourValeur(Double nouvelleValeur) {
        switch (typeCompteur) {
            case CLASSIQUE -> {
                indexPrecedent = indexActuel;
                indexActuel = nouvelleValeur;
            }
            case CASH_POWER -> creditActuel = nouvelleValeur;
        }
    }

    public Double calculerConsommation() {
        if (typeCompteur == TypeCompteur.CLASSIQUE && indexPrecedent != null && indexActuel != null) {
            return indexActuel - indexPrecedent;
        }
        return 0.0;
    }

    public void recharger(Double montant, String codeRecharge) {
        if (typeCompteur == TypeCompteur.CASH_POWER && montant != null && montant > 0) {
            creditActuel += montant;
            dateDerniereRecharge = LocalDateTime.now();
            dernierCodeRecharge = codeRecharge;
        }
    }

    public boolean estConfigurePourLecture() {
        return modeLectureConfigure != null;
    }

    public boolean peutAccepterLecture(SourceReleve source) {
        if (!estConfigurePourLecture()) {
            return false;
        }
        
        return switch (modeLectureConfigure) {
            case MANUAL -> source == SourceReleve.MANUEL;
            case ESP32_CAM -> source == SourceReleve.ESP32_CAM;
            case SENSOR -> source == SourceReleve.SENSOR;
        };
    }

    public void reinitialiser(String motif) {
        if (typeCompteur == TypeCompteur.CLASSIQUE) {
            indexPrecedent = indexActuel;
            indexInitial = indexActuel;
            dateDerniereReinitialisation = LocalDateTime.now();
            motifReinitialisation = motif;
            statut = StatutCompteur.ACTIF;
        }
    }

    public boolean estValidePourReleve(Double nouvelleValeur) {
        return switch (typeCompteur) {
            case CLASSIQUE -> {
                // Valeur doit être >= précédente sauf si réinitialisation récente
                if (dateDerniereReinitialisation != null && 
                    dateDerniereReinitialisation.isAfter(LocalDateTime.now().minusDays(1))) {
                    yield true; // Réinitialisation récente autorise n'importe quelle valeur
                } else if (indexActuel != null) {
                    yield nouvelleValeur >= indexActuel;
                } else {
                    yield true; // Première lecture
                }
            }
            case CASH_POWER -> true; // Cash Power peut diminuer ou augmenter
            default -> true; // Autres types acceptés par défaut
        };
    }
}