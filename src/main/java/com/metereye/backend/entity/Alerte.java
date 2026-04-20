// Alerte.java
package com.metereye.backend.entity;

import com.metereye.backend.enums.CanalEnvoi;
import com.metereye.backend.enums.TypeAlerte;
import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalEnvoi canal;

    @Column(name = "lue")
    @Builder.Default
    private Boolean lue = false;

    @Column(name = "envoyee")
    @Builder.Default
    private Boolean envoyee = false;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;
}