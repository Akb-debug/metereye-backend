// ReadingServiceImpl.java
package com.metereye.backend.service.impl;

import com.metereye.backend.dto.ManualReadingRequest;
import com.metereye.backend.dto.ReadingResponse;
import com.metereye.backend.dto.SensorReadingRequest;
import com.metereye.backend.entity.*;
import com.metereye.backend.enums.SourceReleve;
import com.metereye.backend.enums.StatutReleve;
import com.metereye.backend.repository.CompteurRepository;
import com.metereye.backend.repository.ReleveRepository;
import com.metereye.backend.service.ImageService;
import com.metereye.backend.service.ReadingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

        // Créer le relevé
        Releve releve = Releve.builder()
                .compteur(compteur)
                .valeur(request.getValue())
                .dateTime(LocalDateTime.now())
                .source(SourceReleve.MANUEL)
                .statut(StatutReleve.VALIDE)
                .commentaire(request.getComment())
                .build();

        // Calculer la consommation
        releve.setConsommationCalculee(calculerConsommation(compteur, request.getValue()));

        // Mettre à jour le compteur
        compteur.mettreAJourValeur(request.getValue());
        compteurRepository.save(compteur);

        // Sauvegarder le relevé
        releve = releveRepository.save(releve);

        log.info("Relevé manuel {} créé avec succès", releve.getId());
        return mapToResponse(releve);
    }

    @Override
    public ReadingResponse createImageReading(Long meterId, MultipartFile file, User user) throws IOException {
        log.info("Création relevé image pour compteur {} par utilisateur {}", meterId, user.getId());

        Compteur compteur = findAndValidateMeter(meterId, user);

        // Créer le relevé avec valeur temporaire (sera mise à jour après OCR)
        Releve releve = Releve.builder()
                .compteur(compteur)
                .valeur(0.0) // Sera mis à jour après OCR
                .dateTime(LocalDateTime.now())
                .source(SourceReleve.ESP32_CAM)
                .statut(StatutReleve.EN_ATTENTE)
                .build();

        releve = releveRepository.save(releve);

        // Sauvegarder l'image
        var image = imageService.saveImage(file, releve);

        // Lancer le traitement OCR
        imageService.processImageWithOCR(image.getId());

        // Mettre à jour le relevé avec la valeur OCR
        var processedImage = imageService.getImage(image.getId());
        if (processedImage.getOcrValue() != null) {
            releve.setValeur(processedImage.getOcrValue());
            releve.setConsommationCalculee(calculerConsommation(compteur, processedImage.getOcrValue()));
            releve.setStatut(StatutReleve.VALIDE);
            
            // Mettre à jour le compteur
            compteur.mettreAJourValeur(processedImage.getOcrValue());
            compteurRepository.save(compteur);
        } else {
            releve.setStatut(StatutReleve.ERREUR);
        }

        releve = releveRepository.save(releve);

        log.info("Relevé image {} créé avec succès", releve.getId());
        return mapToResponse(releve);
    }

    @Override
    public ReadingResponse createSensorReading(SensorReadingRequest request, User user) {
        log.info("Création relevé capteur {} pour compteur {} par utilisateur {}", 
                request.getSensorId(), request.getMeterId(), user.getId());

        Compteur compteur = findAndValidateMeter(request.getMeterId(), user);

        // Créer le relevé
        Releve releve = Releve.builder()
                .compteur(compteur)
                .valeur(request.getValue())
                .dateTime(LocalDateTime.now())
                .source(SourceReleve.SENSOR)
                .statut(StatutReleve.VALIDE)
                .commentaire("Capteur: " + request.getSensorId())
                .build();

        // Calculer la consommation
        releve.setConsommationCalculee(calculerConsommation(compteur, request.getValue()));

        // Mettre à jour le compteur
        compteur.mettreAJourValeur(request.getValue());
        compteurRepository.save(compteur);

        // Sauvegarder le relevé
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

    private Compteur findAndValidateMeter(Long meterId, User user) {
        Compteur compteur = compteurRepository.findById(meterId)
                .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));

        // Vérifier autorisation
        if (!compteur.getProprietaire().getId().equals(user.getId()) && 
            !user.getRole().getName().name().equals("STAFF")) {
            throw new RuntimeException("Non autorisé à accéder à ce compteur");
        }

        return compteur;
    }

    private Double calculerConsommation(Compteur compteur, Double nouvelleValeur) {
        if (compteur.getTypeCompteur() == com.metereye.backend.enums.TypeCompteur.CLASSIQUE) {
            return compteur.calculerConsommation();
        }
        return 0.0; // Pas de consommation calculée pour Cash Power
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

        // Ajouter les informations d'image si présentes
        if (releve.getImage() != null) {
            response.setImageUrl(imageService.getImageUrl(releve.getImage().getId()));
            response.setOcrConfidence(releve.getImage().getOcrConfidence());
        }

        return response;
    }
}
