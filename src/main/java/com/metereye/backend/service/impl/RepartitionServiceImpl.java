// ✅ CRÉÉ — RepartitionServiceImpl.java
package com.metereye.backend.service.impl;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.*;
import com.metereye.backend.enums.RoleName;
import com.metereye.backend.enums.StatutFacture;
import com.metereye.backend.repository.*;
import com.metereye.backend.service.RepartitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RepartitionServiceImpl implements RepartitionService {

    private final MaisonRepository maisonRepository;
    private final SousCompteurRepository sousCompteurRepository;
    private final ReleveAdditionneusRepository releveAdditionneusRepository;
    private final FactureLocataireRepository factureLocataireRepository;

    // ─── Prévisualisation (sans sauvegarde) ────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public RepartitionResponseDTO calculerRepartition(Long maisonId, Integer mois, Integer annee,
                                                      Double montantFacturePrincipale, User proprietaire) {
        Maison maison = verifierAccessMaison(maisonId, proprietaire);
        List<SousCompteur> sousCompteurs = sousCompteurRepository.findByMaisonIdAndActifTrue(maisonId);

        LocalDateTime debut = LocalDateTime.of(annee, mois, 1, 0, 0, 0);
        LocalDateTime fin = debut.plusMonths(1).minusSeconds(1);

        Map<Long, Double> consommationParSC = calculerConsommations(sousCompteurs, debut, fin);
        Double consoTotale = consommationParSC.values().stream().mapToDouble(Double::doubleValue).sum();

        List<FactureLocataireResponseDTO> factures = sousCompteurs.stream()
                .map(sc -> buildFactureDTO(sc,
                        consommationParSC.getOrDefault(sc.getId(), 0.0),
                        consoTotale, montantFacturePrincipale, mois, annee, null, null))
                .collect(Collectors.toList());

        log.info("Répartition calculée (prévisualisation): maison={}, {}/{}, montant={}",
                maisonId, mois, annee, montantFacturePrincipale);

        return RepartitionResponseDTO.builder()
                .maisonId(maison.getId())
                .maisonNom(maison.getNom())
                .mois(mois)
                .annee(annee)
                .consommationTotale(consoTotale)
                .montantTotal(montantFacturePrincipale)
                .factures(factures)
                .statut(consoTotale > 0 ? "COMPLETE" : "PARTIELLE")
                .build();
    }

    // ─── Génération et sauvegarde des factures ──────────────────────────────────

    @Override
    public RepartitionResponseDTO genererFactures(GenererFacturesRequestDTO dto, User proprietaire) {
        Maison maison = verifierAccessMaison(dto.getMaisonId(), proprietaire);
        List<SousCompteur> sousCompteurs = sousCompteurRepository.findByMaisonIdAndActifTrue(dto.getMaisonId());

        if (sousCompteurs.isEmpty()) {
            throw new RuntimeException("Aucun sous-compteur actif dans cette maison");
        }

        LocalDateTime debut = LocalDateTime.of(dto.getAnnee(), dto.getMois(), 1, 0, 0, 0);
        LocalDateTime fin = debut.plusMonths(1).minusSeconds(1);

        Map<Long, Double> consommationParSC = calculerConsommations(sousCompteurs, debut, fin);
        Double consoTotale = consommationParSC.values().stream().mapToDouble(Double::doubleValue).sum();

        List<FactureLocataireResponseDTO> facturesDTOs = new ArrayList<>();

        for (SousCompteur sc : sousCompteurs) {
            Double conso = consommationParSC.getOrDefault(sc.getId(), 0.0);
            Double partPct = consoTotale > 0 ? (conso / consoTotale) * 100 : 0.0;
            Double montant = consoTotale > 0 ? (conso / consoTotale) * dto.getMontantFacturePrincipale() : 0.0;

            // Upsert : mise à jour si facture déjà existante pour ce mois/sc
            Optional<FactureLocataire> existante = factureLocataireRepository
                    .findBySousCompteurIdAndMoisAndAnnee(sc.getId(), dto.getMois(), dto.getAnnee());

            FactureLocataire facture;
            if (existante.isPresent()) {
                facture = existante.get();
                facture.setConsommationKwh(conso);
                facture.setConsommationTotaleMaison(consoTotale);
                facture.setPartPourcentage(partPct);
                facture.setMontantFacturePrincipale(dto.getMontantFacturePrincipale());
                facture.setMontantFcfa(montant);
                facture.setStatut(StatutFacture.GENEREE);
                facture.setDateGeneration(LocalDateTime.now());
            } else {
                facture = FactureLocataire.builder()
                        .sousCompteur(sc)
                        .mois(dto.getMois())
                        .annee(dto.getAnnee())
                        .consommationKwh(conso)
                        .consommationTotaleMaison(consoTotale)
                        .partPourcentage(partPct)
                        .montantFacturePrincipale(dto.getMontantFacturePrincipale())
                        .montantFcfa(montant)
                        .statut(StatutFacture.GENEREE)
                        .dateGeneration(LocalDateTime.now())
                        .build();
            }

            FactureLocataire saved = factureLocataireRepository.save(facture);
            facturesDTOs.add(buildFactureDTO(sc, conso, consoTotale,
                    dto.getMontantFacturePrincipale(), dto.getMois(), dto.getAnnee(),
                    saved.getId(), saved.getDateGeneration()));
        }

        log.info("Factures générées: maison={}, {}/{}, {} factures, total={}FCFA",
                dto.getMaisonId(), dto.getMois(), dto.getAnnee(),
                facturesDTOs.size(), dto.getMontantFacturePrincipale());

        return RepartitionResponseDTO.builder()
                .maisonId(maison.getId())
                .maisonNom(maison.getNom())
                .mois(dto.getMois())
                .annee(dto.getAnnee())
                .consommationTotale(consoTotale)
                .montantTotal(dto.getMontantFacturePrincipale())
                .factures(facturesDTOs)
                .statut(consoTotale > 0 ? "COMPLETE" : "PARTIELLE")
                .build();
    }

    // ─── Lecture des factures existantes ────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public RepartitionResponseDTO getFacturesByMaison(Long maisonId, Integer mois, Integer annee, User proprietaire) {
        Maison maison = verifierAccessMaison(maisonId, proprietaire);

        List<FactureLocataire> factures = factureLocataireRepository
                .findBySousCompteurMaisonIdAndMoisAndAnnee(maisonId, mois, annee);

        Double consoTotale = factures.stream()
                .mapToDouble(f -> f.getConsommationKwh() != null ? f.getConsommationKwh() : 0.0)
                .sum();
        Double montantTotal = factures.stream()
                .mapToDouble(f -> f.getMontantFcfa() != null ? f.getMontantFcfa() : 0.0)
                .sum();

        List<FactureLocataireResponseDTO> dtos = factures.stream()
                .map(this::toFactureDTO)
                .collect(Collectors.toList());

        return RepartitionResponseDTO.builder()
                .maisonId(maison.getId())
                .maisonNom(maison.getNom())
                .mois(mois)
                .annee(annee)
                .consommationTotale(consoTotale)
                .montantTotal(montantTotal)
                .factures(dtos)
                .statut(dtos.isEmpty() ? "AUCUNE_FACTURE" : "COMPLETE")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FactureLocataireResponseDTO> getFacturesByLocataire(User locataire) {
        return factureLocataireRepository.findBySousCompteurLocataireId(locataire.getId())
                .stream()
                .map(this::toFactureDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FactureLocataireResponseDTO getFactureById(Long factureId, User currentUser) {
        FactureLocataire facture = factureLocataireRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));

        boolean isLocataire = facture.getSousCompteur().getLocataire() != null
                && facture.getSousCompteur().getLocataire().getId().equals(currentUser.getId());
        boolean isProprietaire = facture.getSousCompteur().getMaison().getProprietaire().getId()
                .equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().getName() == RoleName.ADMIN;

        if (!isLocataire && !isProprietaire && !isAdmin) {
            throw new RuntimeException("Accès non autorisé à cette facture");
        }

        // Si le locataire accède → passe en TELECHARGEE
        if (isLocataire && facture.getStatut() == StatutFacture.GENEREE) {
            facture.setStatut(StatutFacture.TELECHARGEE);
            factureLocataireRepository.save(facture);
        }

        return toFactureDTO(facture);
    }

    // ─── Algorithme de répartition ──────────────────────────────────────────────
    // Part_i = (Conso_i / Conso_totale) × Montant_facture_principale

    private Map<Long, Double> calculerConsommations(List<SousCompteur> sousCompteurs,
                                                     LocalDateTime debut, LocalDateTime fin) {
        Map<Long, Double> map = new HashMap<>();
        for (SousCompteur sc : sousCompteurs) {
            Double conso = releveAdditionneusRepository.sumConsommationByPeriode(sc.getId(), debut, fin);
            map.put(sc.getId(), conso != null ? conso : 0.0);
        }
        return map;
    }

    private FactureLocataireResponseDTO buildFactureDTO(SousCompteur sc, Double conso, Double consoTotale,
                                                         Double montantTotal, Integer mois, Integer annee,
                                                         Long id, LocalDateTime dateGeneration) {
        Double partPct = consoTotale > 0 ? (conso / consoTotale) * 100 : 0.0;
        Double montant = consoTotale > 0 ? (conso / consoTotale) * montantTotal : 0.0;

        FactureLocataireResponseDTO.FactureLocataireResponseDTOBuilder builder =
                FactureLocataireResponseDTO.builder()
                        .id(id)
                        .mois(mois)
                        .annee(annee)
                        .nomMois(getNomMois(mois))
                        .sousCompteurId(sc.getId())
                        .sousCompteurReference(sc.getReference())
                        .consommationKwh(conso)
                        .consommationTotaleMaison(consoTotale)
                        .partPourcentage(partPct)
                        .montantFacturePrincipale(montantTotal)
                        .montantFcfa(montant)
                        .statut(id != null ? StatutFacture.GENEREE.name() : "CALCULE")
                        .dateGeneration(dateGeneration)
                        .pdfDisponible(false);

        if (sc.getLocataire() != null) {
            builder.locataireId(sc.getLocataire().getId())
                   .locataireNom(sc.getLocataire().getNomComplet())
                   .locataireEmail(sc.getLocataire().getEmail());
        }

        return builder.build();
    }

    private FactureLocataireResponseDTO toFactureDTO(FactureLocataire f) {
        SousCompteur sc = f.getSousCompteur();

        FactureLocataireResponseDTO.FactureLocataireResponseDTOBuilder builder =
                FactureLocataireResponseDTO.builder()
                        .id(f.getId())
                        .mois(f.getMois())
                        .annee(f.getAnnee())
                        .nomMois(getNomMois(f.getMois()))
                        .sousCompteurId(sc.getId())
                        .sousCompteurReference(sc.getReference())
                        .consommationKwh(f.getConsommationKwh())
                        .consommationTotaleMaison(f.getConsommationTotaleMaison())
                        .partPourcentage(f.getPartPourcentage())
                        .montantFacturePrincipale(f.getMontantFacturePrincipale())
                        .montantFcfa(f.getMontantFcfa())
                        .statut(f.getStatut().name())
                        .dateGeneration(f.getDateGeneration())
                        .pdfDisponible(f.getPdfPath() != null && !f.getPdfPath().isBlank());

        if (sc.getLocataire() != null) {
            builder.locataireId(sc.getLocataire().getId())
                   .locataireNom(sc.getLocataire().getNomComplet())
                   .locataireEmail(sc.getLocataire().getEmail());
        }

        return builder.build();
    }

    private Maison verifierAccessMaison(Long maisonId, User proprietaire) {
        return maisonRepository.findByIdAndProprietaireId(maisonId, proprietaire.getId())
                .orElseThrow(() -> new RuntimeException("Maison non trouvée ou accès non autorisé"));
    }

    private String getNomMois(Integer mois) {
        return switch (mois) {
            case 1 -> "Janvier";   case 2 -> "Février";   case 3 -> "Mars";
            case 4 -> "Avril";     case 5 -> "Mai";        case 6 -> "Juin";
            case 7 -> "Juillet";   case 8 -> "Août";       case 9 -> "Septembre";
            case 10 -> "Octobre";  case 11 -> "Novembre";  case 12 -> "Décembre";
            default -> "Mois inconnu";
        };
    }
}
