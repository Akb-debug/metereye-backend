package com.metereye.backend.service;

import com.metereye.backend.dto.AlerteResponseDTO;
import com.metereye.backend.entity.Compteur;
import com.metereye.backend.entity.User;
import com.metereye.backend.enums.TypeAlerte;

import java.util.List;

public interface AlerteService {

    AlerteResponseDTO creerAlerte(
            User destination,
            Compteur compteur,
            TypeAlerte typeAlerte,
            String message
    );

    List<AlerteResponseDTO> getAlertesByUser(User user);

    List<AlerteResponseDTO> getAlertesNonLues(User user);

    void marquerCommeLue(Long alerteId);

    void marquerToutesCommeLues(User user);
}