// ✅ CRÉÉ — SousCompteurController.java
package com.metereye.backend.controller;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.User;
import com.metereye.backend.service.SousCompteurService;
import com.metereye.backend.utils.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sous-compteurs")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SousCompteurController extends BaseController {

    private final SousCompteurService sousCompteurService;

    /** POST /api/sous-compteurs — Ajouter une additionneuse (PROPRIETAIRE) */
    @PostMapping
    public BaseResponse<SousCompteurResponseDTO> ajouterSousCompteur(
            @Valid @RequestBody SousCompteurRequestDTO dto) {
        try {
            User currentUser = getCurrentUser();
            SousCompteurResponseDTO response = sousCompteurService.ajouterSousCompteur(dto, currentUser);
            return BaseResponse.created(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    /** GET /api/sous-compteurs/maison/{maisonId} — Lister les additionneuses d'une maison (PROPRIETAIRE) */
    @GetMapping("/maison/{maisonId}")
    public BaseResponse<List<SousCompteurResponseDTO>> getSousCompteursByMaison(
            @PathVariable Long maisonId) {
        try {
            User currentUser = getCurrentUser();
            return BaseResponse.success(
                    sousCompteurService.getSousCompteursByMaison(maisonId, currentUser));
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    /** GET /api/sous-compteurs/{id} — Détail d'un sous-compteur (PROPRIETAIRE ou LOCATAIRE) */
    @GetMapping("/{id}")
    public BaseResponse<SousCompteurResponseDTO> getSousCompteurById(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            return BaseResponse.success(sousCompteurService.getSousCompteurById(id, currentUser));
        } catch (Exception e) {
            return BaseResponse.notFound(e.getMessage());
        }
    }

    /** GET /api/sous-compteurs/mon-additionneuse — Mon additionneuse (LOCATAIRE) */
    @GetMapping("/mon-additionneuse")
    public BaseResponse<SousCompteurResponseDTO> getMonAdditionneuse() {
        try {
            User currentUser = getCurrentUser();
            return BaseResponse.success(sousCompteurService.getSousCompteurDuLocataire(currentUser));
        } catch (Exception e) {
            return BaseResponse.notFound(e.getMessage());
        }
    }

    /** POST /api/sous-compteurs/locataires — Créer un compte locataire (PROPRIETAIRE) */
    @PostMapping("/locataires")
    public BaseResponse<CreerLocataireResponseDTO> creerLocataire(
            @Valid @RequestBody CreerLocataireRequestDTO dto) {
        try {
            User currentUser = getCurrentUser();
            CreerLocataireResponseDTO response = sousCompteurService.creerLocataire(dto, currentUser);
            return BaseResponse.created(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    /** GET /api/sous-compteurs/locataires/maison/{maisonId} — Locataires d'une maison (PROPRIETAIRE) */
    @GetMapping("/locataires/maison/{maisonId}")
    public BaseResponse<List<SousCompteurResponseDTO>> getLocatairesByMaison(
            @PathVariable Long maisonId) {
        try {
            User currentUser = getCurrentUser();
            return BaseResponse.success(
                    sousCompteurService.getLocatairesByMaison(maisonId, currentUser));
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    /** DELETE /api/sous-compteurs/locataires/{locataireId} — Désactiver un locataire (PROPRIETAIRE) */
    @DeleteMapping("/locataires/{locataireId}")
    public BaseResponse<String> desactiverLocataire(@PathVariable Long locataireId) {
        try {
            User currentUser = getCurrentUser();
            String message = sousCompteurService.desactiverLocataire(locataireId, currentUser);
            return BaseResponse.success(message, null);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    /** POST /api/sous-compteurs/{id}/releves — Ajouter un relevé (PROPRIETAIRE ou LOCATAIRE) */
    @PostMapping("/{id}/releves")
    public BaseResponse<ReleveAdditionneusResponseDTO> ajouterReleve(
            @PathVariable Long id,
            @RequestBody ReleveAdditionneusRequestDTO dto) {
        try {
            dto.setSousCompteurId(id); // id de path prioritaire sur le body
            User currentUser = getCurrentUser();
            ReleveAdditionneusResponseDTO response =
                    sousCompteurService.ajouterReleveAdditionneuse(dto, currentUser);
            return BaseResponse.created(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    /** GET /api/sous-compteurs/{id}/releves — Historique des relevés (PROPRIETAIRE ou LOCATAIRE) */
    @GetMapping("/{id}/releves")
    public BaseResponse<List<ReleveAdditionneusResponseDTO>> getHistoriqueReleves(
            @PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            return BaseResponse.success(
                    sousCompteurService.getHistoriqueAdditionneuse(id, currentUser));
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }
}
