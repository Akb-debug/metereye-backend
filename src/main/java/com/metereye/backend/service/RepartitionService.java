// ✅ CRÉÉ — RepartitionService.java
package com.metereye.backend.service;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.User;

import java.util.List;

public interface RepartitionService {

    // Calcule la répartition SANS sauvegarder (prévisualisation)
    RepartitionResponseDTO calculerRepartition(Long maisonId, Integer mois, Integer annee,
                                               Double montantFacturePrincipale, User proprietaire);

    // Génère ET sauvegarde les factures en base
    RepartitionResponseDTO genererFactures(GenererFacturesRequestDTO dto, User proprietaire);

    // Récupère les factures déjà générées pour une maison/mois/année
    RepartitionResponseDTO getFacturesByMaison(Long maisonId, Integer mois, Integer annee, User proprietaire);

    // Factures du locataire connecté
    List<FactureLocataireResponseDTO> getFacturesByLocataire(User locataire);

    // Détail d'une facture
    FactureLocataireResponseDTO getFactureById(Long factureId, User currentUser);
}
