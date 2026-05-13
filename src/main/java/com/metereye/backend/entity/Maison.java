// ✅ CRÉÉ — Maison.java
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
@Table(name = "maisons")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Maison extends BaseEntity {

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String adresse;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private User proprietaire;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compteur_principal_id")
    private Compteur compteurPrincipal;

    @OneToMany(mappedBy = "maison", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SousCompteur> sousCompteurs = new ArrayList<>();

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private Boolean actif = true;
}
