package com.metereye.backend.controller;

import com.metereye.backend.dto.NotificationResponseDTO;
import com.metereye.backend.entity.User;
import com.metereye.backend.mapper.NotificationMapper;
import com.metereye.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    @GetMapping
    public List<NotificationResponseDTO> getNotifications(
            @AuthenticationPrincipal User user
    ) {
        return mapper.toResponseList(
                repository.findByUserOrderByDateCreationDesc(user)
        );
    }
}