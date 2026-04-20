// AlerteServiceImpl.java
package com.metereye.backend.service.impl;

import com.metereye.backend.dto.AlerteResponseDTO;
import com.metereye.backend.entity.Alerte;
import com.metereye.backend.entity.User;
import com.metereye.backend.mapper.AlerteMapper;
import com.metereye.backend.repository.AlerteRepository;
import com.metereye.backend.service.AlerteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AlerteServiceImpl implements AlerteService {

    private final AlerteRepository alerteRepository;
    private final AlerteMapper alerteMapper;

    @Override
    public List<AlerteResponseDTO> getAlertesByUser(User user) {
        List<Alerte> alertes = alerteRepository.findByDestinationOrderByDateCreationDesc(user);
        return alerteMapper.toResponseList(alertes);
    }

    @Override
    public List<AlerteResponseDTO> getAlertesNonLues(User user) {
        List<Alerte> alertes = alerteRepository.findByDestinationAndLueFalse(user);
        return alerteMapper.toResponseList(alertes);
    }

    @Override
    public void marquerCommeLue(Long alerteId) {
        Alerte alerte = alerteRepository.findById(alerteId)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée"));
        alerte.setLue(true);
        alerteRepository.save(alerte);
    }

    @Override
    public void envoyerAlertes() {
        List<Alerte> alertesNonEnvoyees = alerteRepository.findByEnvoyeeFalse();

        for (Alerte alerte : alertesNonEnvoyees) {
            // Logique d'envoi selon le canal
            switch (alerte.getCanal()) {
                case PUSH_MOBILE:
                    envoyerPushNotification(alerte);
                    break;
                case SMS:
                    envoyerSMS(alerte);
                    break;
                case EMAIL:
                    envoyerEmail(alerte);
                    break;
            }
            alerte.setEnvoyee(true);
            alerte.setDateEnvoi(LocalDateTime.now());
            alerteRepository.save(alerte);
        }
    }

    private void envoyerPushNotification(Alerte alerte) {
        log.info("Envoi push notification à {}: {}", alerte.getDestination().getEmail(), alerte.getMessage());
        // Implémentation réelle avec Firebase Cloud Messaging
    }

    private void envoyerSMS(Alerte alerte) {
        log.info("Envoi SMS à {}: {}", alerte.getDestination().getTelephone(), alerte.getMessage());
        // Implémentation réelle avec API SMS
    }

    private void envoyerEmail(Alerte alerte) {
        log.info("Envoi email à {}: {}", alerte.getDestination().getEmail(), alerte.getMessage());
        // Implémentation réelle avec JavaMailSender
    }
}