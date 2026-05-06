// CompteurController.java
package com.metereye.backend.controller;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.User;
import com.metereye.backend.service.CompteurService;
import com.metereye.backend.utils.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/compteurs")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CompteurController extends BaseController {

    private final CompteurService compteurService;

    /**
     * Créer un nouveau compteur
     * POST /api/compteurs
     */
    @PostMapping
    public BaseResponse<CompteurResponseDTO> creerCompteur(@Valid @RequestBody CompteurRequestDTO request) {
        try {
            User currentUser = getCurrentUser();
            CompteurResponseDTO response = compteurService.creerCompteur(request, currentUser);
            return BaseResponse.created(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    /**
     * Récupérer tous les compteurs de l'utilisateur connecté
     * GET /api/compteurs
     */
    @GetMapping
    public BaseResponse<List<CompteurResponseDTO>> getMesCompteurs() {
        try {
            User currentUser = getCurrentUser();
            List<CompteurResponseDTO> compteurs = compteurService.getCompteursByUser(currentUser.getId());
            return BaseResponse.success(compteurs);
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    /**
     * Récupérer un compteur par son ID
     * GET /api/compteurs/{id}
     */
    @GetMapping("/{id}")
    public BaseResponse<CompteurResponseDTO> getCompteurById(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            CompteurResponseDTO compteur = compteurService.getCompteurById(id, currentUser);
            return BaseResponse.success(compteur);
        } catch (Exception e) {
            return BaseResponse.notFound(e.getMessage());
        }
    }

    /**
     * Désactiver un compteur
     * DELETE /api/compteurs/{id}
     */
    @DeleteMapping("/{id}")
    public BaseResponse<CompteurResponseDTO> desactiverCompteur(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            CompteurResponseDTO compteur = compteurService.desactiverCompteur(id, currentUser);
            return BaseResponse.success("Compteur désactivé avec succès", compteur);
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    /**
     * Ajouter un relevé manuel
     * POST /api/compteurs/releves
     */
    @PostMapping("/releves")
    public BaseResponse<ReleveResponseDTO> ajouterReleve(@Valid @RequestBody ReleveRequestDTO request) {
        try {
            User currentUser = getCurrentUser();
            ReleveResponseDTO response = compteurService.ajouterReleve(request, currentUser);
            return BaseResponse.created(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    /**
     * Récupérer l'historique des relevés d'un compteur
     * GET /api/compteurs/{id}/releves
     */
    @GetMapping("/{id}/releves")
    public BaseResponse<List<ReleveResponseDTO>> getHistoriqueReleves(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            User currentUser = getCurrentUser();
            List<ReleveResponseDTO> releves = compteurService.getHistoriqueReleves(id, startDate, endDate, currentUser);
            return BaseResponse.success(releves);
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    /**
     * Recharger un compteur Cash Power
     * POST /api/compteurs/recharge
     */
    @PostMapping("/recharge")
    public BaseResponse<CompteurResponseDTO> rechargerCompteur(@Valid @RequestBody RechargeRequestDTO request) {
        try {
            CompteurResponseDTO response = compteurService.rechargerCompteur(request);
            return BaseResponse.success("Recharge effectuée avec succès", response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    /**
     * Récupérer les statistiques de consommation
     * GET /api/compteurs/{id}/stats
     */
    @GetMapping("/{id}/stats")
    public BaseResponse<ConsommationStatsDTO> getStatistiques(
            @PathVariable Long id,
            @RequestParam(defaultValue = "month") String periode) {
        try {
            ConsommationStatsDTO stats = compteurService.getStatistiquesConsommation(id, periode);
            return BaseResponse.success(stats);
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    /**
     * Ajouter un relevé par OCR
     * POST /api/compteurs/{id}/ocr
     */
    @PostMapping("/{id}/ocr")
    public BaseResponse<ReleveResponseDTO> ajouterReleveOCR(
            @PathVariable Long id,
            @RequestBody String imageBase64) {
        try {
            User currentUser = getCurrentUser();
            ReleveResponseDTO response = compteurService.ajouterReleveParOCR(id, imageBase64, currentUser);
            return BaseResponse.created(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

}