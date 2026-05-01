package com.metereye.backend.service.impl;

import com.metereye.backend.dto.ManualReadingRequest;
import com.metereye.backend.dto.ReadingResponse;
import com.metereye.backend.dto.SensorReadingRequest;
import com.metereye.backend.entity.Compteur;
import com.metereye.backend.entity.Releve;
import com.metereye.backend.entity.User;
import com.metereye.backend.enums.SourceReleve;
import com.metereye.backend.enums.StatutReleve;
import com.metereye.backend.enums.TypeCompteur;
import com.metereye.backend.repository.CompteurRepository;
import com.metereye.backend.repository.ReleveRepository;
import com.metereye.backend.service.ImageService;
import com.metereye.backend.service.ReadingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReadingServiceImpl implements ReadingService {

    private final ReleveRepository releveRepository;
    private final CompteurRepository compteurRepository;
    private final ImageService imageService;

    @Override
    public ReadingResponse createManualReading(ManualReadingRequest request, User user) {
        log.info("Création relevé manuel pour compteur {} par utilisateur {}", request.getMeterId(), user.getId());

        Compteur compteur = findAndValidateMeter(request.getMeterId(), user);

        Double consommation = calculerConsommation(compteur, request.getValue());

        Releve releve = Releve.builder()
                .compteur(compteur)
                .valeur(request.getValue())
                .dateTime(LocalDateTime.now())
                .source(SourceReleve.MANUEL)
                .statut(StatutReleve.VALIDE)
                .consommationCalculee(consommation)
                .commentaire(request.getComment())
                .build();

        compteur.mettreAJourValeur(request.getValue());
        compteurRepository.save(compteur);

        releve = releveRepository.save(releve);

        log.info("Relevé manuel {} créé avec succès", releve.getId());
        return mapToResponse(releve);
    }

    @Override
    public ReadingResponse createImageReading(Long meterId, MultipartFile file, User user) throws IOException {
        log.info("Création relevé image pour compteur {} par utilisateur {}", meterId, user.getId());

        Compteur compteur = findAndValidateMeter(meterId, user);

        Releve releve = Releve.builder()
                .compteur(compteur)
                .valeur(0.0)
                .dateTime(LocalDateTime.now())
                .source(SourceReleve.ESP32_CAM)
                .statut(StatutReleve.EN_ATTENTE)
                .consommationCalculee(0.0)
                .build();

        releve = releveRepository.save(releve);

        var image = imageService.saveImage(file, releve);

        imageService.processImageWithOCR(image.getId());

        var processedImage = imageService.getImage(image.getId());

        if (processedImage.getOcrValue() == null) {
            releve.setStatut(StatutReleve.ERREUR);
            releve.setCommentaire("Impossible de lire la valeur OCR");
            releve = releveRepository.save(releve);
            return mapToResponse(releve);
        }

        Double valeurOCR = processedImage.getOcrValue();

        Double consommation = calculerConsommation(compteur, valeurOCR);

        releve.setValeur(valeurOCR);
        releve.setConsommationCalculee(consommation);
        releve.setStatut(StatutReleve.VALIDE);

        compteur.mettreAJourValeur(valeurOCR);
        compteurRepository.save(compteur);

        releve = releveRepository.save(releve);

        log.info("Relevé image {} créé avec succès", releve.getId());
        return mapToResponse(releve);
    }

    @Override
    public ReadingResponse createSensorReading(SensorReadingRequest request, User user) {
        log.info("Création relevé capteur {} pour compteur {} par utilisateur {}",
                request.getSensorId(), request.getMeterId(), user.getId());

        Compteur compteur = findAndValidateMeter(request.getMeterId(), user);

        Double consommation = calculerConsommation(compteur, request.getValue());

        Releve releve = Releve.builder()
                .compteur(compteur)
                .valeur(request.getValue())
                .dateTime(LocalDateTime.now())
                .source(SourceReleve.SENSOR)
                .statut(StatutReleve.VALIDE)
                .consommationCalculee(consommation)
                .commentaire("Capteur: " + request.getSensorId())
                .build();

        compteur.mettreAJourValeur(request.getValue());
        compteurRepository.save(compteur);

        releve = releveRepository.save(releve);

        log.info("Relevé capteur {} créé avec succès", releve.getId());
        return mapToResponse(releve);
    }

    @Override
    public Page<ReadingResponse> getMeterReadings(Long meterId, Pageable pageable, User user) {
        log.info("Récupération relevés pour compteur {} par utilisateur {}", meterId, user.getId());

        Compteur compteur = findAndValidateMeter(meterId, user);

        Page<Releve> releves = releveRepository.findByCompteurOrderByDateTimeDesc(compteur, pageable);
        return releves.map(this::mapToResponse);
    }

    @Override
    public ReadingResponse getLatestReading(Long meterId, User user) {
        log.info("Récupération dernier relevé pour compteur {} par utilisateur {}", meterId, user.getId());

        Compteur compteur = findAndValidateMeter(meterId, user);

        Optional<Releve> releve = releveRepository.findTopByCompteurOrderByDateTimeDesc(compteur);

        return releve.map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Aucun relevé trouvé pour ce compteur"));
    }

    private Double calculerConsommation(Compteur compteur, Double nouvelleValeur) {
        if (nouvelleValeur == null) {
            throw new RuntimeException("La valeur du relevé est obligatoire");
        }

        if (nouvelleValeur < 0) {
            throw new RuntimeException("La valeur du relevé ne peut pas être négative");
        }

        Double ancienneValeur = compteur.getValeurActuelle();

        if (ancienneValeur == null) {
            log.info("Premier relevé du compteur {}. Consommation = 0", compteur.getId());
            return 0.0;
        }

        TypeCompteur typeCompteur = compteur.getTypeCompteur();

        if (typeCompteur == TypeCompteur.CLASSIQUE) {
            return calculerConsommationClassique(compteur, ancienneValeur, nouvelleValeur);
        }

        if (typeCompteur == TypeCompteur.CASH_POWER) {
            return calculerConsommationCashPower(compteur, ancienneValeur, nouvelleValeur);
        }

        throw new RuntimeException("Type de compteur non reconnu");
    }

    private Double calculerConsommationClassique(
            Compteur compteur,
            Double ancienneValeur,
            Double nouvelleValeur
    ) {
        if (nouvelleValeur < ancienneValeur) {
            log.error(
                    "Anomalie compteur classique {} : ancienne valeur {}, nouvelle valeur {}",
                    compteur.getId(),
                    ancienneValeur,
                    nouvelleValeur
            );

            throw new RuntimeException(
                    "Valeur invalide : un compteur classique ne peut pas diminuer"
            );
        }

        return nouvelleValeur - ancienneValeur;
    }

    private Double calculerConsommationCashPower(
            Compteur compteur,
            Double ancienneValeur,
            Double nouvelleValeur
    ) {
        if (nouvelleValeur < ancienneValeur) {
            Double consommation = ancienneValeur - nouvelleValeur;

            log.info(
                    "Consommation CashPower détectée pour compteur {} : ancienne valeur {}, nouvelle valeur {}, consommation {}",
                    compteur.getId(),
                    ancienneValeur,
                    nouvelleValeur,
                    consommation
            );

            return consommation;
        }

        if (nouvelleValeur > ancienneValeur) {
            log.info(
                    "Recharge CashPower détectée automatiquement pour compteur {} : ancienne valeur {}, nouvelle valeur {}. Consommation enregistrée = 0",
                    compteur.getId(),
                    ancienneValeur,
                    nouvelleValeur
            );

            return 0.0;
        }

        log.info(
                "Aucune variation détectée pour compteur CashPower {} : valeur {}",
                compteur.getId(),
                nouvelleValeur
        );

        return 0.0;
    }

    private Compteur findAndValidateMeter(Long meterId, User user) {
        Compteur compteur = compteurRepository.findById(meterId)
                .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));

        boolean isOwner = compteur.getProprietaire().getId().equals(user.getId());
        boolean isAdmin = user.getRole().getName().name().equals("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Non autorisé à accéder à ce compteur");
        }

        return compteur;
    }

    private ReadingResponse mapToResponse(Releve releve) {
        ReadingResponse response = new ReadingResponse();

        response.setId(releve.getId());
        response.setValue(releve.getValeur());
        response.setDateTime(releve.getDateTime());
        response.setSource(releve.getSource());
        response.setStatut(releve.getStatut());
        response.setConsommation(releve.getConsommationCalculee());
        response.setCommentaire(releve.getCommentaire());
        response.setMeterId(releve.getCompteur().getId());
        response.setMeterReference(releve.getCompteur().getReference());

        if (releve.getImage() != null) {
            response.setImageUrl(imageService.getImageUrl(releve.getImage().getId()));
            response.setOcrConfidence(releve.getImage().getOcrConfidence());
        }

        return response;
    }
}