// ✅ CRÉÉ — SousCompteur.java
package com.metereye.backend.entity;

import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sous_compteurs")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SousCompteur extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(name = "description_logement")
    private String descriptionLogement;

    @Column(name = "valeur_initiale", nullable = false)
    @Builder.Default
    private Double valeurInitiale = 0.0;

    @Column(name = "valeur_actuelle")
    @Builder.Default
    private Double valeurActuelle = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maison_id", nullable = false)
    private Maison maison;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locataire_id")
    private User locataire;

    @OneToMany(mappedBy = "sousCompteur", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ReleveAdditionneuse> releves = new ArrayList<>();

    @OneToMany(mappedBy = "sousCompteur", cascade = CascadeType.ALL)
    @Builder.Default
    private List<FactureLocataire> factures = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;
}
