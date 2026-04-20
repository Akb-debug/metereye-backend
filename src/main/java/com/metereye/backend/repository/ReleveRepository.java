// ReleveRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.Compteur;
import com.metereye.backend.entity.Releve;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReleveRepository extends JpaRepository<Releve, Long> {

    List<Releve> findByCompteurOrderByDateTimeDesc(Compteur compteur);

    Page<Releve> findByCompteurOrderByDateTimeDesc(Compteur compteur, Pageable pageable);

    Optional<Releve> findTopByCompteurOrderByDateTimeDesc(Compteur compteur);

    List<Releve> findByCompteurAndDateTimeBetween(Compteur compteur, LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM Releve r WHERE r.compteur.id = :compteurId AND r.dateTime >= :startDate ORDER BY r.dateTime DESC")
    List<Releve> findRecentRelevesByCompteur(@Param("compteurId") Long compteurId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(r.consommationCalculee) FROM Releve r WHERE r.compteur.id = :compteurId AND r.dateTime >= :startDate")
    Double calculateAverageConsumption(@Param("compteurId") Long compteurId, @Param("startDate") LocalDateTime startDate);
}