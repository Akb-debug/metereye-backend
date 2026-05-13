// ✅ CRÉÉ — MaisonRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.Maison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaisonRepository extends JpaRepository<Maison, Long> {

    List<Maison> findByProprietaireId(Long proprietaireId);

    List<Maison> findByProprietaireIdAndActifTrue(Long proprietaireId);

    Optional<Maison> findByIdAndProprietaireId(Long id, Long proprietaireId);
}
