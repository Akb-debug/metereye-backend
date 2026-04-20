// AlerteRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.Alerte;
import com.metereye.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    List<Alerte> findByDestinationAndLueFalse(User destination);

    List<Alerte> findByDestinationOrderByDateCreationDesc(User destination);

    List<Alerte> findByEnvoyeeFalse();
}