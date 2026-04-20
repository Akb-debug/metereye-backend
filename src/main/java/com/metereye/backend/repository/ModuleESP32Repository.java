// ModuleESP32Repository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.ModuleESP32;
import com.metereye.backend.enums.StatutModuleESP32;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleESP32Repository extends JpaRepository<ModuleESP32, Long> {

    // Anciennes méthodes (compatibilité)
    Optional<ModuleESP32> findByAddressMAC(String addressMAC);

    // Nouvelles méthodes pour l'onboarding
    Optional<ModuleESP32> findByDeviceCode(String deviceCode);

    Optional<ModuleESP32> findByQrCodeValue(String qrCodeValue);

    List<ModuleESP32> findByProprietaireId(Long proprietaireId);

    List<ModuleESP32> findByStatut(StatutModuleESP32 statut);

    List<ModuleESP32> findByCompteurId(Long compteurId);

    List<ModuleESP32> findByConfiguredFalseAndProprietaireId(Long proprietaireId);

    // Méthodes pour le monitoring
    List<ModuleESP32> findByLastSeenAtBefore(LocalDateTime before);

    List<ModuleESP32> findByStatutAndLastSeenAtBefore(StatutModuleESP32 statut, LocalDateTime before);
}