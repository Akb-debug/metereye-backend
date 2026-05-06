// ESP32PzemDeviceRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.ESP32PzemDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour les modules ESP32-PZEM004T
 */
@Repository
public interface ESP32PzemDeviceRepository extends JpaRepository<ESP32PzemDevice, Long> {
    
    Optional<ESP32PzemDevice> findByDeviceCode(String deviceCode);
    
    //Optional<ESP32PzemDevice> findByQrCodeValue(String qrCodeValue);
}
