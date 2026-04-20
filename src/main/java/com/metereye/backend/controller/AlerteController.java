// AlerteController.java
package com.metereye.backend.controller;

import com.metereye.backend.dto.AlerteResponseDTO;
import com.metereye.backend.entity.User;
import com.metereye.backend.repository.UserRepository;
import com.metereye.backend.service.AlerteService;
import com.metereye.backend.utils.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteService alerteService;
    private final UserRepository userRepository;

    /**
     * Récupérer toutes les alertes de l'utilisateur
     * GET /api/alertes
     */
    @GetMapping
    public BaseResponse<List<AlerteResponseDTO>> getMesAlertes() {
        try {
            User currentUser = getCurrentUser();
            List<AlerteResponseDTO> alertes = alerteService.getAlertesByUser(currentUser);
            return BaseResponse.success(alertes);
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    /**
     * Récupérer les alertes non lues
     * GET /api/alertes/non-lues
     */
    @GetMapping("/non-lues")
    public BaseResponse<List<AlerteResponseDTO>> getAlertesNonLues() {
        try {
            User currentUser = getCurrentUser();
            List<AlerteResponseDTO> alertes = alerteService.getAlertesNonLues(currentUser);
            return BaseResponse.success(alertes);
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    /**
     * Marquer une alerte comme lue
     * PUT /api/alertes/{id}/lire
     */
    @PutMapping("/{id}/lire")
    public BaseResponse<Void> marquerCommeLue(@PathVariable Long id) {
        try {
            alerteService.marquerCommeLue(id);
            return BaseResponse.success("Alerte marquée comme lue", null);
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}