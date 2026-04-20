// DeviceServiceImpl.java
package com.metereye.backend.service.impl;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.*;
import com.metereye.backend.enums.StatutModuleESP32;
import com.metereye.backend.mapper.DeviceMapper;
import com.metereye.backend.repository.ModuleESP32Repository;
import com.metereye.backend.repository.CompteurRepository;
import com.metereye.backend.repository.UserRepository;
import com.metereye.backend.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceServiceImpl implements DeviceService {

    private final ModuleESP32Repository moduleRepository;
    private final CompteurRepository compteurRepository;
    private final UserRepository userRepository;
    private final DeviceMapper deviceMapper;

    @Override
    public DeviceResponseDTO scanAndRegisterDevice(DeviceScanDTO scanDTO, User user) {
        log.info("Scan QR code: {} par utilisateur: {}", scanDTO.getQrCode(), user.getId());

        // Vérifier si le QR code existe déjà
        Optional<ModuleESP32> existingModule = moduleRepository.findByQrCodeValue(scanDTO.getQrCode());
        
        ModuleESP32 module;
        if (existingModule.isPresent()) {
            module = existingModule.get();
            
            // Vérifier si déjà associé à quelqu'un d'autre
            if (module.getProprietaire() != null && !module.getProprietaire().getId().equals(user.getId())) {
                throw new RuntimeException("Ce module est déjà associé à un autre utilisateur");
            }
            
            // Associer à l'utilisateur si pas encore fait
            if (module.getProprietaire() == null) {
                module.setProprietaire(user);
                module = moduleRepository.save(module);
            }
        } else {
            // Créer nouveau module
            module = ModuleESP32.builder()
                    .deviceCode(UUID.randomUUID().toString())
                    .qrCodeValue(scanDTO.getQrCode())
                    .statut(StatutModuleESP32.NON_CONFIGURE)
                    .configured(false)
                    .proprietaire(user)
                    .captureInterval(3600)
                    .build();
            
            module = moduleRepository.save(module);
        }

        log.info("Module scanné et enregistré: deviceCode={}", module.getDeviceCode());
        return deviceMapper.toResponse(module);
    }

    @Override
    public DeviceResponseDTO associateDeviceToMeter(String deviceCode, DeviceAssociateDTO associateDTO, User user) {
        ModuleESP32 module = moduleRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new RuntimeException("Module non trouvé: " + deviceCode));

        // Vérifier autorisation
        if (!module.getProprietaire().getId().equals(user.getId())) {
            throw new RuntimeException("Non autorisé à configurer ce module");
        }

        // Récupérer le compteur
        Compteur compteur = compteurRepository.findById(associateDTO.getCompteurId())
                .orElseThrow(() -> new RuntimeException("Compteur non trouvé"));

        // Vérifier que le compteur appartient à l'utilisateur
        if (!compteur.getProprietaire().getId().equals(user.getId())) {
            throw new RuntimeException("Ce compteur ne vous appartient pas");
        }

        // Vérifier que le compteur est configuré pour ESP32_CAM
        if (compteur.getModeLectureConfigure() != com.metereye.backend.enums.ModeLectureCompteur.ESP32_CAM) {
            throw new RuntimeException("Ce compteur n'est pas configuré pour ESP32_CAM");
        }

        // Associer le module au compteur
        module.setCompteur(compteur);
        module.setCaptureInterval(associateDTO.getCaptureInterval());
        module.setStatut(StatutModuleESP32.EN_CONFIGURATION);
        
        module = moduleRepository.save(module);

        // Mettre à jour le compteur
        compteur.setModuleESP32(module);
        compteurRepository.save(compteur);

        log.info("Module associé au compteur: deviceCode={}, compteurId={}", deviceCode, associateDTO.getCompteurId());
        return deviceMapper.toResponse(module);
    }

    @Override
    public DeviceResponseDTO deviceHandshake(String deviceCode, DeviceHandshakeDTO handshakeDTO) {
        ModuleESP32 module = moduleRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new RuntimeException("Module non trouvé: " + deviceCode));

        // Valider version firmware (exemple)
        if (!isValidFirmwareVersion(handshakeDTO.getFirmwareVersion())) {
            throw new RuntimeException("Version firmware non supportée: " + handshakeDTO.getFirmwareVersion());
        }

        // Mettre à jour les informations du module
        module.setFirmwareVersion(handshakeDTO.getFirmwareVersion());
        module.setIpAddress(handshakeDTO.getIpAddress());
        module.setWifiSsid(handshakeDTO.getWifiSsid());
        module.setLastSeenAt(LocalDateTime.now());
        module.setStatut(StatutModuleESP32.ACTIF);
        module.setConfigured(true);

        module = moduleRepository.save(module);

        log.info("Handshake réussi: deviceCode={}, firmware={}", deviceCode, handshakeDTO.getFirmwareVersion());
        return deviceMapper.toResponse(module);
    }

    @Override
    public DeviceResponseDTO deviceHeartbeat(String deviceCode, DeviceHeartbeatDTO heartbeatDTO) {
        ModuleESP32 module = moduleRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new RuntimeException("Module non trouvé: " + deviceCode));

        // Mettre à jour le timestamp et statut
        module.setLastSeenAt(LocalDateTime.now());
        
        if (module.getStatut() == StatutModuleESP32.HORS_LIGNE) {
            module.setStatut(StatutModuleESP32.ACTIF);
        }

        module = moduleRepository.save(module);

        log.debug("Heartbeat reçu: deviceCode={}", deviceCode);
        return deviceMapper.toResponse(module);
    }

    @Override
    public DeviceResponseDTO getDeviceStatus(String deviceCode, User user) {
        ModuleESP32 module = moduleRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new RuntimeException("Module non trouvé: " + deviceCode));

        // Vérifier autorisation
        if (!module.getProprietaire().getId().equals(user.getId()) && 
            !user.getRole().getName().name().equals("STAFF")) {
            throw new RuntimeException("Non autorisé à voir ce module");
        }

        return deviceMapper.toResponse(module);
    }

    @Override
    public List<DeviceResponseDTO> getUserDevices(User user) {
        List<ModuleESP32> modules = moduleRepository.findByProprietaireId(user.getId());
        return modules.stream()
                .map(deviceMapper::toResponse)
                .toList();
    }

    @Override
    public DeviceResponseDTO updateDeviceCaptureInterval(String deviceCode, Integer interval, User user) {
        ModuleESP32 module = moduleRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new RuntimeException("Module non trouvé: " + deviceCode));

        // Vérifier autorisation
        if (!module.getProprietaire().getId().equals(user.getId())) {
            throw new RuntimeException("Non autorisé à modifier ce module");
        }

        module.setCaptureInterval(interval);
        module = moduleRepository.save(module);

        log.info("Intervalle de capture mis à jour: deviceCode={}, interval={}", deviceCode, interval);
        return deviceMapper.toResponse(module);
    }

    @Override
    public boolean isDeviceConfiguredAndActive(String deviceCode) {
        Optional<ModuleESP32> module = moduleRepository.findByDeviceCode(deviceCode);
        return module.filter(ModuleESP32::estConfigureEtActif).isPresent();
    }

    @Override
    public boolean canDeviceSendReading(String deviceCode) {
        Optional<ModuleESP32> module = moduleRepository.findByDeviceCode(deviceCode);
        return module.filter(m -> 
                m.estConfigureEtActif() && 
                m.getCompteur() != null &&
                m.getCompteur().estConfigurePourLecture() &&
                m.getCompteur().peutAccepterLecture(com.metereye.backend.enums.SourceReleve.ESP32_CAM)
        ).isPresent();
    }

    private boolean isValidFirmwareVersion(String version) {
        // Implémenter la logique de validation des versions
        // Pour l'instant, accepter toutes les versions >= 1.0.0
        return version != null && !version.isEmpty();
    }
}
