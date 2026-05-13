// ✅ CRÉÉ — MaisonController.java
package com.metereye.backend.controller;

import com.metereye.backend.dto.MaisonRequestDTO;
import com.metereye.backend.dto.MaisonResponseDTO;
import com.metereye.backend.entity.User;
import com.metereye.backend.service.MaisonService;
import com.metereye.backend.utils.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maisons")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MaisonController extends BaseController {

    private final MaisonService maisonService;

    /** POST /api/maisons — Créer une maison */
    @PostMapping
    public BaseResponse<MaisonResponseDTO> creerMaison(
            @Valid @RequestBody MaisonRequestDTO dto) {
        try {
            User currentUser = getCurrentUser();
            MaisonResponseDTO response = maisonService.creerMaison(dto, currentUser);
            return BaseResponse.created(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    /** GET /api/maisons — Lister mes maisons */
    @GetMapping
    public BaseResponse<List<MaisonResponseDTO>> getMesMaisons() {
        try {
            User currentUser = getCurrentUser();
            return BaseResponse.success(maisonService.getMesMaisons(currentUser));
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    /** GET /api/maisons/{id} — Détail d'une maison */
    @GetMapping("/{id}")
    public BaseResponse<MaisonResponseDTO> getMaisonById(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            return BaseResponse.success(maisonService.getMaisonById(id, currentUser));
        } catch (Exception e) {
            return BaseResponse.notFound(e.getMessage());
        }
    }

    /** PUT /api/maisons/{id} — Modifier une maison */
    @PutMapping("/{id}")
    public BaseResponse<MaisonResponseDTO> modifierMaison(
            @PathVariable Long id,
            @Valid @RequestBody MaisonRequestDTO dto) {
        try {
            User currentUser = getCurrentUser();
            MaisonResponseDTO response = maisonService.modifierMaison(id, dto, currentUser);
            return BaseResponse.success("Maison modifiée avec succès", response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    /** DELETE /api/maisons/{id} — Désactiver une maison (soft-delete) */
    @DeleteMapping("/{id}")
    public BaseResponse<String> desactiverMaison(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            String message = maisonService.desactiverMaison(id, currentUser);
            return BaseResponse.success(message, null);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    /**
     * POST /api/maisons/{id}/compteur-principal
     * Body : { "compteurId": 42 }
     */
    @PostMapping("/{id}/compteur-principal")
    public BaseResponse<MaisonResponseDTO> associerCompteurPrincipal(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        try {
            Long compteurId = body.get("compteurId");
            if (compteurId == null) {
                return BaseResponse.badRequest("Le champ 'compteurId' est obligatoire");
            }
            User currentUser = getCurrentUser();
            MaisonResponseDTO response = maisonService.associerCompteurPrincipal(id, compteurId, currentUser);
            return BaseResponse.success("Compteur associé avec succès", response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }
}
