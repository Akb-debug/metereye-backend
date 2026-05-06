package com.metereye.backend.service.impl;

import com.metereye.backend.dto.ManualReadingRequest;
import com.metereye.backend.dto.ReadingResponse;
import com.metereye.backend.dto.SensorReadingRequest;
import com.metereye.backend.entity.Compteur;
import com.metereye.backend.entity.Releve;
import com.metereye.backend.entity.User;
import com.metereye.backend.enums.SourceReleve;
import com.metereye.backend.enums.StatutReleve;
import com.metereye.backend.enums.TypeAlerte;
import com.metereye.backend.enums.TypeCompteur;
import com.metereye.backend.repository.CompteurRepository;
import com.metereye.backend.repository.ReleveRepository;
import com.metereye.backend.service.AlerteService;
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

    private static final double SEUIL_CREDIT_FAIBLE = 1000.0;
    private static final double SEUIL_CONSOMMATION_ANORMALE = 2.0;
    private static final double SEUIL_JOURS_COUPURE = 2.0;

    private final ReleveRepository releveRepository;
    private final CompteurRepository compteurRepository;
    private final ImageService imageService;
    private final AlerteService alerteService;

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

        declencherAlertesApresReleve(compteur, releve, user);

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

        declencherAlertesApresReleve(compteur, releve, user);

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

        declencherAlertesApresReleve(compteur, releve, user);

        log.info("Relevé capteur {} créé avec succès", releve.getId());
        return mapToResponse(releve);
    }

    @Override
    public Page<ReadingResponse> getMeterReadings(Long meterId, Pageable pageable, User user) {
        Compteur compteur = findAndValidateMeter(meterId, user);
        Page<Releve> releves = releveRepository.findByCompteurOrderByDateTimeDesc(compteur, pageable);
        return releves.map(this::mapToResponse);
    }

    @Override
    public ReadingResponse getLatestReading(Long meterId, User user) {
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
            return 0.0;
        }

        if (compteur.getTypeCompteur() == TypeCompteur.CLASSIQUE) {
            return calculerConsommationClassique(compteur, ancienneValeur, nouvelleValeur);
        }

        if (compteur.getTypeCompteur() == TypeCompteur.CASH_POWER) {
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
            safeCreateAlert(
                    compteur.getProprietaire(),
                    compteur,
                    TypeAlerte.ANOMALIE_CONSOMMATION,
                    "Anomalie détectée : la valeur du compteur classique a diminué."
            );

            throw new RuntimeException("Valeur invalide : un compteur classique ne peut pas diminuer");
        }

        Double consommation = nouvelleValeur - ancienneValeur;

        if (consommation > ancienneValeur * SEUIL_CONSOMMATION_ANORMALE) {
            safeCreateAlert(
                    compteur.getProprietaire(),
                    compteur,
                    TypeAlerte.ANOMALIE_CONSOMMATION,
                    "Consommation anormalement élevée détectée sur le compteur " + compteur.getReference()
            );
        }

        return consommation;
    }

    private Double calculerConsommationCashPower(
            Compteur compteur,
            Double ancienneValeur,
            Double nouvelleValeur
    ) {
        if (nouvelleValeur < ancienneValeur) {
            return ancienneValeur - nouvelleValeur;
        }

        if (nouvelleValeur > ancienneValeur) {
            log.info("Recharge CashPower détectée automatiquement pour compteur {}", compteur.getId());
            return 0.0;
        }

        return 0.0;
    }

    private void declencherAlertesApresReleve(Compteur compteur, Releve releve, User user) {
        safeCreateAlert(
                user,
                compteur,
                TypeAlerte.NOUVEAU_RELEVE,
                "Nouveau relevé enregistré pour le compteur " + compteur.getReference()
        );

        if (compteur.getTypeCompteur() == TypeCompteur.CASH_POWER) {
            verifierCreditCashPower(compteur, releve, user);
        }
    }

    private void verifierCreditCashPower(Compteur compteur, Releve releve, User user) {
        Double creditActuel = compteur.getCreditActuel();

        if (creditActuel == null) {
            return;
        }

        if (creditActuel <= SEUIL_CREDIT_FAIBLE) {
            safeCreateAlert(
                    user,
                    compteur,
                    TypeAlerte.CREDIT_FAIBLE,
                    "Votre crédit Cash Power est faible : " + creditActuel + " FCFA restants."
            );
        }

        Double consommation = releve.getConsommationCalculee();

        if (consommation != null && consommation > 0) {
            double joursRestants = creditActuel / consommation;

            if (joursRestants <= SEUIL_JOURS_COUPURE) {
                safeCreateAlert(
                        user,
                        compteur,
                        TypeAlerte.COUPURE_IMMINENTE,
                        "Coupure probable dans environ " + Math.max(1, Math.round(joursRestants)) + " jour(s)."
                );
            }
        }
    }

    private void safeCreateAlert(User destination, Compteur compteur, TypeAlerte typeAlerte, String message) {
        try {
            alerteService.creerAlerte(destination, compteur, typeAlerte, message);
        } catch (Exception e) {
            log.error("Erreur lors du déclenchement de l'alerte {} : {}", typeAlerte, e.getMessage());
        }
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