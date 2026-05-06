package com.metereye.backend.service;

import com.metereye.backend.entity.Alerte;
import com.metereye.backend.entity.User;

public interface NotificationService {

    // Méthode principale
    void notifyUser(User user, Alerte alerte);

}