// CompteurRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.Compteur;
import com.metereye.backend.entity.User;
import com.metereye.backend.enums.TypeCompteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompteurRepository extends JpaRepository<Compteur, Long> {

    Optional<Compteur> findByReference(String reference);

    List<Compteur> findByProprietaire(User proprietaire);

    List<Compteur> findByProprietaireIdAndActifTrue(Long userId);

    List<Compteur> findByTypeCompteur(TypeCompteur typeCompteur);

    @Query("SELECT c FROM Compteur c WHERE c.typeCompteur = 'CASH_POWER' AND c.actif = true")
    List<Compteur> findAllCashPowerActifs();
}