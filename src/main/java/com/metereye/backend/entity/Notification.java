package com.metereye.backend.entity;

import com.metereye.backend.enums.CanalEnvoi;
import com.metereye.backend.enums.NotificationStatus;
import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notifications")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alerte_id")
    private Alerte alerte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalEnvoi canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;
}