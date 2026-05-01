// ESP32CamDeviceRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.ESP32CamDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour les modules ESP32-CAM
 */
@Repository
public interface ESP32CamDeviceRepository extends JpaRepository<ESP32CamDevice, Long> {
    
    Optional<ESP32CamDevice> findByDeviceCode(String deviceCode);
    
}
