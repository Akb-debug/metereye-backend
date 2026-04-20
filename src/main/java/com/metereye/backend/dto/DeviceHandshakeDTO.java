// DeviceHandshakeDTO.java
package com.metereye.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceHandshakeDTO {

    @NotBlank(message = "La version du firmware est obligatoire")
    private String firmwareVersion;

    private String ipAddress;
    
    private String wifiSsid;
}
