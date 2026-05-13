// ✅ CRÉÉ — SousCompteurServiceImpl.java
package com.metereye.backend.service.impl;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.*;
import com.metereye.backend.enums.RoleName;
import com.metereye.backend.enums.SourceReleve;
import com.metereye.backend.repository.*;
import com.metereye.backend.service.SousCompteurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SousCompteurServiceImpl implements SousCompteurService {

    private final SousCompteurRepository sousCompteurRepository;
    private final MaisonRepository maisonRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ReleveAdditionneusRepository releveAdditionneusRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SousCompteurResponseDTO ajouterSousCompteur(SousCompteurRequestDTO dto, User proprietaire) {
        maisonRepository.findByIdAndProprietaireId(dto.getMaisonId(), proprietaire.getId())
                .orElseThrow(() -> new RuntimeException("Maison non trouvée ou accès non autorisé"));

        if (sousCompteurRepository.existsByReferenceAndMaisonId(dto.getReference(), dto.getMaisonId())) {
            throw new RuntimeException("Un sous-compteur avec cette référence existe déjà dans cette maison");
        }

        Maison maison = maisonRepository.findById(dto.getMaisonId())
                .orElseThrow(() -> new RuntimeException("Maison non trouvée"));

        SousCompteur sousCompteur = SousCompteur.builder()
                .reference(dto.getReference())
                .descriptionLogement(dto.getDescriptionLogement())
                .valeurInitiale(dto.getValeurInitiale())
                .valeurActuelle(dto.getValeurInitiale())
                .maison(maison)
                .actif(true)
                .build();

        SousCompteur saved = sousCompteurRepository.save(sousCompteur);
        log.info("Sous-compteur créé: {} pour maison {} par {}", dto.getReference(), dto.getMaisonId(), proprietaire.getEmail());
        return toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SousCompteurResponseDTO> getSousCompteursByMaison(Long maisonId, User proprietaire) {
        maisonRepository.findByIdAndProprietaireId(maisonId, proprietaire.getId())
                .orElseThrow(() -> new RuntimeException("Maison non trouvée ou accès non autorisé"));

        return sousCompteurRepository.findByMaisonIdAndActifTrue(maisonId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CreerLocataireResponseDTO creerLocataire(CreerLocataireRequestDTO dto, User proprietaire) {
        SousCompteur sousCompteur = sousCompteurRepository.findById(dto.getSousCompteurId())
                .orElseThrow(() -> new RuntimeException("Sous-compteur non trouvé"));

        maisonRepository.findByIdAndProprietaireId(sousCompteur.getMaison().getId(), proprietaire.getId())
                .orElseThrow(() -> new RuntimeException("Accès non autorisé : ce sous-compteur n'appartient pas à vos maisons"));

        if (sousCompteur.getLocataire() != null && sousCompteur.getLocataire().isActif()) {
            throw new RuntimeException("Ce sous-compteur a déjà un locataire actif");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }

        String motDePasseTemp = (dto.getMotDePasseTemporaire() != null && !dto.getMotDePasseTemporaire().isBlank())
                ? dto.getMotDePasseTemporaire()
                : "METER" + String.format("%04d", new Random().nextInt(10000));

        Role roleLocataire = roleRepository.findByName(RoleName.LOCATAIRE)
                .orElseThrow(() -> new RuntimeException("Rôle LOCATAIRE non trouvé en base"));

        User locataire = User.builder()
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .motDePasse(passwordEncoder.encode(motDePasseTemp))
                .telephone(dto.getTelephone())
                .role(roleLocataire)
                .actif(true)
                .build();

        User savedLocataire = userRepository.save(locataire);

        sousCompteur.setLocataire(savedLocataire);
        sousCompteurRepository.save(sousCompteur);

        log.info("Locataire créé: {} associé au sous-compteur {} par {}",
                dto.getEmail(), sousCompteur.getReference(), proprietaire.getEmail());

        return CreerLocataireResponseDTO.builder()
                .locataireId(savedLocataire.getId())
                .nom(savedLocataire.getNom())
                .prenom(savedLocataire.getPrenom())
                .email(savedLocataire.getEmail())
                .motDePasseTemporaire(motDePasseTemp)
                .identifiantConnexion(savedLocataire.getEmail())
                .sousCompteurId(sousCompteur.getId())
                .sousCompteurReference(sousCompteur.getReference())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SousCompteurResponseDTO> getLocatairesByMaison(Long maisonId, User proprietaire) {
        maisonRepository.findByIdAndProprietaireId(maisonId, proprietaire.getId())
                .orElseThrow(() -> new RuntimeException("Maison non trouvée ou accès non autorisé"));

        return sousCompteurRepository.findByMaisonIdAndActifTrue(maisonId)
                .stream()
                .filter(sc -> sc.getLocataire() != null)
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String desactiverLocataire(Long locataireId, User proprietaire) {
        SousCompteur sousCompteur = sousCompteurRepository.findByLocataireId(locataireId)
                .orElseThrow(() -> new RuntimeException("Locataire non trouvé dans vos maisons"));

        maisonRepository.findByIdAndProprietaireId(sousCompteur.getMaison().getId(), proprietaire.getId())
                .orElseThrow(() -> new RuntimeException("Accès non autorisé"));

        User locataire = sousCompteur.getLocataire();
        locataire.setActif(false);
        userRepository.save(locataire);

        sousCompteur.setLocataire(null);
        sousCompteurRepository.save(sousCompteur);

        log.info("Locataire id={} désactivé par {}", locataireId, proprietaire.getEmail());
        return "Locataire désactivé avec succès";
    }

    @Override
    public ReleveAdditionneusResponseDTO ajouterReleveAdditionneuse(ReleveAdditionneusRequestDTO dto, User currentUser) {
        SousCompteur sousCompteur = sousCompteurRepository.findById(dto.getSousCompteurId())
                .orElseThrow(() -> new RuntimeException("Sous-compteur non trouvé"));

        verifierDroitAccesSousCompteur(sousCompteur, currentUser);

        Optional<ReleveAdditionneuse> dernierReleve = releveAdditionneusRepository
                .findTopBySousCompteurIdOrderByDateReleveDesc(sousCompteur.getId());

        Double consommation = 0.0;
        if (dernierReleve.isPresent()) {
            consommation = dto.getValeur() - dernierReleve.get().getValeur();
            if (consommation < 0) {
                throw new RuntimeException(
                        "Valeur inférieure au relevé précédent (" + dernierReleve.get().getValeur() + ")");
            }
        }

        SourceReleve sourceEnum;
        try {
            sourceEnum = SourceReleve.valueOf(dto.getSource().toUpperCase());
        } catch (IllegalArgumentException e) {
            sourceEnum = SourceReleve.MANUEL;
        }

        ReleveAdditionneuse releve = ReleveAdditionneuse.builder()
                .sousCompteur(sousCompteur)
                .valeur(dto.getValeur())
                .consommationCalculee(consommation)
                .source(sourceEnum)
                .commentaire(dto.getCommentaire())
                .dateReleve(dto.getDateReleve() != null ? dto.getDateReleve() : LocalDateTime.now())
                .build();

        ReleveAdditionneuse saved = releveAdditionneusRepository.save(releve);

        sousCompteur.setValeurActuelle(dto.getValeur());
        sousCompteurRepository.save(sousCompteur);

        log.info("Relevé additionneuse: sc={}, valeur={}, conso={}",
                sousCompteur.getReference(), dto.getValeur(), consommation);
        return toReleveResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReleveAdditionneusResponseDTO> getHistoriqueAdditionneuse(Long sousCompteurId, User currentUser) {
        SousCompteur sousCompteur = sousCompteurRepository.findById(sousCompteurId)
                .orElseThrow(() -> new RuntimeException("Sous-compteur non trouvé"));

        verifierDroitAccesSousCompteur(sousCompteur, currentUser);

        return releveAdditionneusRepository.findBySousCompteurId(sousCompteurId)
                .stream()
                .map(this::toReleveResponseDTO)
                .collect(Collectors.toList());
    }

    // 🔄 AJOUTÉ — getSousCompteurById

    @Override
    @Transactional(readOnly = true)
    public SousCompteurResponseDTO getSousCompteurById(Long id, User currentUser) {
        SousCompteur sousCompteur = sousCompteurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sous-compteur non trouvé"));
        verifierDroitAccesSousCompteur(sousCompteur, currentUser);
        return toResponseDTO(sousCompteur);
    }

    // 🔄 AJOUTÉ — getSousCompteurDuLocataire

    @Override
    @Transactional(readOnly = true)
    public SousCompteurResponseDTO getSousCompteurDuLocataire(User locataire) {
        SousCompteur sousCompteur = sousCompteurRepository.findByLocataireId(locataire.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Aucun sous-compteur associé à votre compte locataire"));
        return toResponseDTO(sousCompteur);
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    private void verifierDroitAccesSousCompteur(SousCompteur sousCompteur, User user) {
        boolean isProprietaire = sousCompteur.getMaison().getProprietaire().getId().equals(user.getId());
        boolean isLocataire = sousCompteur.getLocataire() != null
                && sousCompteur.getLocataire().getId().equals(user.getId());
        boolean isAdmin = user.getRole().getName() == RoleName.ADMIN;
        if (!isProprietaire && !isLocataire && !isAdmin) {
            throw new RuntimeException("Accès non autorisé à ce sous-compteur");
        }
    }

    private SousCompteurResponseDTO toResponseDTO(SousCompteur sc) {
        Optional<ReleveAdditionneuse> dernierReleve = releveAdditionneusRepository
                .findTopBySousCompteurIdOrderByDateReleveDesc(sc.getId());

        SousCompteurResponseDTO.SousCompteurResponseDTOBuilder builder = SousCompteurResponseDTO.builder()
                .id(sc.getId())
                .reference(sc.getReference())
                .descriptionLogement(sc.getDescriptionLogement())
                .valeurInitiale(sc.getValeurInitiale())
                .valeurActuelle(sc.getValeurActuelle())
                .maisonId(sc.getMaison().getId())
                .maisonNom(sc.getMaison().getNom())
                .actif(sc.getActif())
                .dateCreation(sc.getDateCreation());

        if (sc.getLocataire() != null) {
            builder.locataireId(sc.getLocataire().getId())
                   .locataireNom(sc.getLocataire().getNomComplet())
                   .locataireEmail(sc.getLocataire().getEmail());
        }

        dernierReleve.ifPresent(r -> builder
                .dernierReleve(r.getValeur())
                .dateDernierReleve(r.getDateReleve()));

        return builder.build();
    }

    private ReleveAdditionneusResponseDTO toReleveResponseDTO(ReleveAdditionneuse r) {
        return ReleveAdditionneusResponseDTO.builder()
                .id(r.getId())
                .sousCompteurId(r.getSousCompteur().getId())
                .sousCompteurReference(r.getSousCompteur().getReference())
                .valeur(r.getValeur())
                .consommationCalculee(r.getConsommationCalculee())
                .source(r.getSource().name())
                .dateReleve(r.getDateReleve())
                .commentaire(r.getCommentaire())
                .build();
    }
}
