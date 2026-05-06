package com.metereye.backend.service;

import com.metereye.backend.entity.User;

public interface PushNotificationService {

    void send(User user, String message);

}