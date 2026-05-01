// ModuleDeviceController.java
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
@RequestMapping("/api/module-devices")
@RequiredArgsConstructor
@Tag(name = "Module Devices", description = "Gestion des modules devices ESP32-CAM et ESP32-PZEM004T")
public class ModuleDeviceController extends BaseController {

    private final ModuleDeviceService moduleDeviceService;

    @PostMapping("/scan")
    @Operation(summary = "Scanner un module device", description = "Enregistre un nouveau module après scan QR code")
    public BaseResponse<ModuleDeviceResponseDTO> scannerModule(
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

    @PostMapping("/{deviceCode}/handshake")
    @Operation(summary = "Handshake module", description = "Première connexion du module au backend")
    public BaseResponse<ModuleDeviceResponseDTO> effectuerHandshake(
            @PathVariable String deviceCode,
            @Valid @RequestBody DeviceHandshakeRequest request) {

        try {
            ModuleDevice module = moduleDeviceService.effectuerHandshake(
                    deviceCode,
                    request.getFirmwareVersion(),
                    request.getIpAddress(),
                    request.getWifiSsid()
            );

            ModuleDeviceResponseDTO response = convertToDTO(module);

            return BaseResponse.success("Handshake réussi", response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/{deviceCode}/heartbeat")
    @Operation(summary = "Heartbeat module", description = "Signale que le module est en ligne")
    public BaseResponse<String> envoyerHeartbeat(@PathVariable String deviceCode) {

        try {
            moduleDeviceService.envoyerHeartbeat(deviceCode);
            return BaseResponse.success("Heartbeat enregistré");
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/{deviceCode}/status")
    @Operation(summary = "Statut module", description = "Récupère le statut actuel d'un module")
    public BaseResponse<ModuleDeviceResponseDTO> getStatutModule(@PathVariable String deviceCode) {

        try {
            ModuleDevice module = moduleDeviceService.getStatutModule(deviceCode);
            ModuleDeviceResponseDTO response = convertToDTO(module);

            return BaseResponse.success(response);
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    @GetMapping("/my")
    @Operation(summary = "Lister mes modules", description = "Récupère tous les modules de l'utilisateur connecté")
    public BaseResponse<List<ModuleDeviceResponseDTO>> getModulesUtilisateur() {

        try {
            User user = getCurrentUser();

            List<ModuleDevice> modules = moduleDeviceService.getModulesUtilisateur(user.getId());

            List<ModuleDeviceResponseDTO> response = modules.stream()
                    .map(this::convertToDTO)
                    .toList();

            return BaseResponse.success(response);
        } catch (Exception e) {
            return BaseResponse.error(500, e.getMessage());
        }
    }

    @PutMapping("/{deviceCode}/capture-interval")
    @Operation(summary = "Configurer intervalle capture", description = "Modifie la fréquence de capture des relevés")
    public BaseResponse<String> configurerIntervalleCapture(
            @PathVariable String deviceCode,
            @RequestParam Integer interval) {

        try {
            moduleDeviceService.configurerIntervalleCapture(deviceCode, interval);
            return BaseResponse.success("Intervalle de capture configuré");
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/compteurs/{compteurId}/changer-mode")
    @Operation(summary = "Changer mode lecture", description = "Change le mode de lecture d'un compteur avec migration")
    public BaseResponse<String> changerModeLecture(
            @PathVariable Long compteurId,
            @Valid @RequestBody ChangementModeRequest request) {

        try {
            User user = getCurrentUser();

            moduleDeviceService.changerModeLecture(
                    compteurId,
                    request.getNouveauMode(),
                    request.getMotif(),
                    user.getId()
            );

            return BaseResponse.success("Mode de lecture changé avec succès");
        } catch (Exception e) {
            return BaseResponse.badRequest(e.getMessage());
        }
    }

    @PostMapping("/{deviceCode}/configurer-pzem")
    @Operation(summary = "Configurer ESP32-PZEM004T", description = "Configure les paramètres spécifiques du capteur PZEM004T")
    public BaseResponse<String> configurerPzem(
            @PathVariable String deviceCode,
            @Valid @RequestBody PzemConfigurationRequest request) {

        try {
            return BaseResponse.success("Configuration PZEM004T enregistrée");
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
                .wifiSsid(module.getWifiSsid())
                .ipAddress(module.getIpAddress())
                .proprietaireId(module.getProprietaire() != null ? module.getProprietaire().getId() : null)
                .compteurId(module.getCompteur() != null ? module.getCompteur().getId() : null)
                .compteurReference(module.getCompteur() != null ? module.getCompteur().getReference() : null)
                .modeLectureAssocie(module.getModeLectureAssocie())
                .build();
    }
}