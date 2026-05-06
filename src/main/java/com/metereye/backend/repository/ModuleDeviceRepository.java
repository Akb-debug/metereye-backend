// ModuleDeviceRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.ModuleDevice;
import com.metereye.backend.enums.StatutModuleDevice;
import com.metereye.backend.enums.TypeModuleDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour les modules devices
 */
@Repository
public interface ModuleDeviceRepository extends JpaRepository<ModuleDevice, Long> {
    
    Optional<ModuleDevice> findByDeviceCode(String deviceCode);
    
    Optional<ModuleDevice> findByBluetoothAddress(String bluetoothAddress);
    
    List<ModuleDevice> findByProprietaireId(Long proprietaireId);
    
    List<ModuleDevice> findByTypeModule(TypeModuleDevice typeModule);
    
    @Query("SELECT m FROM ModuleDevice m WHERE m.compteur.id = :compteurId")
    Optional<ModuleDevice> findByCompteurId(@Param("compteurId") Long compteurId);
    
    @Query("SELECT m FROM ModuleDevice m WHERE m.proprietaire.id = :proprietaireId AND m.statut = :statut")
    List<ModuleDevice> findByProprietaireIdAndStatut(@Param("proprietaireId") Long proprietaireId, @Param("statut") StatutModuleDevice statut);
    
    boolean existsByBluetoothAddress(String bluetoothAddress);
    
    boolean existsByDeviceCode(String deviceCode);
    
    @Query("SELECT COUNT(m) FROM ModuleDevice m WHERE m.proprietaire.id = :proprietaireId AND m.statut = :statut")
    long countByProprietaireIdAndStatut(@Param("proprietaireId") Long proprietaireId, @Param("statut") StatutModuleDevice statut);
}
