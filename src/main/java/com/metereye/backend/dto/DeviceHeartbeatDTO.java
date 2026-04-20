// DeviceHeartbeatDTO.java
package com.metereye.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceHeartbeatDTO {

    private LocalDateTime timestamp;
    
    private Integer batteryLevel;
    
    private Integer signalStrength;
    
    private Double temperature;
}
