// ✅ CRÉÉ — RepartitionController.java
package com.metereye.backend.controller;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.User;
import com.metereye.backend.service.RepartitionService;
import com.metereye.backend.utils.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repartition")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RepartitionController extends BaseController {

    private final RepartitionService repartitionService;

    /**
     * GET /api/repartition/apercu?maisonId=&mois=&annee=&montantFacturePrincipale=
     * Calcule la répartition SANS sauvegarder — prévisualisation (PROPRIETAIRE)
     */
    @GetMapping("/apercu")
    public BaseResponse<RepartitionResponseDTO> apercu(
            @RequestParam Long maisonId,
            @RequestParam Integer mois,
            @RequestParam Integer annee,
            @RequestParam(defaultValue = "0") Double montantFacturePrincipale) {
        try {
            User currentUser = getCurrentUser();
            RepartitionResponseDTO response = repartitionService.calculerRepartition(
                    maisonId, mois, annee, montantFacturePrincipale, currentUser);
            return BaseResponse.success(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    /**
     * POST /api/repartition/generer — Génère ET sauvegarde les factures (PROPRIETAIRE)
     * Body : GenererFacturesRequestDTO
     */
    @PostMapping("/generer")
    public BaseResponse<RepartitionResponseDTO> genererFactures(
            @Valid @RequestBody GenererFacturesRequestDTO dto) {
        try {
            User currentUser = getCurrentUser();
            RepartitionResponseDTO response = repartitionService.genererFactures(dto, currentUser);
            return BaseResponse.created(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    /**
     * GET /api/repartition/maison/{maisonId}?mois=&annee=
     * Factures déjà générées pour une maison/mois/année (PROPRIETAIRE)
     */
    @GetMapping("/maison/{maisonId}")
    public BaseResponse<RepartitionResponseDTO> getFacturesByMaison(
            @PathVariable Long maisonId,
            @RequestParam Integer mois,
            @RequestParam Integer annee) {
        try {
            User currentUser = getCurrentUser();
            RepartitionResponseDTO response =
                    repartitionService.getFacturesByMaison(maisonId, mois, annee, currentUser);
            return BaseResponse.success(response);
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    /**
     * GET /api/repartition/mes-factures — Toutes mes factures (LOCATAIRE)
     */
    @GetMapping("/mes-factures")
    public BaseResponse<List<FactureLocataireResponseDTO>> getMesFactures() {
        try {
            User currentUser = getCurrentUser();
            return BaseResponse.success(repartitionService.getFacturesByLocataire(currentUser));
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    /**
     * GET /api/repartition/factures/{factureId} — Détail d'une facture (PROPRIETAIRE ou LOCATAIRE)
     */
    @GetMapping("/factures/{factureId}")
    public BaseResponse<FactureLocataireResponseDTO> getFactureById(@PathVariable Long factureId) {
        try {
            User currentUser = getCurrentUser();
            return BaseResponse.success(repartitionService.getFactureById(factureId, currentUser));
        } catch (Exception e) {
            return BaseResponse.notFound(e.getMessage());
        }
    }
}
