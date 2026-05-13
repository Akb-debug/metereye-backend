// ✅ CRÉÉ — ReleveAdditionneuse.java
package com.metereye.backend.entity;

import com.metereye.backend.enums.SourceReleve;
import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "releves_additionneuses")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReleveAdditionneuse extends BaseEntity {

    @Column(nullable = false)
    private Double valeur;

    @Column(name = "consommation_calculee")
    private Double consommationCalculee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceReleve source;

    @Column(name = "date_releve", nullable = false)
    private LocalDateTime dateReleve;

    @Column(name = "commentaire")
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sous_compteur_id", nullable = false)
    private SousCompteur sousCompteur;

    @PrePersist
    protected void prePersist() {
        if (dateReleve == null) {
            dateReleve = LocalDateTime.now();
        }
    }
}
