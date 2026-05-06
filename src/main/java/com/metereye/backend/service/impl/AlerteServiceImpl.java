package com.metereye.backend.service.impl;

import com.metereye.backend.dto.AlerteResponseDTO;
import com.metereye.backend.entity.Alerte;
import com.metereye.backend.entity.Compteur;
import com.metereye.backend.entity.User;
import com.metereye.backend.enums.TypeAlerte;
import com.metereye.backend.mapper.AlerteMapper;
import com.metereye.backend.repository.AlerteRepository;
import com.metereye.backend.service.AlerteService;
import com.metereye.backend.service.NotificationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlerteServiceImpl implements AlerteService {

    private final AlerteRepository alerteRepository;
    private final AlerteMapper alerteMapper;
    private final NotificationService notificationService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AlerteResponseDTO creerAlerte(
            User destination,
            Compteur compteur,
            TypeAlerte typeAlerte,
            String message
    ) {
        log.debug("Création alerte type={} pour user={} compteur={}",
                typeAlerte, destination.getId(), compteur.getId());

        // Les entités passées en paramètre peuvent être détachées (chargées dans TX1 suspendue).
        // getReference() les rattache à la session TX2 sans requête SELECT supplémentaire.
        User managedUser = entityManager.getReference(User.class, destination.getId());
        Compteur managedCompteur = entityManager.getReference(Compteur.class, compteur.getId());

        Alerte alerte = Alerte.builder()
                .destination(managedUser)
                .compteur(managedCompteur)
                .typeAlerte(typeAlerte)
                .message(message)
                .lue(false)
                .build();

        @SuppressWarnings("null") Alerte saved = alerteRepository.save(alerte);
        log.info("Alerte id={} type={} créée pour user={}", saved.getId(), typeAlerte, destination.getId());

        try {
            notificationService.notifyUser(managedUser, saved);
        } catch (Exception e) {
            log.error("Notification échouée pour alerte id={} type={} : {}",
                    saved.getId(), typeAlerte, e.getMessage(), e);
        }

        return alerteMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlerteResponseDTO> getAlertesByUser(User user) {
        List<Alerte> alertes = alerteRepository.findByDestinationOrderByDateCreationDesc(user);
        return alerteMapper.toResponseList(alertes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlerteResponseDTO> getAlertesNonLues(User user) {
        List<Alerte> alertes =
                alerteRepository.findByDestinationAndLueFalseOrderByDateCreationDesc(user);
        return alerteMapper.toResponseList(alertes);
    }

    @Override
    @Transactional
    public void marquerCommeLue(Long alerteId) {
        Alerte alerte = alerteRepository.findById(alerteId)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée"));
        if (!Boolean.TRUE.equals(alerte.getLue())) {
            alerte.setLue(true);
            alerteRepository.save(alerte);
        }
    }

    @Override
    @Transactional
    public void marquerToutesCommeLues(User user) {
        List<Alerte> alertes =
                alerteRepository.findByDestinationAndLueFalseOrderByDateCreationDesc(user);
        if (!alertes.isEmpty()) {
            alertes.forEach(a -> a.setLue(true));
            alerteRepository.saveAll(alertes);
        }
    }
}
