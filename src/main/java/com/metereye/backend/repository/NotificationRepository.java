package com.metereye.backend.repository;

import com.metereye.backend.entity.Notification;
import com.metereye.backend.entity.User;
import com.metereye.backend.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Toutes les notifications (triées)
    List<Notification> findByUserOrderByDateCreationDesc(User user);

    // Notifications par statut
    List<Notification> findByUserAndStatusOrderByDateCreationDesc(
            User user,
            NotificationStatus status
    );

    // Compter notifications par statut
    long countByUserAndStatus(User user, NotificationStatus status);
}