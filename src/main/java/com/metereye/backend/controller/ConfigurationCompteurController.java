// ConfigurationCompteurController.java
package com.metereye.backend.controller;

import com.metereye.backend.dto.ConfigurationLectureDTO;
import com.metereye.backend.dto.ReinitialisationCompteurDTO;
import com.metereye.backend.entity.Compteur;
import com.metereye.backend.entity.User;
import com.metereye.backend.enums.ModeLectureCompteur;
import com.metereye.backend.repository.CompteurRepository;
import com.metereye.backend.service.CompteurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compteurs")
@RequiredArgsConstructor
@Tag(name = "Configuration Compteurs", description = "Gestion de la configuration des compteurs")
public class ConfigurationCompteurController {

    private final CompteurService compteurService;
    private final CompteurRepository compteurRepository;

    @PostMapping("/{id}/mode-lecture")
    @Operation(summary = "Configurer le mode de lecture d'un compteur")
    public ResponseEntity<String> configurerModeLecture(
            @PathVariable Long id,
            @Valid @RequestBody ConfigurationLectureDTO config,
            @AuthenticationPrincipal User user) {
        
        Compteur compteur = compteurService.configurerModeLecture(id, config.getModeLecture(), user);
        
        return ResponseEntity.ok(String.format(
                "Compteur %s configuré en mode %s", 
                compteur.getReference(), 
                config.getModeLecture()
        ));
    }

    @PostMapping("/{id}/reinitialiser")
    @Operation(summary = "Réinitialiser un compteur classique")
    public ResponseEntity<String> reinitialiserCompteur(
            @PathVariable Long id,
            @Valid @RequestBody ReinitialisationCompteurDTO reinitialisation,
            @AuthenticationPrincipal User user) {
        
        Compteur compteur = compteurService.reinitialiserCompteur(id, reinitialisation.getMotif(), user);
        
        return ResponseEntity.ok(String.format(
                "Compteur %s réinitialisé: %s", 
                compteur.getReference(), 
                reinitialisation.getMotif()
        ));
    }

    @GetMapping("/{id}/statut-configuration")
    @Operation(summary = "Vérifier le statut de configuration d'un compteur")
    public ResponseEntity<Object> getStatutConfiguration(@PathVariable Long id) {

        Compteur compteur = compteurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));
        
        return ResponseEntity.ok(new StatutConfigurationResponse(
                compteur.getReference(),
                compteur.getStatut(),
                compteur.getModeLectureConfigure(),
                compteur.estConfigurePourLecture()
        ));
    }

    // DTO de réponse pour le statut
    public record StatutConfigurationResponse(
            String reference,
            com.metereye.backend.enums.StatutCompteur statut,
            ModeLectureCompteur modeLectureConfigure,
            boolean configurePourLecture
    ) {}
}
