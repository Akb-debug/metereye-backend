// BluetoothController.java
package com.metereye.backend.controller;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.ModuleDevice;
import com.metereye.backend.entity.User;
import com.metereye.backend.service.ModuleDeviceService;
import com.metereye.backend.utils.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bluetooth")
@RequiredArgsConstructor
@Tag(name = "Bluetooth Configuration", description = "Configuration des modules via Bluetooth")
public class BluetoothController extends BaseController {

    private final ModuleDeviceService moduleDeviceService;

    @PostMapping("/scan")
    @Operation(summary = "Scanner un module Bluetooth", description = "Enregistre un module détecté via Bluetooth")
    public BaseResponse<ModuleDeviceResponseDTO> scannerModuleBluetooth(
            @Valid @RequestBody BluetoothScanRequest request) {

        try {
            User user = getCurrentUser();

            request.setUserId(user.getId());

            ModuleDevice module = moduleDeviceService.scannerModuleBluetooth(request);
            ModuleDeviceResponseDTO response = convertToDTO(module);

            return BaseResponse.created(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/configure")
    @Operation(summary = "Configurer un module Bluetooth", description = "Configure et associe un module à un compteur")
    public BaseResponse<ModuleDeviceResponseDTO> configurerModuleBluetooth(
            @Valid @RequestBody BluetoothConfigurationRequest request) {

        try {
            User user = getCurrentUser();

            ModuleDevice module = moduleDeviceService.configurerModuleBluetooth(request, user.getId());
            ModuleDeviceResponseDTO response = convertToDTO(module);

            return BaseResponse.success("Module configuré avec succès", response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/available")
    @Operation(summary = "Modules disponibles", description = "Liste les modules non configurés de l'utilisateur")
    public BaseResponse<List<ModuleDeviceResponseDTO>> getModulesDisponibles() {

        try {
            User user = getCurrentUser();

            List<ModuleDevice> modules = moduleDeviceService.getModulesUtilisateur(user.getId());

            List<ModuleDeviceResponseDTO> response = modules.stream()
                    .filter(module -> Boolean.FALSE.equals(module.getConfigured()))
                    .map(this::convertToDTO)
                    .toList();

            return BaseResponse.success(response);
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/compatibility")
    @Operation(summary = "Vérifier compatibilité", description = "Vérifie si un module est compatible avec un compteur")
    public BaseResponse<CompatibilityResponse> verifierCompatibilite(
            @Valid @RequestBody CompatibilityRequest request) {

        try {
            boolean compatible = moduleDeviceService.verifierCompatibilite(
                    request.getBluetoothAddress(),
                    request.getCompteurId()
            );

            CompatibilityResponse response = new CompatibilityResponse(
                    request.getBluetoothAddress(),
                    request.getCompteurId(),
                    compatible,
                    compatible ? "Module compatible avec le compteur" : "Module non compatible"
            );

            return BaseResponse.success(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/{bluetoothAddress}")
    @Operation(summary = "Informations module", description = "Récupère les détails d'un module par adresse Bluetooth")
    public BaseResponse<ModuleDeviceResponseDTO> getModuleParAdresseBluetooth(
            @PathVariable String bluetoothAddress) {

        try {
            User user = getCurrentUser();

            ModuleDevice module = moduleDeviceService.getModuleParAdresseBluetooth(
                    bluetoothAddress,
                    user.getId()
            );

            ModuleDeviceResponseDTO response = convertToDTO(module);

            return BaseResponse.success(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/direct-configure")
    @Operation(summary = "Configuration directe", description = "Configure un module directement via adresse Bluetooth")
    public BaseResponse<ModuleDeviceResponseDTO> configurerModuleDirect(
            @Valid @RequestBody BluetoothDirectRequest request) {

        try {
            User user = getCurrentUser();

            request.setUserId(user.getId());

            ModuleDevice module = moduleDeviceService.configurerModuleDirect(request);
            ModuleDeviceResponseDTO response = convertToDTO(module);

            return BaseResponse.success("Module configuré avec succès", response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/search/{bluetoothAddress}")
    @Operation(summary = "Rechercher module", description = "Recherche un module par adresse Bluetooth")
    public BaseResponse<ModuleSearchResponse> rechercherModule(
            @PathVariable String bluetoothAddress) {

        try {
            User user = getCurrentUser();

            ModuleSearchResponse response = moduleDeviceService.rechercherModule(
                    bluetoothAddress,
                    user.getId()
            );

            return BaseResponse.success(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    @DeleteMapping("/{bluetoothAddress}")
    @Operation(summary = "Supprimer module", description = "Supprime un module non configuré")
    public BaseResponse<String> supprimerModule(
            @PathVariable String bluetoothAddress) {

        try {
            User user = getCurrentUser();

            moduleDeviceService.supprimerModuleNonConfigure(
                    bluetoothAddress,
                    user.getId()
            );

            return BaseResponse.success("Module supprimé avec succès");
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    private ModuleDeviceResponseDTO convertToDTO(ModuleDevice module) {
        return ModuleDeviceResponseDTO.builder()
                .deviceCode(module.getDeviceCode())
                .bluetoothAddress(module.getBluetoothAddress())
                .typeModule(module.getTypeModule())
                .statut(module.getStatut())
                .configured(module.getConfigured())
                .lastSeenAt(module.getLastSeenAt())
                .firmwareVersion(module.getFirmwareVersion())
                .captureInterval(module.getCaptureInterval())
                .proprietaireId(module.getProprietaire() != null ? module.getProprietaire().getId() : null)
                .compteurId(module.getCompteur() != null ? module.getCompteur().getId() : null)
                .compteurReference(module.getCompteur() != null ? module.getCompteur().getReference() : null)
                .modeLectureAssocie(module.getModeLectureAssocie())
                .build();
    }

    public record CompatibilityResponse(
            String bluetoothAddress,
            Long compteurId,
            boolean compatible,
            String message
    ) {}
}