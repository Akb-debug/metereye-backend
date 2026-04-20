// Releve.java
package com.metereye.backend.entity;

import com.metereye.backend.enums.SourceReleve;
import com.metereye.backend.enums.StatutReleve;
import com.metereye.backend.enums.TypeCompteur;
import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "releves")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Releve extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compteur_id", nullable = false)
    private Compteur compteur;

    @Column(nullable = false)
    private Double valeur;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "consommation_calculee")
    private Double consommationCalculee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceReleve source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReleve statut;

    @Column(length = 500)
    private String commentaire;

    @OneToOne(mappedBy = "releve", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Image image;

    // Méthodes de validation
    public boolean estValidePourCreation() {
        // Vérifier si le compteur est configuré pour cette source
        if (compteur == null || !compteur.peutAccepterLecture(source)) {
            return false;
        }

        // Vérifier si la valeur est valide selon le type de compteur
        if (compteur != null && !compteur.estValidePourReleve(valeur)) {
            return false;
        }

        // Vérifier si le compteur est actif
        if (compteur != null && !compteur.getActif()) {
            return false;
        }

        return true;
    }

    public String getMessageErreurValidation() {
        if (compteur == null) {
            return "Compteur non spécifié";
        }

        if (!compteur.getActif()) {
            return "Le compteur n'est pas actif";
        }

        if (!compteur.estConfigurePourLecture()) {
            return "Le compteur n'est pas configuré pour la lecture";
        }

        if (!compteur.peutAccepterLecture(source)) {
            return String.format("Le compteur est configuré pour %s mais la source est %s", 
                    compteur.getModeLectureConfigure(), source);
        }

        if (!compteur.estValidePourReleve(valeur)) {
            if (compteur.getTypeCompteur() == TypeCompteur.CLASSIQUE) {
                return "La valeur du compteur classique ne peut pas être inférieure à la précédente";
            }
        }

        return null;
    }

    public void calculerConsommation() {
        if (compteur != null && compteur.getTypeCompteur() == TypeCompteur.CLASSIQUE) {
            this.consommationCalculee = compteur.calculerConsommation();
        } else {
            this.consommationCalculee = 0.0;
        }
    }
}