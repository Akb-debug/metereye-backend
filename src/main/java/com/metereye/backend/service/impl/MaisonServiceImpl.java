// ✅ CRÉÉ — MaisonServiceImpl.java
package com.metereye.backend.service.impl;

import com.metereye.backend.dto.MaisonRequestDTO;
import com.metereye.backend.dto.MaisonResponseDTO;
import com.metereye.backend.entity.Compteur;
import com.metereye.backend.entity.Maison;
import com.metereye.backend.entity.User;
import com.metereye.backend.enums.RoleName;
import com.metereye.backend.repository.CompteurRepository;
import com.metereye.backend.repository.MaisonRepository;
import com.metereye.backend.service.MaisonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MaisonServiceImpl implements MaisonService {

    private final MaisonRepository maisonRepository;
    private final CompteurRepository compteurRepository;

    @Override
    public MaisonResponseDTO creerMaison(MaisonRequestDTO dto, User proprietaire) {
        verifierRoleProprietaire(proprietaire);

        Maison.MaisonBuilder builder = Maison.builder()
                .nom(dto.getNom())
                .adresse(dto.getAdresse())
                .description(dto.getDescription())
                .proprietaire(proprietaire)
                .actif(true);

        if (dto.getCompteurPrincipalId() != null) {
            Compteur compteur = compteurRepository.findById(dto.getCompteurPrincipalId())
                    .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));
            verifierOwnershipCompteur(compteur, proprietaire);
            builder.compteurPrincipal(compteur);
        }

        Maison maison = maisonRepository.save(builder.build());
        log.info("Maison créée: {} par {}", maison.getNom(), proprietaire.getEmail());
        return toResponseDTO(maison);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaisonResponseDTO> getMesMaisons(User proprietaire) {
        return maisonRepository.findByProprietaireIdAndActifTrue(proprietaire.getId())
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MaisonResponseDTO getMaisonById(Long maisonId, User proprietaire) {
        Maison maison = maisonRepository.findByIdAndProprietaireId(maisonId, proprietaire.getId())
                .orElseThrow(() -> new RuntimeException("Maison non trouvée ou accès non autorisé"));
        return toResponseDTO(maison);
    }

    @Override
    public MaisonResponseDTO modifierMaison(Long maisonId, MaisonRequestDTO dto, User proprietaire) {
        Maison maison = maisonRepository.findByIdAndProprietaireId(maisonId, proprietaire.getId())
                .orElseThrow(() -> new RuntimeException("Maison non trouvée ou accès non autorisé"));

        maison.setNom(dto.getNom());
        maison.setAdresse(dto.getAdresse());
        maison.setDescription(dto.getDescription());

        if (dto.getCompteurPrincipalId() != null) {
            Compteur compteur = compteurRepository.findById(dto.getCompteurPrincipalId())
                    .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));
            verifierOwnershipCompteur(compteur, proprietaire);
            maison.setCompteurPrincipal(compteur);
        }

        log.info("Maison modifiée: id={} par {}", maisonId, proprietaire.getEmail());
        return toResponseDTO(maisonRepository.save(maison));
    }

    @Override
    public String desactiverMaison(Long maisonId, User proprietaire) {
        Maison maison = maisonRepository.findByIdAndProprietaireId(maisonId, proprietaire.getId())
                .orElseThrow(() -> new RuntimeException("Maison non trouvée ou accès non autorisé"));

        maison.setActif(false);
        maison.getSousCompteurs().forEach(sc -> sc.setActif(false));
        maisonRepository.save(maison);

        log.info("Maison désactivée: id={} par {}", maisonId, proprietaire.getEmail());
        return "Maison désactivée avec succès";
    }

    @Override
    public MaisonResponseDTO associerCompteurPrincipal(Long maisonId, Long compteurId, User proprietaire) {
        Maison maison = maisonRepository.findByIdAndProprietaireId(maisonId, proprietaire.getId())
                .orElseThrow(() -> new RuntimeException("Maison non trouvée ou accès non autorisé"));

        Compteur compteur = compteurRepository.findById(compteurId)
                .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));
        verifierOwnershipCompteur(compteur, proprietaire);

        maison.setCompteurPrincipal(compteur);
        log.info("Compteur {} associé à maison {} par {}", compteurId, maisonId, proprietaire.getEmail());
        return toResponseDTO(maisonRepository.save(maison));
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    private void verifierRoleProprietaire(User user) {
        RoleName role = user.getRole().getName();
        if (role != RoleName.PROPRIETAIRE && role != RoleName.ADMIN) {
            throw new RuntimeException("Seul un propriétaire peut effectuer cette action");
        }
    }

    private void verifierOwnershipCompteur(Compteur compteur, User proprietaire) {
        if (!compteur.getProprietaire().getId().equals(proprietaire.getId())) {
            throw new RuntimeException("Ce compteur n'appartient pas au propriétaire");
        }
    }

    private MaisonResponseDTO toResponseDTO(Maison maison) {
        long nbLocataires = maison.getSousCompteurs().stream()
                .filter(sc -> Boolean.TRUE.equals(sc.getActif()) && sc.getLocataire() != null)
                .count();

        MaisonResponseDTO.MaisonResponseDTOBuilder builder = MaisonResponseDTO.builder()
                .id(maison.getId())
                .nom(maison.getNom())
                .adresse(maison.getAdresse())
                .description(maison.getDescription())
                .proprietaireId(maison.getProprietaire().getId())
                .proprietaireNom(maison.getProprietaire().getNomComplet())
                .nombreLocataires((int) nbLocataires)
                .actif(maison.getActif())
                .dateCreation(maison.getDateCreation());

        if (maison.getCompteurPrincipal() != null) {
            builder.compteurPrincipalId(maison.getCompteurPrincipal().getId())
                   .compteurPrincipalReference(maison.getCompteurPrincipal().getReference())
                   .typeCompteur(maison.getCompteurPrincipal().getTypeCompteur().name());
        }

        return builder.build();
    }
}
