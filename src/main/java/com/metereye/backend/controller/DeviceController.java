// DeviceController.java
package com.metereye.backend.controller;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.User;
import com.metereye.backend.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Device Management", description = "Gestion des modules ESP32-CAM")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/scan")
    @Operation(summary = "Scanner et enregistrer un module via QR code")
    public ResponseEntity<DeviceResponseDTO> scanAndRegisterDevice(
            @Valid @RequestBody DeviceScanDTO scanDTO,
            @AuthenticationPrincipal User user) {
        
        DeviceResponseDTO response = deviceService.scanAndRegisterDevice(scanDTO, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deviceCode}/associate")
    @Operation(summary = "Associer un module à un compteur")
    public ResponseEntity<DeviceResponseDTO> associateDeviceToMeter(
            @PathVariable String deviceCode,
            @Valid @RequestBody DeviceAssociateDTO associateDTO,
            @AuthenticationPrincipal User user) {
        
        DeviceResponseDTO response = deviceService.associateDeviceToMeter(deviceCode, associateDTO, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deviceCode}/handshake")
    @Operation(summary = "Handshake initial du module avec le backend")
    public ResponseEntity<DeviceResponseDTO> deviceHandshake(
            @PathVariable String deviceCode,
            @Valid @RequestBody DeviceHandshakeDTO handshakeDTO) {
        
        DeviceResponseDTO response = deviceService.deviceHandshake(deviceCode, handshakeDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deviceCode}/heartbeat")
    @Operation(summary = "Heartbeat du module pour maintenir la connexion")
    public ResponseEntity<DeviceResponseDTO> deviceHeartbeat(
            @PathVariable String deviceCode,
            @Valid @RequestBody DeviceHeartbeatDTO heartbeatDTO) {
        
        DeviceResponseDTO response = deviceService.deviceHeartbeat(deviceCode, heartbeatDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{deviceCode}/status")
    @Operation(summary = "Obtenir le statut d'un module")
    public ResponseEntity<DeviceResponseDTO> getDeviceStatus(
            @PathVariable String deviceCode,
            @AuthenticationPrincipal User user) {
        
        DeviceResponseDTO response = deviceService.getDeviceStatus(deviceCode, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @Operation(summary = "Lister les modules de l'utilisateur connecté")
    public ResponseEntity<List<DeviceResponseDTO>> getUserDevices(@AuthenticationPrincipal User user) {
        List<DeviceResponseDTO> devices = deviceService.getUserDevices(user);
        return ResponseEntity.ok(devices);
    }

    @PutMapping("/{deviceCode}/capture-interval")
    @Operation(summary = "Mettre à jour l'intervalle de capture")
    public ResponseEntity<DeviceResponseDTO> updateCaptureInterval(
            @PathVariable String deviceCode,
            @RequestParam Integer interval,
            @AuthenticationPrincipal User user) {
        
        DeviceResponseDTO response = deviceService.updateDeviceCaptureInterval(deviceCode, interval, user);
        return ResponseEntity.ok(response);
    }
}
