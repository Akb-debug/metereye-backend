package com.metereye.backend.entity;

import com.metereye.backend.enums.TypeAlerte;
import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "alertes")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Alerte extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    private User destination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compteur_id", nullable = false)
    private Compteur compteur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAlerte typeAlerte;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "lue")
    @Builder.Default
    private Boolean lue = false;
}