package com.metereye.backend.repository;

import com.metereye.backend.entity.Alerte;
import com.metereye.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    // Toutes les alertes (triées)
    List<Alerte> findByDestinationOrderByDateCreationDesc(User user);

    // Alertes non lues
    List<Alerte> findByDestinationAndLueFalseOrderByDateCreationDesc(User user);
}