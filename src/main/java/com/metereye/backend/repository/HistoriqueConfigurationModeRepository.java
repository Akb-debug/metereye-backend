// HistoriqueConfigurationModeRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.HistoriqueConfigurationMode;
import com.metereye.backend.enums.ModeLectureCompteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour l'historique des configurations de mode
 */
@Repository
public interface HistoriqueConfigurationModeRepository extends JpaRepository<HistoriqueConfigurationMode, Long> {
    
    List<HistoriqueConfigurationMode> findByCompteurIdOrderByDateChangementDesc(Long compteurId);
    
    List<HistoriqueConfigurationMode> findByChangeParUserIdOrderByDateChangementDesc(Long userId);
    
    @Query("SELECT h FROM HistoriqueConfigurationMode h WHERE h.compteur.id = :compteurId AND h.dateChangement >= :debut")
    List<HistoriqueConfigurationMode> findByCompteurIdAndDateChangementAfter(
            @Param("compteurId") Long compteurId, 
            @Param("debut") LocalDateTime debut);
    
    @Query("SELECT COUNT(h) FROM HistoriqueConfigurationMode h WHERE h.compteur.id = :compteurId AND h.dateChangement >= :debut")
    long countChangementsRecents(@Param("compteurId") Long compteurId, @Param("debut") LocalDateTime debut);
    
    @Query("SELECT h FROM HistoriqueConfigurationMode h WHERE h.nouveauMode = :mode AND h.dateChangement >= :debut")
    List<HistoriqueConfigurationMode> findByNouveauModeAndDateChangementAfter(
            @Param("mode") ModeLectureCompteur mode, 
            @Param("debut") LocalDateTime debut);
}
