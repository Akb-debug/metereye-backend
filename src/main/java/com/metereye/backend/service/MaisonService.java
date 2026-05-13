// ✅ CRÉÉ — MaisonService.java
package com.metereye.backend.service;

import com.metereye.backend.dto.MaisonRequestDTO;
import com.metereye.backend.dto.MaisonResponseDTO;
import com.metereye.backend.entity.User;

import java.util.List;

public interface MaisonService {

    MaisonResponseDTO creerMaison(MaisonRequestDTO dto, User proprietaire);

    List<MaisonResponseDTO> getMesMaisons(User proprietaire);

    MaisonResponseDTO getMaisonById(Long maisonId, User proprietaire);

    MaisonResponseDTO modifierMaison(Long maisonId, MaisonRequestDTO dto, User proprietaire);

    String desactiverMaison(Long maisonId, User proprietaire);

    MaisonResponseDTO associerCompteurPrincipal(Long maisonId, Long compteurId, User proprietaire);
}
