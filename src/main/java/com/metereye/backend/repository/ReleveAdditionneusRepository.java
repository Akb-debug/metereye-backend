// ✅ CRÉÉ — ReleveAdditionneusRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.ReleveAdditionneuse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReleveAdditionneusRepository extends JpaRepository<ReleveAdditionneuse, Long> {

    List<ReleveAdditionneuse> findBySousCompteurId(Long sousCompteurId);

    Optional<ReleveAdditionneuse> findTopBySousCompteurIdOrderByDateReleveDesc(Long sousCompteurId);

    List<ReleveAdditionneuse> findBySousCompteurIdAndDateReleveBetween(
            Long sousCompteurId, LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(r.consommationCalculee), 0)" +
           " FROM ReleveAdditionneuse r" +
           " WHERE r.sousCompteur.id = :id" +
           " AND r.dateReleve BETWEEN :debut AND :fin")
    Double sumConsommationByPeriode(
            @Param("id") Long id,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);
}
