package com.metereye.backend.service.impl;

import com.metereye.backend.dto.NotificationPreferenceRequestDTO;
import com.metereye.backend.dto.NotificationPreferenceResponseDTO;
import com.metereye.backend.entity.NotificationPreference;
import com.metereye.backend.entity.User;
import com.metereye.backend.mapper.NotificationPreferenceMapper;
import com.metereye.backend.repository.NotificationPreferenceRepository;
import com.metereye.backend.service.NotificationPreferenceService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository repository;
    private final NotificationPreferenceMapper mapper;

    @Override
    public NotificationPreferenceResponseDTO getPreferences(User user) {

        NotificationPreference pref = repository.findByUser(user)
                .orElseGet(() -> repository.save(
                        NotificationPreference.builder()
                                .user(user)
                                .notificationsActive(true)
                                .pushEnabled(true)
                                .emailEnabled(true)
                                .smsEnabled(false)
                                .creditAlerts(true)
                                .anomalyAlerts(true)
                                .systemAlerts(true)
                                .build()
                ));

        return mapper.toResponse(pref);
    }

    @Override
    public NotificationPreferenceResponseDTO updatePreferences(
            User user,
            NotificationPreferenceRequestDTO request) {

        NotificationPreference pref = repository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Préférences non trouvées"));

        pref.setPushEnabled(request.getPushEnabled());
        pref.setEmailEnabled(request.getEmailEnabled());
        pref.setSmsEnabled(request.getSmsEnabled());

        pref.setCreditAlerts(request.getCreditAlerts());
        pref.setAnomalyAlerts(request.getAnomalyAlerts());
        pref.setSystemAlerts(request.getSystemAlerts());

        repository.save(pref);

        return mapper.toResponse(pref);
    }
}