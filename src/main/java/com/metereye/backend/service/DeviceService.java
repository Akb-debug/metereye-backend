// DeviceService.java
package com.metereye.backend.service;

import com.metereye.backend.dto.*;
import com.metereye.backend.entity.ModuleESP32;
import com.metereye.backend.entity.User;

import java.util.List;

public interface DeviceService {

    // Onboarding
    DeviceResponseDTO scanAndRegisterDevice(DeviceScanDTO scanDTO, User user);
    
    DeviceResponseDTO associateDeviceToMeter(String deviceCode, DeviceAssociateDTO associateDTO, User user);
    
    DeviceResponseDTO deviceHandshake(String deviceCode, DeviceHandshakeDTO handshakeDTO);
    
    DeviceResponseDTO deviceHeartbeat(String deviceCode, DeviceHeartbeatDTO heartbeatDTO);
    
    // Gestion
    DeviceResponseDTO getDeviceStatus(String deviceCode, User user);
    
    List<DeviceResponseDTO> getUserDevices(User user);
    
    DeviceResponseDTO updateDeviceCaptureInterval(String deviceCode, Integer interval, User user);
    
    // Validation
    boolean isDeviceConfiguredAndActive(String deviceCode);
    
    boolean canDeviceSendReading(String deviceCode);
}
