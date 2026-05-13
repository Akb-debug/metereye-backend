// ✅ CRÉÉ — SousCompteurRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.SousCompteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SousCompteurRepository extends JpaRepository<SousCompteur, Long> {

    List<SousCompteur> findByMaisonId(Long maisonId);

    List<SousCompteur> findByMaisonIdAndActifTrue(Long maisonId);

    Optional<SousCompteur> findByLocataireId(Long locataireId);

    boolean existsByReferenceAndMaisonId(String reference, Long maisonId);
}
