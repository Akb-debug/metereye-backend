// ModuleDeviceService.java
package com.metereye.backend.service;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.*;
import com.metereye.backend.enums.*;
import com.metereye.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des modules devices
 * Gère ESP32-CAM, ESP32-PZEM004T et autres modules
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ModuleDeviceService {

    private final ModuleDeviceRepository moduleDeviceRepository;
    private final ESP32CamDeviceRepository esp32CamDeviceRepository;
    private final ESP32PzemDeviceRepository esp32PzemDeviceRepository;
    private final CompteurRepository compteurRepository;
    private final HistoriqueConfigurationModeRepository historiqueRepository;
    private final UserRepository userRepository;

    /**
     * Scanner et enregistrer un nouveau module device via Bluetooth
     */
    public ModuleDevice scannerModuleBluetooth(BluetoothScanRequest request) {
        // Vérifier si l'adresse Bluetooth existe déjà
        if (moduleDeviceRepository.existsByBluetoothAddress(request.getBluetoothAddress())) {
            throw new IllegalArgumentException("Cette adresse Bluetooth est déjà enregistrée");
        }

        // Créer le module approprié selon le type
        ModuleDevice module = creerModuleSelonTypeBluetooth(request);
        
        module = moduleDeviceRepository.save(module);
        log.info("Module {} scanné et enregistré via Bluetooth avec deviceCode: {}", 
                module.getTypeModule(), module.getDeviceCode());
        
        return module;
    }

    /**
     * Configurer et associer un module via Bluetooth
     */
    public ModuleDevice configurerModuleBluetooth(BluetoothConfigurationRequest request, Long userId) {
        // Trouver le module par adresse Bluetooth
        ModuleDevice module = moduleDeviceRepository.findByBluetoothAddress(request.getBluetoothAddress())
                .orElseThrow(() -> new IllegalArgumentException("Module non trouvé avec cette adresse Bluetooth"));
        
        Compteur compteur = compteurRepository.findById(request.getCompteurId())
                .orElseThrow(() -> new IllegalArgumentException("Compteur non trouvé: " + request.getCompteurId()));

        // Vérifier la compatibilité mode métier
        String modeLectureModule = module.getModeLectureAssocie();
        String modeLectureCompteur = compteur.getModeLectureConfigure() != null ? 
                                   compteur.getModeLectureConfigure().name() : null;

        if (modeLectureCompteur != null && !modeLectureCompteur.equals(modeLectureModule)) {
            throw new IllegalArgumentException(String.format(
                    "Incompatibilité: module %s nécessite mode %s mais compteur configuré en %s",
                    module.getTypeModule(), modeLectureModule, modeLectureCompteur));
        }

        // Configurer le module
        module.setCompteur(compteur);
        module.setCaptureInterval(request.getCaptureInterval());
        module.setStatut(StatutModuleDevice.EN_CONFIGURATION);
        
        // Configurer les spécificités selon le type
        if (module instanceof ESP32CamDevice esp32Cam) {
            configurerESP32Cam(esp32Cam, request);
        } else if (module instanceof ESP32PzemDevice esp32Pzem) {
            configurerESP32Pzem(esp32Pzem, request);
        }
        
        module = moduleDeviceRepository.save(module);
        
        // Configurer le compteur si nécessaire
        if (modeLectureCompteur == null) {
            configurerModeLectureCompteur(compteur, ModeLectureCompteur.valueOf(modeLectureModule));
        }
        
        log.info("Module {} configuré et associé au compteur {} via Bluetooth", 
                request.getBluetoothAddress(), compteur.getReference());
        
        return module;
    }

    /**
     * Configurer le mode de lecture d'un compteur avec gestion de l'historique
     */
    public void configurerModeLectureCompteur(Compteur compteur, ModeLectureCompteur nouveauMode) {
        ModeLectureCompteur ancienMode = compteur.getModeLectureConfigure();
        
        if (ancienMode == nouveauMode) {
            return; // Pas de changement
        }

        // Désactiver l'ancienne configuration
        if (ancienMode != null) {
            desactiverAncienneConfiguration(compteur, ancienMode);
        }

        // Configurer le nouveau mode
        compteur.setModeLectureConfigure(nouveauMode);
        compteur.setStatut(StatutCompteur.ACTIF);
        compteurRepository.save(compteur);

        log.info("Compteur {} configuré en mode {} (précédemment: {})", 
                compteur.getReference(), nouveauMode, ancienMode);
    }

    /**
     * Changer le mode de lecture d'un compteur (migration)
     */
    public void changerModeLecture(Long compteurId, ModeLectureCompteur nouveauMode, String motif, Long userId) {
        Compteur compteur = compteurRepository.findById(compteurId)
                .orElseThrow(() -> new IllegalArgumentException("Compteur non trouvé: " + compteurId));
        
        ModeLectureCompteur ancienMode = compteur.getModeLectureConfigure();
        User utilisateur = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + userId));

        // Créer l'historique du changement
        HistoriqueConfigurationMode historique = HistoriqueConfigurationMode.builder()
                .compteur(compteur)
                .ancienMode(ancienMode)
                .nouveauMode(nouveauMode)
                .dateChangement(LocalDateTime.now())
                .changeParUser(utilisateur)
                .motifChangement(motif)
                .ancienDeviceCode(getDeviceCodeActuel(compteur))
                .configurationDesactivee(false)
                .donneesMigrees(false)
                .build();

        // Désactiver l'ancienne configuration
        if (ancienMode != null) {
            desactiverAncienneConfiguration(compteur, ancienMode);
            historique.setConfigurationDesactivee(true);
        }

        // Configurer le nouveau mode
        configurerModeLectureCompteur(compteur, nouveauMode);
        
        // Sauvegarder l'historique
        historiqueRepository.save(historique);

        log.info("Changement de mode compteur {}: {} -> {} par utilisateur {}", 
                compteur.getReference(), ancienMode, nouveauMode, userId);
    }

    /**
     * Effectuer le handshake d'un module
     */
    public ModuleDevice effectuerHandshake(String deviceCode, String firmwareVersion, String ipAddress, String wifiSsid) {
        ModuleDevice module = moduleDeviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new IllegalArgumentException("Module non trouvé: " + deviceCode));

        module.setFirmwareVersion(firmwareVersion);
        module.setIpAddress(ipAddress);
        module.setWifiSsid(wifiSsid);
        module.setLastSeenAt(LocalDateTime.now());
        module.setStatut(StatutModuleDevice.ACTIF);
        module.setConfigured(true);

        // Configurations spécifiques selon le type
        if (module instanceof ESP32CamDevice esp32Cam) {
            esp32Cam.configurerCapture(esp32Cam.getCaptureInterval(), esp32Cam.getQualiteImage(), esp32Cam.getFlashActive());
        } else if (module instanceof ESP32PzemDevice esp32Pzem) {
            esp32Pzem.configurerCapteur(esp32Pzem.getSeuilAlerte(), esp32Pzem.getFacteurCorrection(), esp32Pzem.getModeCalibrage());
        }

        module = moduleDeviceRepository.save(module);
        
        log.info("Handshake réussi pour module {} (type: {})", deviceCode, module.getTypeModule());
        
        return module;
    }

    /**
     * Envoyer un heartbeat
     */
    public void envoyerHeartbeat(String deviceCode) {
        ModuleDevice module = moduleDeviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new IllegalArgumentException("Module non trouvé: " + deviceCode));

        module.setLastSeenAt(LocalDateTime.now());
        
        // Si le module était hors ligne, le remettre actif
        if (module.getStatut() == StatutModuleDevice.HORS_LIGNE) {
            module.setStatut(StatutModuleDevice.ACTIF);
        }
        
        moduleDeviceRepository.save(module);
    }

    /**
     * Obtenir le statut d'un module
     */
    public ModuleDevice getStatutModule(String deviceCode) {
        return moduleDeviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new IllegalArgumentException("Module non trouvé: " + deviceCode));
    }

    /**
     * Lister les modules d'un utilisateur
     */
    public List<ModuleDevice> getModulesUtilisateur(Long userId) {
        return moduleDeviceRepository.findByProprietaireId(userId);
    }

    /**
     * Vérifier la compatibilité d'un module avec un compteur
     */
    public boolean verifierCompatibilite(String bluetoothAddress, Long compteurId) {
        ModuleDevice module = moduleDeviceRepository.findByBluetoothAddress(bluetoothAddress)
                .orElseThrow(() -> new IllegalArgumentException("Module non trouvé: " + bluetoothAddress));
        
        Compteur compteur = compteurRepository.findById(compteurId)
                .orElseThrow(() -> new IllegalArgumentException("Compteur non trouvé: " + compteurId));

        String modeLectureModule = module.getModeLectureAssocie();
        String modeLectureCompteur = compteur.getModeLectureConfigure() != null ? 
                                   compteur.getModeLectureConfigure().name() : null;

        // Si le compteur n'est pas encore configuré, compatible
        if (modeLectureCompteur == null) {
            return true;
        }

        return modeLectureCompteur.equals(modeLectureModule);
    }

    /**
     * Obtenir un module par adresse Bluetooth
     */
    public ModuleDevice getModuleParAdresseBluetooth(String bluetoothAddress, Long userId) {
        ModuleDevice module = moduleDeviceRepository.findByBluetoothAddress(bluetoothAddress)
                .orElseThrow(() -> new IllegalArgumentException("Module non trouvé: " + bluetoothAddress));

        // Vérifier que le module appartient à l'utilisateur
        if (!module.getProprietaire().getId().equals(userId)) {
            throw new IllegalArgumentException("Ce module ne vous appartient pas");
        }

        return module;
    }

    /**
     * Configurer un module directement via adresse Bluetooth
     */
    public ModuleDevice configurerModuleDirect(BluetoothDirectRequest request) {
        Compteur compteur = compteurRepository.findById(request.getCompteurId())
                .orElseThrow(() -> new IllegalArgumentException("Compteur non trouvé: " + request.getCompteurId()));

        Optional<ModuleDevice> existingModule = moduleDeviceRepository.findByBluetoothAddress(request.getBluetoothAddress());

        ModuleDevice module;
        if (existingModule.isPresent()) {
            module = existingModule.get();
            if (!module.getProprietaire().getId().equals(request.getUserId())) {
                throw new IllegalArgumentException("Ce module est déjà associé à un autre utilisateur");
            }
            if (module.getConfigured()) {
                throw new IllegalArgumentException("Ce module est déjà configuré");
            }
            // Vérifier compatibilité pour un module existant
            String modeLectureModule = module.getModeLectureAssocie();
            String modeLectureCompteur = compteur.getModeLectureConfigure() != null
                    ? compteur.getModeLectureConfigure().name() : null;
            if (modeLectureCompteur != null && !modeLectureCompteur.equals(modeLectureModule)) {
                throw new IllegalArgumentException("Module non compatible avec ce compteur");
            }
        } else {
            // Pour un nouveau module : vérifier la compatibilité par type avant de créer
            String modeLectureModule = request.getTypeModule() == TypeModuleDevice.ESP32_CAM ? "ESP32_CAM" : "SENSOR";
            String modeLectureCompteur = compteur.getModeLectureConfigure() != null
                    ? compteur.getModeLectureConfigure().name() : null;
            if (modeLectureCompteur != null && !modeLectureCompteur.equals(modeLectureModule)) {
                throw new IllegalArgumentException("Module non compatible avec ce compteur");
            }
            module = creerModuleDirect(request);
        }

        module.setCompteur(compteur);
        module.setCaptureInterval(request.getCaptureInterval());
        module.setStatut(StatutModuleDevice.EN_CONFIGURATION);

        if (module instanceof ESP32CamDevice esp32Cam) {
            configurerESP32CamDirect(esp32Cam, request);
        } else if (module instanceof ESP32PzemDevice esp32Pzem) {
            configurerESP32PzemDirect(esp32Pzem, request);
        }

        module = moduleDeviceRepository.save(module);

        String modeLectureModule = module.getModeLectureAssocie();
        if (compteur.getModeLectureConfigure() == null) {
            configurerModeLectureCompteur(compteur, ModeLectureCompteur.valueOf(modeLectureModule));
        }

        log.info("Module {} configuré directement et associé au compteur {}",
                request.getBluetoothAddress(), compteur.getReference());

        return module;
    }

    /**
     * Rechercher un module par adresse Bluetooth
     */
    public ModuleSearchResponse rechercherModule(String bluetoothAddress, Long userId) {
        boolean exists = moduleDeviceRepository.existsByBluetoothAddress(bluetoothAddress);
        
        if (!exists) {
            return ModuleSearchResponse.builder()
                    .bluetoothAddress(bluetoothAddress)
                    .exists(false)
                    .belongsToUser(false)
                    .canConfigure(false)
                    .build();
        }

        ModuleDevice module = moduleDeviceRepository.findByBluetoothAddress(bluetoothAddress)
                .orElse(null);

        boolean belongsToUser = module != null && module.getProprietaire() != null && 
                              module.getProprietaire().getId().equals(userId);
        boolean canConfigure = module != null && !module.getConfigured() && belongsToUser;

        return ModuleSearchResponse.builder()
                .deviceCode(module != null ? module.getDeviceCode() : null)
                .bluetoothAddress(bluetoothAddress)
                .typeModule(module != null ? module.getTypeModule() : null)
                .statut(module != null ? module.getStatut() : null)
                .configured(module != null ? module.getConfigured() : false)
                .moduleName("Module-" + (module != null ? module.getTypeModule() : "UNKNOWN"))
                .firmwareVersion(module != null ? module.getFirmwareVersion() : null)
                .lastSeenAt(module != null ? module.getLastSeenAt() : null)
                .proprietaireId(module != null && module.getProprietaire() != null ? module.getProprietaire().getId() : null)
                .compteurId(module != null && module.getCompteur() != null ? module.getCompteur().getId() : null)
                .compteurReference(module != null && module.getCompteur() != null ? module.getCompteur().getReference() : null)
                .modeLectureAssocie(module != null ? module.getModeLectureAssocie() : null)
                .exists(true)
                .belongsToUser(belongsToUser)
                .canConfigure(canConfigure)
                .build();
    }

    /**
     * Supprimer un module non configuré
     */
    public void supprimerModuleNonConfigure(String bluetoothAddress, Long userId) {
        ModuleDevice module = getModuleParAdresseBluetooth(bluetoothAddress, userId);

        if (module.getConfigured()) {
            throw new IllegalArgumentException("Impossible de supprimer un module configuré");
        }

        moduleDeviceRepository.delete(module);
        log.info("Module non configuré {} supprimé par utilisateur {}", bluetoothAddress, userId);
    }

    /**
     * Configurer l'intervalle de capture
     */
    public void configurerIntervalleCapture(String deviceCode, Integer interval) {
        ModuleDevice module = moduleDeviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new IllegalArgumentException("Module non trouvé: " + deviceCode));

        module.setCaptureInterval(interval);
        moduleDeviceRepository.save(module);
        
        log.info("Intervalle de capture configuré à {}s pour module {}", interval, deviceCode);
    }

    // Méthodes privées

    private ModuleDevice creerModuleSelonTypeBluetooth(BluetoothScanRequest request) {
        String deviceCode = generateDeviceCode();
        User proprietaire = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + request.getUserId()));

        return switch (request.getTypeModule()) {
            case ESP32_CAM -> ESP32CamDevice.builder()
                    .deviceCode(deviceCode)
                    .bluetoothAddress(request.getBluetoothAddress())
                    .serialNumber(request.getSerialNumber())
                    .firmwareVersion(request.getFirmwareVersion())
                    .typeModule(TypeModuleDevice.ESP32_CAM)
                    .proprietaire(proprietaire)
                    .build();
            case ESP32_PZEM004T -> ESP32PzemDevice.builder()
                    .deviceCode(deviceCode)
                    .bluetoothAddress(request.getBluetoothAddress())
                    .serialNumber(request.getSerialNumber())
                    .firmwareVersion(request.getFirmwareVersion())
                    .typeModule(TypeModuleDevice.ESP32_PZEM004T)
                    .proprietaire(proprietaire)
                    .build();
            default -> throw new IllegalArgumentException("Type de module non supporté: " + request.getTypeModule());
        };
    }

    private void configurerESP32Cam(ESP32CamDevice esp32Cam, BluetoothConfigurationRequest request) {
        if (request.getResolutionCamera() != null) {
            esp32Cam.setResolutionCamera(request.getResolutionCamera());
        }
        if (request.getFlashActive() != null) {
            esp32Cam.setFlashActive(request.getFlashActive());
        }
        if (request.getQualiteImage() != null) {
            esp32Cam.setQualiteImage(request.getQualiteImage());
        }
        if (request.getAngleCapture() != null) {
            esp32Cam.setAngleCapture(request.getAngleCapture());
        }
    }

    private void configurerESP32Pzem(ESP32PzemDevice esp32Pzem, BluetoothConfigurationRequest request) {
        if (request.getSeuilAlerte() != null) {
            esp32Pzem.setSeuilAlerte(request.getSeuilAlerte());
        }
        if (request.getFacteurCorrection() != null) {
            esp32Pzem.setFacteurCorrection(request.getFacteurCorrection());
        }
        if (request.getModeCalibrage() != null) {
            esp32Pzem.setModeCalibrage(request.getModeCalibrage());
        }
        if (request.getTensionMax() != null) {
            esp32Pzem.setTensionMax(request.getTensionMax());
        }
        if (request.getCourantMax() != null) {
            esp32Pzem.setCourantMax(request.getCourantMax());
        }
        if (request.getPuissanceMax() != null) {
            esp32Pzem.setPuissanceMax(request.getPuissanceMax());
        }
    }

    private ModuleDevice creerModuleDirect(BluetoothDirectRequest request) {
        String deviceCode = generateDeviceCode();
        User proprietaire = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + request.getUserId()));

        return switch (request.getTypeModule()) {
            case ESP32_CAM -> ESP32CamDevice.builder()
                    .deviceCode(deviceCode)
                    .bluetoothAddress(request.getBluetoothAddress())
                    .serialNumber(request.getSerialNumber())
                    .firmwareVersion(request.getFirmwareVersion())
                    .typeModule(TypeModuleDevice.ESP32_CAM)
                    .proprietaire(proprietaire)
                    .build();
            case ESP32_PZEM004T -> ESP32PzemDevice.builder()
                    .deviceCode(deviceCode)
                    .bluetoothAddress(request.getBluetoothAddress())
                    .serialNumber(request.getSerialNumber())
                    .firmwareVersion(request.getFirmwareVersion())
                    .typeModule(TypeModuleDevice.ESP32_PZEM004T)
                    .proprietaire(proprietaire)
                    .build();
            default -> throw new IllegalArgumentException("Type de module non supporté: " + request.getTypeModule());
        };
    }

    private void configurerESP32CamDirect(ESP32CamDevice esp32Cam, BluetoothDirectRequest request) {
        if (request.getResolutionCamera() != null) {
            esp32Cam.setResolutionCamera(request.getResolutionCamera());
        }
        if (request.getFlashActive() != null) {
            esp32Cam.setFlashActive(request.getFlashActive());
        }
        if (request.getQualiteImage() != null) {
            esp32Cam.setQualiteImage(request.getQualiteImage());
        }
        if (request.getAngleCapture() != null) {
            esp32Cam.setAngleCapture(request.getAngleCapture());
        }
    }

    private void configurerESP32PzemDirect(ESP32PzemDevice esp32Pzem, BluetoothDirectRequest request) {
        if (request.getSeuilAlerte() != null) {
            esp32Pzem.setSeuilAlerte(request.getSeuilAlerte());
        }
        if (request.getFacteurCorrection() != null) {
            esp32Pzem.setFacteurCorrection(request.getFacteurCorrection());
        }
        if (request.getModeCalibrage() != null) {
            esp32Pzem.setModeCalibrage(request.getModeCalibrage());
        }
        if (request.getTensionMax() != null) {
            esp32Pzem.setTensionMax(request.getTensionMax());
        }
        if (request.getCourantMax() != null) {
            esp32Pzem.setCourantMax(request.getCourantMax());
        }
        if (request.getPuissanceMax() != null) {
            esp32Pzem.setPuissanceMax(request.getPuissanceMax());
        }
    }

    private String generateDeviceCode() {
        return java.util.UUID.randomUUID().toString();
    }

    private void desactiverAncienneConfiguration(Compteur compteur, ModeLectureCompteur ancienMode) {
        // Désactiver le module associé
        if (compteur.getModuleESP32() != null) {
            ModuleESP32 ancienModule = compteur.getModuleESP32();
            ancienModule.setStatut(StatutModuleESP32.HORS_LIGNE);
            ancienModule.setConfigured(false);
            // ancienModule.setCompteur(null); // Garder la référence pour l'historique
        }
        
        log.info("Ancienne configuration {} désactivée pour compteur {}", ancienMode, compteur.getReference());
    }

    private String getDeviceCodeActuel(Compteur compteur) {
        if (compteur.getModuleESP32() != null) {
            return compteur.getModuleESP32().getDeviceCode();
        }
        return null;
    }
}
