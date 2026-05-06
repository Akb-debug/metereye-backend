package com.metereye.backend.entity;

import com.metereye.backend.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notification_preferences")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Activation globale
    @Column(nullable = false)
    private Boolean notificationsActive = true;

    // Canaux
    @Column(nullable = false)
    private Boolean pushEnabled = true;

    @Column(nullable = false)
    private Boolean emailEnabled = true;

    @Column(nullable = false)
    private Boolean smsEnabled = false;

    // Types critiques
    @Column(nullable = false)
    private Boolean creditAlerts = true;

    @Column(nullable = false)
    private Boolean anomalyAlerts = true;

    @Column(nullable = false)
    private Boolean systemAlerts = true;
}