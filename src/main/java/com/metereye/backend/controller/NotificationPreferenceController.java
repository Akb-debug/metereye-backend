package com.metereye.backend.controller;

import com.metereye.backend.dto.NotificationPreferenceRequestDTO;
import com.metereye.backend.dto.NotificationPreferenceResponseDTO;
import com.metereye.backend.entity.User;
import com.metereye.backend.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService service;

    @GetMapping
    public NotificationPreferenceResponseDTO getPreferences(
            @AuthenticationPrincipal User user
    ) {
        return service.getPreferences(user);
    }

    @PutMapping
    public NotificationPreferenceResponseDTO updatePreferences(
            @AuthenticationPrincipal User user,
            @RequestBody NotificationPreferenceRequestDTO request
    ) {
        return service.updatePreferences(user, request);
    }
}