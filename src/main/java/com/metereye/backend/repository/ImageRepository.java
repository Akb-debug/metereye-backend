// ImageRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    
    Optional<Image> findByReleve_Id(Long releveId);
    
    void deleteByReleve_Id(Long releveId);
}
