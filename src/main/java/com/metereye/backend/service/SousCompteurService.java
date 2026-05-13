// ✅ CRÉÉ — SousCompteurService.java
package com.metereye.backend.service;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.User;

import java.util.List;

public interface SousCompteurService {

    SousCompteurResponseDTO ajouterSousCompteur(SousCompteurRequestDTO dto, User proprietaire);

    List<SousCompteurResponseDTO> getSousCompteursByMaison(Long maisonId, User proprietaire);

    CreerLocataireResponseDTO creerLocataire(CreerLocataireRequestDTO dto, User proprietaire);

    List<SousCompteurResponseDTO> getLocatairesByMaison(Long maisonId, User proprietaire);

    String desactiverLocataire(Long locataireId, User proprietaire);

    ReleveAdditionneusResponseDTO ajouterReleveAdditionneuse(ReleveAdditionneusRequestDTO dto, User currentUser);

    List<ReleveAdditionneusResponseDTO> getHistoriqueAdditionneuse(Long sousCompteurId, User currentUser);

    // 🔄 AJOUTÉ — pour l'endpoint GET /api/sous-compteurs/{id} (propriétaire ou locataire)
    SousCompteurResponseDTO getSousCompteurById(Long id, User currentUser);

    // 🔄 AJOUTÉ — pour l'endpoint GET /api/sous-compteurs/mon-additionneuse (locataire)
    SousCompteurResponseDTO getSousCompteurDuLocataire(User locataire);
}
