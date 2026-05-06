package com.metereye.backend.service.impl;

import com.metereye.backend.entity.User;
import com.metereye.backend.service.PushNotificationService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    @Override
    public void send(User user, String message) {
        log.info("PUSH envoyé à user {} : {}", user.getId(), message);
    }
}