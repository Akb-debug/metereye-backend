package com.metereye.backend.controller;

import com.metereye.backend.dto.AlerteResponseDTO;
import com.metereye.backend.entity.User;
import com.metereye.backend.service.AlerteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertes")
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteService alerteService;

    @GetMapping
    public List<AlerteResponseDTO> getMesAlertes(
            @AuthenticationPrincipal User user
    ) {
        return alerteService.getAlertesByUser(user);
    }

    @GetMapping("/non-lues")
    public List<AlerteResponseDTO> getMesAlertesNonLues(
            @AuthenticationPrincipal User user
    ) {
        return alerteService.getAlertesNonLues(user);
    }

    @PatchMapping("/{id}/lue")
    public void marquerCommeLue(@PathVariable Long id) {
        alerteService.marquerCommeLue(id);
    }

    @PatchMapping("/tout-lu")
    public void marquerToutesCommeLues(
            @AuthenticationPrincipal User user
    ) {
        alerteService.marquerToutesCommeLues(user);
    }
}