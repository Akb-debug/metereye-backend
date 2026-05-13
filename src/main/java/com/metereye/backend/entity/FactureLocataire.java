// ✅ CRÉÉ — FactureLocataire.java
package com.metereye.backend.entity;

import com.metereye.backend.enums.StatutFacture;
import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "factures_locataires")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FactureLocataire extends BaseEntity {

    @Column(nullable = false)
    private Integer mois;

    @Column(nullable = false)
    private Integer annee;

    @Column(name = "consommation_kwh", nullable = false)
    private Double consommationKwh;

    @Column(name = "consommation_totale_maison")
    private Double consommationTotaleMaison;

    @Column(name = "part_pourcentage")
    private Double partPourcentage;

    @Column(name = "montant_facture_principale")
    private Double montantFacturePrincipale;

    @Column(name = "montant_fcfa", nullable = false)
    private Double montantFcfa;

    @Column(name = "pdf_path")
    private String pdfPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutFacture statut = StatutFacture.EN_ATTENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sous_compteur_id", nullable = false)
    private SousCompteur sousCompteur;

    @Column(name = "date_generation")
    private LocalDateTime dateGeneration;
}
