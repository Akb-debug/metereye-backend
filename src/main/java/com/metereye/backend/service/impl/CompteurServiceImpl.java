// CompteurServiceImpl.java
package com.metereye.backend.service.impl;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.*;
import com.metereye.backend.enums.*;
import com.metereye.backend.mapper.CompteurMapper;
import com.metereye.backend.mapper.ReleveMapper;
import com.metereye.backend.repository.*;
import com.metereye.backend.service.CompteurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompteurServiceImpl implements CompteurService {

    private final CompteurRepository compteurRepository;
    private final ReleveRepository releveRepository;
    private final UserRepository userRepository;
    private final CompteurMapper compteurMapper;
    private final ReleveMapper releveMapper;

    @Override
    public CompteurResponseDTO creerCompteur(CompteurRequestDTO request, User proprietaire) {
        if (compteurRepository.findByReference(request.getReference()).isPresent()) {
            throw new RuntimeException("Un compteur avec cette référence existe déjà");
        }

        Compteur compteur = compteurMapper.toEntity(request);
        compteur.setProprietaire(proprietaire);

        // Initialiser les valeurs selon le type
        if (compteur.getTypeCompteur() == TypeCompteur.CLASSIQUE) {
            compteur.setIndexActuel(request.getValeurInitiale());
        } else {
            compteur.setCreditActuel(request.getValeurInitiale());
        }

        Compteur savedCompteur = compteurRepository.save(compteur);

        // Créer le premier relevé
        Releve premierReleve = Releve.builder()
                .compteur(savedCompteur)
                .valeur(request.getValeurInitiale())
                .dateTime(LocalDateTime.now())
                .source(SourceReleve.MANUEL)
                .statut(StatutReleve.VALIDE)
                .commentaire("Relevé initial")
                .build();
        releveRepository.save(premierReleve);

        log.info("Compteur créé: {} par {}", request.getReference(), proprietaire.getEmail());
        return compteurMapper.toResponse(savedCompteur);
    }

    @Override
    public CompteurResponseDTO getCompteurById(Long id, User user) {
        Compteur compteur = compteurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));
        checkOwnership(compteur, user);
        return compteurMapper.toResponse(compteur);
    }

    @Override
    public List<CompteurResponseDTO> getCompteursByUser(Long userId) {
        List<Compteur> compteurs = compteurRepository.findByProprietaireIdAndActifTrue(userId);
        return compteurMapper.toResponseList(compteurs);
    }

    @Override
    public List<CompteurResponseDTO> getAllCompteurs() {
        List<Compteur> compteurs = compteurRepository.findAll();
        return compteurMapper.toResponseList(compteurs);
    }

    @Override
    public CompteurResponseDTO desactiverCompteur(Long id, User user) {
        Compteur compteur = compteurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));
        checkOwnership(compteur, user);
        compteur.setActif(false);
        return compteurMapper.toResponse(compteurRepository.save(compteur));
    }

    @Override
    public ReleveResponseDTO ajouterReleve(ReleveRequestDTO request, User user) {
        Compteur compteur = compteurRepository.findById(request.getCompteurId())
                .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));

        checkOwnership(compteur, user);

        // Créer le relevé avec validation
        Releve releve = releveMapper.toEntity(request);
        releve.setCompteur(compteur);
        
        // Valider selon les nouvelles règles métier
        if (!releve.estValidePourCreation()) {
            String erreur = releve.getMessageErreurValidation();
            throw new RuntimeException(erreur != null ? erreur : "Relevé non valide");
        }
        
        // Calculer la consommation
        releve.calculerConsommation();
        Releve savedReleve = releveRepository.save(releve);

        // Mettre à jour le compteur
        compteur.mettreAJourValeur(request.getValeur());
        compteurRepository.save(compteur);

        log.info("Relevé ajouté: compteur={}, valeur={}, source={}", 
                compteur.getReference(), request.getValeur(), releve.getSource());
        return releveMapper.toResponse(savedReleve);
    }

    @Override
    public List<ReleveResponseDTO> getHistoriqueReleves(Long compteurId, LocalDateTime startDate, LocalDateTime endDate, User user) {
        Compteur compteur = compteurRepository.findById(compteurId)
                .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));
        checkOwnership(compteur, user);

        List<Releve> releves;
        if (startDate != null && endDate != null) {
            releves = releveRepository.findByCompteurAndDateTimeBetween(compteur, startDate, endDate);
        } else {
            releves = releveRepository.findByCompteurOrderByDateTimeDesc(compteur);
        }

        return releveMapper.toResponseList(releves);
    }

    private void checkOwnership(Compteur compteur, User user) {
        boolean isOwner = compteur.getProprietaire().getId().equals(user.getId());
        boolean isAdmin = user.getRole().getName().name().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Non autorisé à accéder à ce compteur");
        }
    }

    @Override
    public Double calculerConsommation(Long compteurId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Releve> releves = releveRepository.findByCompteurAndDateTimeBetween(
                compteurRepository.findById(compteurId).orElseThrow(() -> new RuntimeException("Compteur non trouvé")),
                startDate, endDate);

        if (releves.size() < 2) {
            return 0.0;
        }

        Releve premier = releves.get(releves.size() - 1);
        Releve dernier = releves.get(0);

        return dernier.getValeur() - premier.getValeur();
    }

    @Override
    public CompteurResponseDTO rechargerCompteur(RechargeRequestDTO request) {
        Compteur compteur = compteurRepository.findById(request.getCompteurId())
                .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));

        if (compteur.getTypeCompteur() != TypeCompteur.CASH_POWER) {
            throw new RuntimeException("Seuls les compteurs Cash Power peuvent être rechargés");
        }

        compteur.recharger(request.getMontant(), request.getCodeRecharge());
        Compteur updated = compteurRepository.save(compteur);

        // Ajouter un relevé pour la recharge
        Releve releveRecharge = Releve.builder()
                .compteur(updated)
                .valeur(compteur.getCreditActuel())
                .dateTime(LocalDateTime.now())
                .source(SourceReleve.MANUEL)
                .statut(StatutReleve.VALIDE)
                .commentaire("Recharge de " + request.getMontant() + " FCFA")
                .build();
        releveRepository.save(releveRecharge);

        log.info("Recharge effectuée: compteur={}, montant={}", compteur.getReference(), request.getMontant());
        return compteurMapper.toResponse(updated);
    }

    @Override
    public ReleveResponseDTO ajouterReleveParOCR(Long compteurId, String imageBase64, User user) {
        throw new RuntimeException("OCR via base64 non implémenté. Utiliser POST /api/readings/image à la place.");
    }

    @Override
    public ConsommationStatsDTO getStatistiquesConsommation(Long compteurId, String periode) {
        Compteur compteur = compteurRepository.findById(compteurId)
                .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));

        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime debutJour = maintenant.toLocalDate().atStartOfDay();
        LocalDateTime debutSemaine = maintenant.minusDays(7);
        LocalDateTime debutMois = maintenant.minusDays(30);

        Double consommationJour = sumConsommation(compteurId, debutJour, maintenant);
        Double consommationSemaine = sumConsommation(compteurId, debutSemaine, maintenant);
        Double consommationMois = sumConsommation(compteurId, debutMois, maintenant);

        long joursAvecDonnees = releveRepository
                .findByCompteurAndDateTimeBetween(compteur, debutMois, maintenant)
                .stream()
                .map(r -> r.getDateTime().toLocalDate())
                .distinct()
                .count();
        Double moyenneJour = joursAvecDonnees > 0 ? consommationMois / joursAvecDonnees : 0.0;

        ConsommationStatsDTO.ConsommationStatsDTOBuilder builder = ConsommationStatsDTO.builder()
                .consommationJour(consommationJour)
                .consommationSemaine(consommationSemaine)
                .consommationMois(consommationMois)
                .consommationMoyenneJour(moyenneJour);

        if (compteur.getTypeCompteur() == TypeCompteur.CASH_POWER) {
            builder.creditRestant(compteur.getCreditActuel());
            if (moyenneJour > 0 && compteur.getCreditActuel() != null) {
                long joursRestants = (long) (compteur.getCreditActuel() / moyenneJour);
                builder.dateEstimationEpuisement(maintenant.plusDays(joursRestants));
            }
        }

        return builder.build();
    }

    private Double sumConsommation(Long compteurId, LocalDateTime debut, LocalDateTime fin) {
        return releveRepository.findRecentRelevesByCompteur(compteurId, debut).stream()
                .filter(r -> r.getConsommationCalculee() != null && r.getDateTime().isBefore(fin))
                .mapToDouble(Releve::getConsommationCalculee)
                .sum();
    }

    @Override
    public Compteur configurerModeLecture(Long compteurId, ModeLectureCompteur modeLecture, User user) {
        Compteur compteur = compteurRepository.findById(compteurId)
                .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));

        checkOwnership(compteur, user);

        // Configurer le mode de lecture
        compteur.setModeLectureConfigure(modeLecture);
        compteur.setStatut(com.metereye.backend.enums.StatutCompteur.ACTIF);

        Compteur saved = compteurRepository.save(compteur);
        
        log.info("Mode de lecture configuré: compteur={}, mode={}, utilisateur={}", 
                compteur.getReference(), modeLecture, user.getEmail());
        
        return saved;
    }

    @Override
    public Compteur reinitialiserCompteur(Long compteurId, String motif, User user) {
        Compteur compteur = compteurRepository.findById(compteurId)
                .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));

        checkOwnership(compteur, user);

        // Vérifier que c'est un compteur classique
        if (compteur.getTypeCompteur() != com.metereye.backend.enums.TypeCompteur.CLASSIQUE) {
            throw new RuntimeException("Seuls les compteurs CLASSIQUE peuvent être réinitialisés");
        }

        // Réinitialiser le compteur
        compteur.reinitialiser(motif);
        Compteur saved = compteurRepository.save(compteur);
        
        log.info("Compteur réinitialisé: compteur={}, motif={}, utilisateur={}", 
                compteur.getReference(), motif, user.getEmail());
        
        return saved;
    }
}
