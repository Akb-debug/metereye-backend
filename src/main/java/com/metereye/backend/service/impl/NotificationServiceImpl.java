package com.metereye.backend.service.impl;

import com.metereye.backend.entity.Alerte;
import com.metereye.backend.entity.Notification;
import com.metereye.backend.entity.NotificationPreference;
import com.metereye.backend.entity.User;
import com.metereye.backend.enums.CanalEnvoi;
import com.metereye.backend.enums.NotificationStatus;
import com.metereye.backend.repository.NotificationPreferenceRepository;
import com.metereye.backend.repository.NotificationRepository;
import com.metereye.backend.service.EmailService;
import com.metereye.backend.service.NotificationService;
import com.metereye.backend.service.PushNotificationService;
import com.metereye.backend.service.SmsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    private final EmailService emailService;
    private final SmsService smsService;
    private final PushNotificationService pushService;

    @Override
    public void notifyUser(User user, Alerte alerte) {

        NotificationPreference pref = preferenceRepository.findByUser(user).orElse(null);

        String message = alerte.getMessage();
        String titre = alerte.getTypeAlerte().name();

        // PUSH
        if (pref == null || Boolean.TRUE.equals(pref.getPushEnabled())) {
            send(user, alerte, CanalEnvoi.PUSH, titre, message, () ->
                    pushService.send(user, message)
            );
        }

        // EMAIL
        if (pref == null || Boolean.TRUE.equals(pref.getEmailEnabled())) {
            send(user, alerte, CanalEnvoi.EMAIL, titre, message, () ->
                    emailService.send(user.getEmail(), message)
            );
        }

        // SMS
        if (pref != null && Boolean.TRUE.equals(pref.getSmsEnabled())) {
            send(user, alerte, CanalEnvoi.SMS, titre, message, () ->
                    smsService.send(user.getTelephone(), message)
            );
        }
    }

    private void send(User user, Alerte alerte, CanalEnvoi canal,
                      String titre, String message, Runnable sender) {

        Notification notification = Notification.builder()
                .user(user)
                .alerte(alerte)
                .canal(canal)
                .status(NotificationStatus.PENDING)
                .titre(titre)
                .message(message)
                .dateEnvoi(LocalDateTime.now())
                .build();

        try {
            sender.run();
            notification.setStatus(NotificationStatus.SENT);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
        }

        try {
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Impossible de persister la notification canal={} : {}", canal, e.getMessage());
        }
    }
}