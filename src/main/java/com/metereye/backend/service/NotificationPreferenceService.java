package com.metereye.backend.service;

import com.metereye.backend.dto.NotificationPreferenceRequestDTO;
import com.metereye.backend.dto.NotificationPreferenceResponseDTO;
import com.metereye.backend.entity.User;

public interface NotificationPreferenceService {

    NotificationPreferenceResponseDTO getPreferences(User user);

    NotificationPreferenceResponseDTO updatePreferences(
            User user,
            NotificationPreferenceRequestDTO request
    );
}