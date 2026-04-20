// CompteurService.java
package com.metereye.backend.service;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.Compteur;
import com.metereye.backend.entity.User;
import com.metereye.backend.enums.ModeLectureCompteur;

import java.time.LocalDateTime;
import java.util.List;

public interface CompteurService {

    // Gestion des compteurs
    CompteurResponseDTO creerCompteur(CompteurRequestDTO request, User proprietaire);
    CompteurResponseDTO getCompteurById(Long id);
    List<CompteurResponseDTO> getCompteursByUser(Long userId);
    List<CompteurResponseDTO> getAllCompteurs();
    CompteurResponseDTO desactiverCompteur(Long id);

    // Gestion des relevés
    ReleveResponseDTO ajouterReleve(ReleveRequestDTO request, User user);
    List<ReleveResponseDTO> getHistoriqueReleves(Long compteurId, LocalDateTime startDate, LocalDateTime endDate);
    Double calculerConsommation(Long compteurId, LocalDateTime startDate, LocalDateTime endDate);

    // Statistiques
    ConsommationStatsDTO getStatistiquesConsommation(Long compteurId, String periode);

    // Recharge Cash Power
    CompteurResponseDTO rechargerCompteur(RechargeRequestDTO request);

    // OCR
    ReleveResponseDTO ajouterReleveParOCR(Long compteurId, String imageBase64, User user);

    // Nouvelles méthodes pour la configuration
    Compteur configurerModeLecture(Long compteurId, ModeLectureCompteur modeLecture, User user);

    Compteur reinitialiserCompteur(Long compteurId, String motif, User user);
}