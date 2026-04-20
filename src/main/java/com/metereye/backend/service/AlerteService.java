// AlerteService.java
package com.metereye.backend.service;

import com.metereye.backend.dto.AlerteResponseDTO;
import com.metereye.backend.entity.User;

import java.util.List;

public interface AlerteService {

    List<AlerteResponseDTO> getAlertesByUser(User user);
    List<AlerteResponseDTO> getAlertesNonLues(User user);
    void marquerCommeLue(Long alerteId);
    void envoyerAlertes();
}