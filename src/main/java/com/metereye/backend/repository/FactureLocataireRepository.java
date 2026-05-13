// ✅ CRÉÉ — FactureLocataireRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.FactureLocataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactureLocataireRepository extends JpaRepository<FactureLocataire, Long> {

    List<FactureLocataire> findBySousCompteurId(Long sousCompteurId);

    List<FactureLocataire> findBySousCompteurMaisonId(Long maisonId);

    Optional<FactureLocataire> findBySousCompteurIdAndMoisAndAnnee(
            Long sousCompteurId, Integer mois, Integer annee);

    List<FactureLocataire> findBySousCompteurMaisonIdAndMoisAndAnnee(
            Long maisonId, Integer mois, Integer annee);

    List<FactureLocataire> findBySousCompteurLocataireId(Long locataireId);
}
